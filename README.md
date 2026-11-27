# PS5 Game Hub — Android

An Android game hub and integration layer for the experimental KytyPS5 Android engine.

## Current state

- **Latest public stable release:** [PS5 Game Hub v1.0.3](https://github.com/loudply0-eng/PS5-Game-Hub-Android/releases/tag/v1.0.3).
- **Latest private production build:** **1.0.4**, built successfully by [Unified Signed Android Release run 45849763792](https://github.com/loudply0-eng/PS5-Game-Hub-Android/actions/runs/45849763792). It was uploaded as the private Actions artifact `ps5-game-hub-production-apk-19` (artifact ID `15134133012`); no public v1.0.4 tag or Release was published.
- The 1.0.4 APK has application ID `com.ps5gamehub.android`, versionCode `100019`, and versionName `1.0.4`.
- The v1.0.3 APK has versionCode `100018`. The production certificate report from both builds shows the same SHA-256 certificate fingerprint: `6a7f318716ecd858f3f449466c237a261f98122154b2214ce90b0ca0f1ddb791`. This makes 1.0.4 an Android package update candidate for the v1.0.3 production installation, subject to the normal on-device Package Installer check.
- The 1.0.4 APK SHA-256 is `516c04a8d339433b56f5d5a018b4401bf119312bebeb3e2d98cc1c191e502243`. The delivered APK was checked byte-for-byte against the APK inside the Actions artifact.
- The production APK contains six ELF64/AArch64 native libraries: `libmain.so`, `libSDL3.so`, `libhook_impl.so`, `libmain_hook.so`, `libfile_redirect_hook.so`, and `libgsl_alloc_hook.so`.
- **Physical-device installation, native startup, Vulkan initialization, and game compatibility are not yet validated.** A successful build is not evidence that a PS5 game runs on the target phone.

The 1.0.4 build came from integration commit `914c0fbb27c26b111770aaaab2f3316b6280a762` and pins KytyPS5 engine commit `24019a0232f7f80312614230bceaa0096399573a`. Engine PR [#4](https://github.com/loudply0-eng/KytyPS5-Android/pull/4) is still open and unmerged; the production build used the explicit commit pin without merging that PR.

## Engine selected

The maintained source fork is [loudply0-eng/KytyPS5-Android](https://github.com/loudply0-eng/KytyPS5-Android), derived from [tarek369/KytyPS5-Android](https://github.com/tarek369/KytyPS5-Android), branch `android-boot-wip`.

The Android port uses FEXCore JIT for PS5 x86-64 guest code on Android ARM64 and Vulkan graphics translation, with Mesa Turnip on supported Qualcomm Adreno GPUs. The upstream project is experimental and licensed GPL-2.0-only.

The current PKG probe change is metadata-only: it recognizes bounded outer-container metadata and deliberately does not decrypt, extract, install, or launch protected PKG contents. See [engine PR #4](https://github.com/loudply0-eng/KytyPS5-Android/pull/4).

See [docs/ENGINE_INTEGRATION.md](docs/ENGINE_INTEGRATION.md) for the integration architecture, build phases, licensing, security rules, and device-validation gates.

## Device target

Initial target: Realme GT Neo 5 SE (Snapdragon 7+ Gen 2 / Adreno 725).

The upstream project's published reference device is OnePlus Pad 3 (Snapdragon 8 Elite / Adreno 830 / 16 GB RAM). Upstream says other GPUs are untested. **Adreno 725 compatibility and performance are unknown until tested on the actual phone.** Do not claim a game is playable based only on APK build success.

## Build and updates

The integrated Android module lives at `engine/KytyPS5-Android/android` in the parent build workspace. The reproducible workflow uses Java 21, Android SDK 36, NDK 28.2, CMake, Ninja, glslangValidator, and the pinned FFmpeg build script.

The signed production workflow builds Release, verifies the native libraries and production signing certificate, and uploads an Actions artifact. The integration-branch workflow is configured with public publishing disabled. The private 1.0.4 artifact is **not** the latest public GitHub Release, so the in-app updater does not distribute it; 1.0.4 is currently a manual installation candidate. The public stable updater continues to use the public v1.0.3 release until a later stable release is explicitly authorized and published.

The updater verifies HTTPS download sources, APK length and SHA-256, then invokes Android's system package installer. Android still requires the user to approve the installation. See [docs/PRODUCTION_RELEASE_SETUP.md](docs/PRODUCTION_RELEASE_SETUP.md).

## Remaining work

1. Install 1.0.4 over the existing production app on the target Realme phone; verify the system package installer accepts the update and the app's data/library settings remain intact.
2. Verify launcher startup, game-folder access, native Kyty startup, SDL3 loading, Vulkan initialization, and Adreno 725 behavior using device logs.
3. Test only game dumps the owner is entitled to use; publish per-game compatibility results only after actual measurements.
4. Review and merge engine PR #4 only after maintainer review; it remains unmerged.
5. Publish a later stable tag/Release only with explicit owner authorization and after device validation.

## Legal and security

Use only game dumps you own. This project does not include games, saves, keys, firmware, or proprietary system software. Preserve GPL-2.0-only and third-party notices and meet the applicable source/distribution obligations when distributing derived binaries. Never commit game dumps, firmware, console keys, or signing secrets.
