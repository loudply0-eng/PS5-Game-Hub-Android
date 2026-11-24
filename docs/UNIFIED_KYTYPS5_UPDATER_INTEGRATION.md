# Unified KytyPS5 App and In-App Updater Integration

## Current verified status

- The maintained KytyPS5 fork integration was merged into its default `android-boot-wip` branch through [fork PR #1](https://github.com/loudply0-eng/KytyPS5-Android/pull/1).
- Parent preparation work was merged into `main` through [PR #4](https://github.com/loudply0-eng/PS5-Game-Hub-Android/pull/4).
- The unified application, native ARM64 build pipeline, and signed production workflow were merged into `main` through [PR #5](https://github.com/loudply0-eng/PS5-Game-Hub-Android/pull/5).
- This PR #6 records the integration architecture, verified production artifact, updater behavior, and remaining device/release gates.

## Verified source and identity

- Reviewed upstream starting revision: `9ab8b82daf2de1f763491b005e1ed1b28f843bb7`.
- Maintained project fork revision pinned by the parent workflow and submodule: `dac144daa519ef7f3a0857a16d9bca7cb6289f42`.
- Upstream Android application ID was `com.kytyps5.android`.
- Unified PS5 Game Hub package ID is `com.ps5gamehub.android`.
- Kotlin namespace remains `com.kytyps5.android` to preserve existing source/activity references.
- The KytyPS5 launcher retains its game library and SDL3 `GameActivity`; the updater is a separate `InAppUpdater` component attached to the launcher UI.

## Implemented unified application

The production APK built successfully in workflow run [45438754578](https://github.com/loudply0-eng/PS5-Game-Hub-Android/actions/runs/45438754578).

Verified build facts:

- Release version: `1.0.0`; Android `versionCode=100006`.
- Private Actions artifact: `ps5-game-hub-production-apk-6`, artifact ID `14945288481`.
- APK SHA-256: `ba68f4f95cbfedd818908af7a6fdc9b262fdde2e6b601aefdd3854565d0b4b76`.
- `testDebugUnitTest` and `assembleRelease` completed successfully.
- The production APK was signed with the dedicated PS5 Game Hub RSA-3072 key, and the workflow verified that its SHA-256 certificate fingerprint matches the configured `ANDROID_SIGNING_CERT_SHA256` secret.
- Package identity `com.ps5gamehub.android` and `android.permission.REQUEST_INSTALL_PACKAGES` were verified.
- All six expected ELF64 AArch64 libraries were staged and checked:
  - `libmain.so`
  - `libSDL3.so`
  - `libhook_impl.so`
  - `libmain_hook.so`
  - `libfile_redirect_hook.so`
  - `libgsl_alloc_hook.so`

This signed APK is a private Actions artifact, not a public GitHub Release.

## In-app updater behavior

The existing KytyPS5 launcher calls the updater automatically during startup and also displays a manual **Check for updates** action.

The updater checks the repository's latest stable GitHub Release. It rejects drafts and prereleases, requires a strict three-part stable semantic version, exactly one APK asset, and a GitHub SHA-256 asset digest. Downloads are restricted to HTTPS GitHub asset hosts, every redirect is validated, APK size and content length are bounded/checked, and SHA-256 is verified before the APK is passed to Android's package installer.

Android's package installer still requires the device owner to approve the installation. Silent installation is not claimed.

**No public stable release has been published yet.** The updater is integrated but has no public APK asset to offer until a stable GitHub Release is published.

The private production artifact is version `1.0.0`. The first public release should be `v1.0.1` so an installation of that private `1.0.0` APK sees the published version as a real update. The signed release workflow now runs only for a protected `v*` tag or a deliberate manual dispatch; the manual workflow default is `1.0.1`.

The repository currently has an active ruleset protecting creation, update, and deletion of `v*` tags, with no bypass actor configured. The current connected account cannot bypass this rule. The ruleset was deliberately left unchanged; the repository owner must authorize a permitted release-tag creation path before the public release can be published.

## Signing identity and preservation

A new dedicated PS5 Game Hub production signing key was generated and its five required Actions secrets were configured. This is a new identity; it will not update an older APK signed with a different certificate.

The owner's server-side keystore and password files are:
- `/home/opc/app-signing/ps5-game-hub-release.jks`
- `/home/opc/app-signing/ps5-game-hub-release.password`

Keep encrypted backups outside the server. Never commit, publish, or share these private files or their secret values.

## Remaining verification gates

- [ ] Install the signed production APK on the target Realme phone. Remove any differently signed debug installation first, if present.
- [ ] Confirm the launcher opens and scans the selected games folder.
- [ ] Confirm native KytyPS5 startup and Vulkan/Adreno 725 behavior on the actual device.
- [ ] Authorize and publish the first stable GitHub Release `v1.0.1` so the updater has a real APK asset and digest.
- [ ] Install the higher version from the in-app updater; verify Android's installer approval and preservation of app data.
- [ ] Test owned-game compatibility before claiming any commercial PS5 game is playable.
- [ ] Keep GPL-2.0-only and third-party notices, and satisfy applicable source-distribution obligations for distributed derived binaries.

## Important limitation

A successful native build and six packaged ARM64 libraries do not prove that the emulator runs correctly on every Android device or that PS5 commercial games are playable. Physical-device and owned-game testing remain mandatory.
