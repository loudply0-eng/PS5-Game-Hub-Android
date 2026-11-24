# Production Android build and release

The production workflow compiles the pinned KytyPS5 ARM64 engine and FFmpeg, packages the existing KytyPS5 launcher/game library, integrates the verified in-app updater, signs the Android APK, and uploads a private GitHub Actions artifact. Branch builds do not publish a public GitHub Release.

## Latest production build — passed

- Workflow: [run 45438754578](https://github.com/loudply0-eng/PS5-Game-Hub-Android/actions/runs/45438754578)
- Job: `Build signed KytyPS5 Game Hub production APK` — **success**.
- Release version: `1.0.0`; Android versionCode: `100006`.
- Artifact: `ps5-game-hub-production-apk-6`, artifact ID `14945288481`.
- APK SHA-256: `ba68f4f95cbfedd818908af7a6fdc9b262fdde2e6b601aefdd3854565d0b4b76`.
- The APK in the Actions artifact was independently compared byte-for-byte with the separately retrieved APK; the SHA-256 values match.
- The artifact certificate report confirms a single RSA 3072-bit signer: `CN=PS5 Game Hub, OU=Android, O=PS5 Game Hub, C=IQ`. The workflow compared the APK signer SHA-256 with `ANDROID_SIGNING_CERT_SHA256` and passed.
- Android build ran `testDebugUnitTest` and `assembleRelease` successfully.
- The workflow verified package `com.ps5gamehub.android`, permission `android.permission.REQUEST_INSTALL_PACKAGES`, and these six ELF64 AArch64 libraries:
  - `libmain.so`
  - `libSDL3.so`
  - `libhook_impl.so`
  - `libmain_hook.so`
  - `libfile_redirect_hook.so`
  - `libgsl_alloc_hook.so`
- The independent APK ZIP inspection also passed and confirmed all six shared libraries are ELF64 little-endian AArch64.
- Public release publication was skipped deliberately (`publish_release=false`). No public tag or GitHub Release was created.

## Production signing identity

A new dedicated PS5 Game Hub production signing identity was generated on the owner's Oracle server and added to the repository Actions secrets. Its signer identity is shown in the certificate report above. It is a **new** signing identity and cannot update an older installation signed with a different certificate.

Required GitHub Actions secrets:
- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`
- `ANDROID_SIGNING_CERT_SHA256`

The private keystore and password are stored outside the repository at the owner's server path:
- `/home/opc/app-signing/ps5-game-hub-release.jks`
- `/home/opc/app-signing/ps5-game-hub-release.password`

The owner should keep an encrypted offline backup of both files. Never commit the keystore, password, backup archive, or secret values to GitHub. Never paste them into chat.

## In-app update behavior and publication gate

The KytyPS5 launcher preserves the game-library and launch flow. `MainActivity` invokes the updater automatically during `onCreate`, and the launcher panel exposes a manual **Check for updates** action.

The updater:
- checks the repository's latest stable GitHub Release;
- ignores drafts and prereleases and requires a strict stable semantic version;
- requires exactly one APK asset and GitHub's SHA-256 digest;
- validates HTTPS download hosts and redirect hops;
- enforces APK size and exact downloaded length, then verifies SHA-256 before installation;
- passes the verified APK to Android's system package installer.

**No public stable GitHub Release currently exists.** Therefore, until a first stable release is explicitly authorized and published, the in-app check will report that no official release is available. Once the first release is published, subsequent higher stable versions can be discovered and updated from inside the app.

Android requires the user to approve installation in the system installer; an ordinary app cannot silently install its own update.

## Remaining physical-device validation

The signed production APK has passed CI/build and static artifact checks, but it has not yet been installed or launched on the target phone in this workflow. Still required:
- install the APK on the target Realme phone (remove any differently signed debug build first, if present);
- confirm the launcher opens and scans the selected game folder;
- confirm native KytyPS5 startup and Vulkan/Adreno behavior;
- test an actual in-app update after a stable release is published;
- test owned-game compatibility before claiming a particular game is playable.

A successful Android build does not establish that commercial PS5 games are playable on the target device.

## Public release authorization

Publishing `v1.0.0` requires a protected version tag or an explicitly authorized manual publish from `main`. Do not bypass tag protection, change rulesets, merge draft PRs, or publish a release without the repository owner's explicit authorization.
