# AttendancePro

A selfie-based attendance app for Admin and Staff, built as a hiring assignment submission.
Local-only, no backend.

## Demo credentials

| Role  | Username | Password |
|-------|----------|----------|
| Admin | `admin`  | `admin`  |
| Staff | `staff`  | `staff`  |

## Technology / architecture

- **Kotlin + Jetpack Compose**, single Activity, **Navigation Compose** for screen routing.
- **MVVM**: each screen has a `@HiltViewModel` exposing a `StateFlow<UiState>`; screens are
  stateless composables that render that state and forward user actions.
- **Hilt** for dependency injection (`di/AppModule`, `di/DatabaseModule`).
- **Room** for local persistence only — no backend, no network calls except downloading the
  bundled face-embedding model at build time (see below); the app itself is fully offline.
- **CameraX** (`camera/SelfieCamera.kt`) drives the front camera preview and JPEG capture,
  reused by both the Face Enrolment and Mark Attendance screens.
- **ML Kit Face Detection** (`face/FaceRecognitionManager.kt`) verifies that a captured selfie
  contains exactly one face and crops it before embedding.
- **TFLite face embedding** (`face/FaceEmbedder.kt`) turns the cropped face into a 128-d vector;
  matching is done with cosine similarity, threshold configurable in
  `FaceRecognitionManager.DEFAULT_MATCH_THRESHOLD` (default `0.65`).
- **FusedLocationProviderClient** (`location/LocationHelper.kt`) captures a one-shot high-accuracy
  location at the moment attendance is marked.

### Package layout

```
data/local/        Room entities, DAOs, database
data/repository/    StaffRepository, AttendanceRepository
di/                 Hilt modules
face/                FaceRecognitionManager, FaceEmbedder, cosine similarity, embedding codec
camera/              Reusable CameraX selfie capture composable
location/            FusedLocationProviderClient wrapper
navigation/          Screen routes + NavHost graph
ui/screens/*/        One package per screen: Screen.kt + ViewModel.kt
ui/components/       Shared composables (permission gate, selfie thumbnail)
util/                File/date helpers, EXIF-aware bitmap decoding, Role enum
```

### Data model

- `StaffEntity`: `id, name, employeeId, faceEmbedding (nullable, comma-separated floats), enrolledSelfiePath`
- `AttendanceEntity`: `id, staffId, date, time, selfiePath, latitude, longitude, matched`

### Face recognition flow

1. **Enrolment** (Admin → Add Staff → Face Enrolment): admin enters name + employee ID, staff
   record is created, then the camera opens. ML Kit must find exactly one face in the selfie;
   the face is cropped, embedded, and the embedding + selfie path are saved on the staff record.
2. **Attendance** (Staff → Mark Attendance): staff opens the camera and takes a selfie. ML Kit
   again requires exactly one face. The new embedding is compared via cosine similarity against
   **every enrolled staff member's** embedding (the staff login is generic, not tied to one
   person — the matched face determines whose attendance is recorded, which is how the app knows
   *which* staff ID to save without a per-person login). If the best match clears the threshold,
   an `AttendanceEntity` is saved with the matched staff ID, current date/time, the selfie, and
   the device's current lat/lng. If no face, more than one face, or no match above the threshold,
   nothing is written — attendance is only ever recorded on a genuine match.
3. **History** (Admin → tap a staff member): shows every recorded attendance row for that staff
   member with its selfie, date, time, and coordinates.

All of the above logic lives in `FaceRecognitionManager`, used identically by both the enrolment
and attendance screens, so there is exactly one place that decides what counts as "one face" and
what counts as "a match."

## MobileFaceNet — limitation and substitution (read this)

The assignment asks for MobileFaceNet embeddings, and asks us **not to fake face recognition**,
with the explicit fallback: *"if MobileFaceNet integration is blocked, document the limitation
clearly."* That fallback applies here.

