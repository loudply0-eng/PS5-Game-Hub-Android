# Unified KytyPS5 App and In-App Updater Integration

## Status

This is an integration gate, not a release claim. The pinned KytyPS5 source currently builds a standalone APK. It is not yet the PS5 Game Hub application with its updater.

Verified source facts at upstream commit `9ab8b82daf2de1f763491b005e1ed1b28f843bb7`:

- Upstream Android application ID: `com.kytyps5.android`.
- Existing hub application ID: `com.ps5gamehub.android`.
- Upstream launcher is `com.kytyps5.android.MainActivity`; it owns the game-library UI and launches `GameActivity`.
- Upstream manifest does not declare `REQUEST_INSTALL_PACKAGES` or a `FileProvider`.
- Upstream Gradle signing is optional and falls back to debug signing when release credentials are absent.
- The upstream launcher has no in-app release checker/downloader.
- The existing hub updater is implemented in `app/src/main/java/com/ps5gamehub/android/MainActivity.kt` and `UpdatePolicy.kt`, but that activity is only a shell and does not launch KytyPS5.

## Required integration architecture

The final APK must be one application, not two separately installable APKs:

1. Keep the upstream KytyPS5 game-library and SDL3 `GameActivity` implementation.
2. Port the existing updater into the KytyPS5 launcher without replacing its game-library UI or breaking its existing worker lifecycle.
3. Use the established package ID `com.ps5gamehub.android` for the production application. Do not ship a package-ID change as an update.
4. Add the updater manifest requirements to the unified manifest:
   - `android.permission.INTERNET`
   - `android.permission.REQUEST_INSTALL_PACKAGES`
   - a non-exported AndroidX `FileProvider` with authority `${applicationId}.fileprovider`
   - provider paths limited to the private `cache/update/` directory
   - `android:allowBackup="false"` and `android:usesCleartextTraffic="false"`
5. Preserve the updater trust policy: stable semantic versions only, exactly one APK asset, HTTPS-only allowlisted download hosts, validation of every redirect, bounded APK size and redirect count, exact Content-Length, SHA-256 verification, private cache, and Android system installer handoff.
6. Automatically check for stable updates at launcher startup and keep a manual check action. Android still requires the user/system confirmation to complete APK installation; silent installation must not be claimed.
7. Use a monotonically increasing `versionCode` and preserve the exact production signing certificate. Never create a replacement keystore or silently use a debug key for a release.
8. Retain KytyPS5 GPL-2.0-only notices and provide the corresponding modified source when distributing the app.

## Mandatory release gates

- [ ] Unified debug APK contains the real ARM64 KytyPS5 libraries and the KytyPS5 game-library UI.
- [ ] Merged manifest contains the narrowly scoped updater provider and install permission.
- [ ] APK application ID is exactly `com.ps5gamehub.android`.
- [ ] Release build fails closed if production signing material is absent.
- [ ] `apksigner` verifies the release APK and its certificate SHA-256 matches the established production certificate.
- [ ] Unit tests cover semantic-version rejection, unsafe URLs, redirect host changes, redirect limits, invalid/missing digest, and download-size mismatch.
- [ ] Device test installs the first signed build, downloads a higher version from a test release, verifies the digest, invokes Android's installer, and confirms the update installs over the existing app while preserving app data.
- [ ] No release or tag is published until explicitly authorized.

## Current limitation

The current native-engine workflow builds `engine/KytyPS5-Android/android` as a standalone app. Its APK uses `com.kytyps5.android` and debug signing. It is useful as an engine-build validation artifact only; it is not an update for the existing Hub app and must not be presented as the final application.

## Implementation approach

Because the engine is currently a pinned Git submodule, editing the parent repository cannot modify the upstream launcher's source files. The integration must first establish a maintained, license-compliant source location (a project-owned fork or a vendored, pinned source snapshot with full license notices). Only then should the unified Android module and updater be built together. Do not attempt to patch generated files inside a submodule during CI.
