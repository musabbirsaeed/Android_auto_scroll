# TikTok Auto Scroll (Android, Java)

Accessibility + floating overlay app that auto-swipes TikTok when a video ends.

## Features
- Runs on top of official TikTok (`com.zhiliaoapp.musically`).
- Floating control with:
  - Start/Pause auto-scroll
  - Switch swipe direction (Up/Down)
- Detects end-of-video using visible countdown/time text in TikTok's UI.

## One-click Android Studio setup
1. Install Android Studio (latest stable) with Android SDK Platform 34.
2. Clone this repo and open it in Android Studio.
3. Copy `local.properties.example` to `local.properties`, then set `sdk.dir`.
4. Click **Sync Project with Gradle Files**.
5. Select `app` run configuration and run on your device.

This repository includes Gradle Wrapper (`./gradlew`) pinned to Gradle 8.14.4.


## If your platform rejects binary files
Some Git platforms/tools reject binary diffs (for example `gradle-wrapper.jar`).
If you see `Binary files are not supported`, run this once after clone:

```bash
gradle wrapper --gradle-version 8.14.4 --no-validate-url
```

This regenerates `gradle/wrapper/gradle-wrapper.jar` locally so `./gradlew` works.

## If push is rejected (common fix)
If `git push` fails because generated files were included (for example `build/`), clean and commit again:

```bash
rm -rf build app/build
git add -A
git commit -m "chore: remove generated build artifacts"
git push
```

## Build from terminal
```bash
./gradlew clean assembleDebug
```
Output APK:
- `app/build/outputs/apk/debug/app-debug.apk`

## Release APK setup (keystore-ready)
1. Generate keystore (example):
   ```bash
   keytool -genkeypair -v -keystore app/keystore/release.keystore -alias release -keyalg RSA -keysize 2048 -validity 10000
   ```
2. Create `keystore.properties` in project root:
   ```properties
   storeFile=app/keystore/release.keystore
   storePassword=YOUR_STORE_PASSWORD
   keyAlias=release
   keyPassword=YOUR_KEY_PASSWORD
   ```
3. Build release APK:
   ```bash
   ./gradlew clean assembleRelease
   ```

Output APK:
- `app/build/outputs/apk/release/app-release.apk`

## GitHub Actions CI artifacts
Workflow: `.github/workflows/android-build.yml`
- Builds debug APK on every push/PR.
- Uploads `app-debug.apk` as workflow artifact.
- Also builds and uploads release APK artifact.

---

## Click-by-click GitHub guide (push + build + download APK)

### A) Create GitHub repository and push code
1. Go to **https://github.com** and sign in.
2. Click top-right **+** → **New repository**.
3. Repository name: `Android_auto_scroll` (or any name you prefer).
4. Keep it **Public** or **Private** (your choice).
5. Click **Create repository**.
6. In your local terminal, from this project folder, run:
   ```bash
   git remote add origin https://github.com/<your-username>/<your-repo>.git
   git branch -M main
   git push -u origin main
   ```

### B) Trigger APK build on GitHub Actions
1. Open your repo page.
2. Click **Actions** tab.
3. Select workflow: **Android Build**.
4. Click **Run workflow** (right side) → choose `main` branch → **Run workflow**.
5. Wait until both jobs finish (green check).

### C) Download debug APK
1. Open the finished workflow run.
2. Scroll to **Artifacts** section.
3. Click **app-debug-apk** to download ZIP.
4. Extract ZIP → get `app-debug.apk`.

### D) Install on your Android phone
1. Copy `app-debug.apk` to your phone.
2. Open file manager on phone and tap APK.
3. Allow **Install unknown apps** when prompted.
4. Install app.

### E) First run setup inside app
1. Open the app.
2. Tap **Open Accessibility Settings** and enable service.
3. Tap **Grant Overlay Permission** and allow it.
4. Open TikTok and use floating controls.
## Setup
1. Open app.
2. Grant overlay permission.
3. Enable accessibility service for this app.
4. Open TikTok and enable `Start` in floating control.

## Notes
TikTok UI can vary by version/region/device. If no time text is exposed to accessibility tree, end detection may require a per-device heuristic.