This environment has outbound network access but no way to verify the provenance or license of a
random `MobileFaceNet.tflite` binary pulled from an arbitrary GitHub repo — several plausible
sources were checked and either didn't exist or had no clear license. Rather than bundle an
unverifiable binary into a hiring submission, the app instead bundles a **real, working,
Apache-2.0-licensed face-embedding TFLite model from the same family** (`assets/facenet.tflite`,
a 128-d FaceNet model, sourced from
[shubham0204/FaceRecognition_With_FaceNet_Android](https://github.com/shubham0204/FaceRecognition_With_FaceNet_Android),
Apache License 2.0). This is a genuine trained face-recognition network — not a mock, not a
random-vector stand-in. It plugs into the exact same interface (crop → resize → per-image
standardize → 128-d embedding → cosine similarity) that a MobileFaceNet `.tflite` would use.

Swapping in an actual MobileFaceNet model later is a one-file change: drop the `.tflite` into
`app/src/main/assets/`, update `FaceEmbedder.ModelSpec` (asset filename, input size, embedding
size) to match, and adjust preprocessing if MobileFaceNet's expected normalization differs
(MobileFaceNet models are typically trained with `(pixel - 127.5) / 128` normalization at 112×112
input rather than this model's 160×160 per-image standardization). Everything else —
`FaceRecognitionManager`, matching, storage — is unaffected.

Because the substituted model's embedding space differs from MobileFaceNet's, the assignment's
suggested `0.65` cosine threshold is kept as the default (it's a single named constant,
`FaceRecognitionManager.DEFAULT_MATCH_THRESHOLD`) but may need recalibration for this specific
model in practice — the model author's own reference implementation recommends a lower threshold
(~0.4) for genuine matches with this particular network. Test on-device and adjust that one
constant if you see false rejects/accepts.

## How to run

1. Open the project root in Android Studio (the version that ships AGP 9.3.2 / Kotlin 2.2.10 or
   compatible).
2. Let Gradle sync — it will download dependencies including the bundled face embedding model,
   which is already committed under `app/src/main/assets/facenet.tflite` (~23.7 MB), so no extra
   download step is required to build.
3. Run on a **physical device** with a front camera (an emulator's virtual front camera also
   works for exercising the flow, but real face recognition needs a real camera feed). Minimum
   SDK 24.
4. Grant camera and location permissions when prompted (Face Enrolment needs camera only; Mark
   Attendance needs camera + location).
5. Log in as `admin/admin`, add a staff member, enrol their face, log out, log back in as
   `staff/staff`, and mark attendance.

Command line: `./gradlew assembleDebug`, APK at `app/build/outputs/apk/debug/app-debug.apk`.

## Assumptions & limitations

- **Staff login is generic**, not per-person (`staff/staff` for anyone marking attendance). The
  attendance flow identifies *which* staff member it is entirely from the face match against all
  enrolled embeddings — this is what the assignment's data model implies (`Attendance.staffId` is
  filled in by the app, not chosen by the person logging in).
- Attendance is **only ever persisted when a face match clears the threshold**; failed detections
  (no face / multiple faces / no match) show an error and write nothing, per the assignment.
- Selfies are stored as JPEGs in app-private storage (`context.filesDir/selfies/`); embeddings are
  stored as comma-separated floats on the `staff` row rather than a separate blob table, to keep
  the schema small for this assignment's scope.
- No edit/delete staff, no attendance editing, no offline queueing of location — all out of scope
  per the assignment's screen list.
- If location can't be obtained (permission edge case, no GPS fix), attendance is still recorded
  with `(0.0, 0.0)` rather than blocking attendance on a location failure; a stricter policy would
  reject the record entirely, but the assignment doesn't specify this and blocking a legitimate,
  face-verified attendance on a flaky GPS fix seemed like the worse trade-off for a demo.
- Debug APK is ~96 MB, mostly ML Kit's bundled face-detection models for multiple ABIs and the
  23.7 MB embedding model — acceptable for a local sideload/demo build, not optimized for a store
  release (no ABI splits / R8 shrinking configured).
- Built against a very recent AGP (9.3.2) that has experimental "built-in Kotlin" compilation;
  Room/Hilt's KSP-based annotation processors register sources through an API that trips AGP's new
  strict check, so `gradle.properties` sets `android.disallowKotlinSourceSets=false` to allow it.
  This is a build-tooling workaround, not an app behavior change.

## Conversation export

`claude-conversation.json` in the repo root is the JSON export of the Claude Code conversation
this project was built in.
