# Production Android build and release

The production workflow compiles a pinned KytyPS5 ARM64 engine and FFmpeg, packages the launcher/game library and updater, signs the APK, verifies the signing certificate, and uploads an Actions artifact. Integration-branch builds do not publish a public GitHub Release.

## Latest production build — 1.0.4 passed

- Workflow: [run 45849763792](https://github.com/loudply0-eng/PS5-Game-Hub-Android/actions/runs/45849763792).
- Job: `Build signed KytyPS5 Game Hub production APK` — **success**.
- Build commit: `914c0fbb27c26b111770aaaab2f3316b6280a762`.
- Pinned engine commit: `24019a0232f7f80312614230bceaa0096399573a`.
- Version name: `1.0.4`; Android versionCode: `100019`.
- Application ID: `com.ps5gamehub.android`.
- Artifact: `ps5-game-hub-production-apk-19`, artifact ID `15134133012`, uploaded 2026-11-26 and scheduled to expire 2026-12-26.
- APK SHA-256: `516c04a8d339433b56f5d5a018b4401bf119312bebeb3e2d98cc1c191e502243`.
- The APK in the downloaded Actions artifact was compared byte-for-byte with the delivered APK; the hashes match.
- Android build ran `testDebugUnitTest` and `assembleRelease` successfully.
- The workflow verified package `com.ps5gamehub.android`, versionName/versionCode, and permission `android.permission.REQUEST_INSTALL_PACKAGES`.
- All six packaged native libraries were independently checked as ELF64/AArch64: `libmain.so`, `libSDL3.so`, `libhook_impl.so`, `libmain_hook.so`, `libfile_redirect_hook.so`, and `libgsl_alloc_hook.so`.
- The artifact's certificate report shows one RSA 3072-bit signer: `CN=PS5 Game Hub, OU=Android, O=PS5 Game Hub, C=IQ`.

## Update-signature verification against public v1.0.3

The previous production build [v1.0.3](https://github.com/loudply0-eng/PS5-Game-Hub-Android/releases/tag/v1.0.3) used Android versionCode `100018`. The downloaded v1.0.3 and v1.0.4 Actions artifacts were inspected independently. Both certificate reports contain this identical signer SHA-256 fingerprint:

```
6a7f318716ecd858f3f449466c237a261f98122154b2214ce90b0ca0f1ddb791
```

Both reports also show the same certificate DN, RSA key size, certificate SHA-1, and public-key SHA-256. VersionCode increased from `100018` to `100019`, and the application ID stayed `com.ps5gamehub.android`. These checks establish that 1.0.4 is signed as an update to the v1.0.3 production app; the Android Package Installer on the actual phone must still complete the real installation test.

## Previous production build — v1.0.3

- Workflow: [run 45633294147](https://github.com/loudply0-eng/PS5-Game-Hub-Android/actions/runs/45633294147).
- Version name: `1.0.3`; Android versionCode: `100018`.
- Public Release: [v1.0.3](https://github.com/loudply0-eng/PS5-Game-Hub-Android/releases/tag/v1.0.3).
- APK SHA-256: `ebe72f9fabc320ad40811fe695ecc352e6a2cb82b0cd785b0dd01a3798bcc958`.

## Production signing identity

The dedicated PS5 Game Hub production signing identity is stored in GitHub Actions secrets and outside the repository on the owner's server. Do not expose, commit, or paste the keystore or password.

Required GitHub Actions secrets:
- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`
- `ANDROID_SIGNING_CERT_SHA256`

The private keystore and password are stored outside the repository at the owner's server path:
- `/home/opc/app-signing/ps5-game-hub-release.jks`
- `/home/opc/app-signing/ps5-game-hub-release.password`

Keep an encrypted offline backup of both files. Never commit the keystore, password, backup archive, or secret values. The dedicated production identity can update installations signed with that same certificate; it cannot update an older installation signed with a different certificate.

## In-app update behavior and publication gate

The launcher checks the repository's latest stable GitHub Release and exposes a manual **Check for updates** action. The updater:
- ignores drafts and prereleases and requires a strict stable semantic version;
- requires exactly one APK asset and GitHub's SHA-256 digest;
- validates HTTPS download hosts and redirect hops;
- enforces APK size and exact downloaded length, then verifies SHA-256 before installation;
- passes the verified APK to Android's system package installer.

**Current public stable release: v1.0.3.** The signed 1.0.4 build was intentionally uploaded as a private Actions artifact from the integration branch; no v1.0.4 public tag or Release was created. Therefore the in-app updater does not distribute this private artifact. Install 1.0.4 manually for validation. A subsequent stable version can be offered in-app only after an explicitly authorized public release is published.

Android requires the user to approve installation in the system installer; an ordinary app cannot silently install its own update.

## Physical-device validation still required

The 1.0.4 production APK passed CI/build, certificate matching, package identity, and static native-library checks. **It has not yet been installed or launched on the target phone in this workflow.** Required tests:
1. Install over the existing production app without uninstalling or clearing data.
2. Confirm Android's package installer accepts the update.
3. Confirm launcher startup, saved game-folder permission, and library contents.
4. Confirm native KytyPS5 startup and SDL3 library loading.
5. Collect Vulkan/Turnip initialization logs and inspect Adreno 725 behavior.
6. Test owned-game compatibility before claiming any game is playable.

A successful APK build does not establish runtime startup or game playability on the target device.

## Engine integration and review status

The 1.0.4 workflow pinned engine commit `24019a0232f7f80312614230bceaa0096399573a` from [KytyPS5-Android PR #4](https://github.com/loudply0-eng/KytyPS5-Android/pull/4). PR #4 remains open and unmerged. Its PKG support is metadata-only: it does not decrypt PFS/PFSC, extract or install PKG content, handle DRM/keys, or launch PKG entries. The separate [exFAT image-discovery PR #3](https://github.com/loudply0-eng/KytyPS5-Android/pull/3) also remains open as a draft. Neither PR was merged as part of the 1.0.4 build.

## Public release authorization

Do not bypass tag protection, change rulesets, merge draft PRs, or publish a public release without the repository owner's explicit authorization. The 1.0.4 integration-branch build deliberately skipped public release publication.
