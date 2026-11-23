# KytyPS5 Android Engine Integration Assessment

**Status:** research and integration planning only. No emulator engine has been copied or linked into this repository.

## Current application boundary

The current app is a Kotlin Android launcher shell. It provides a dark launcher screen and Android Storage Access Framework folder selection. Game launching must remain disabled until a real native engine is integrated and validated.

The target device is the Realme GT Neo 5 SE (Snapdragon 7+ Gen 2, Adreno 725, 16 GB RAM). The upstream Android port's published test device is Snapdragon 8 Elite / Adreno 830. Upstream explicitly does not establish compatibility with Adreno 725.

## Upstream engine

- Repository: https://github.com/tarek369/KytyPS5-Android
- Android branch: `android-boot-wip`
- Android status report: https://github.com/tarek369/KytyPS5-Android/blob/android-boot-wip/docs/android/STATUS.md
- Upstream Android contributor/build instructions must be treated as authoritative. The documented toolchain includes Android NDK 28.2, CMake, Ninja and Gradle.
- The Android port uses FEXCore for x86-64 guest execution and Vulkan/Turnip on supported Qualcomm GPUs. These are native, high-complexity dependencies, not drop-in Android libraries.

## Compatibility risks

1. **GPU:** Adreno 725 is not among the upstream-reported test GPUs. Vulkan feature/extension support and Turnip driver behavior must be queried on the actual phone.
2. **Memory:** Upstream recommends 16 GB RAM. Some reported games consume a large fraction of that; Android background services and the app itself reduce available memory.
3. **Thermals/performance:** Successful boot does not imply playable frame rates. Sustained CPU/GPU load may trigger thermal throttling.
4. **ABI/toolchain:** All native components must be built for Android `arm64-v8a` with mutually compatible NDK/CMake settings and matching JNI interfaces.
5. **Licensing:** KytyPS5 Android is GPL-2.0-only. Any derived integration or redistribution must satisfy GPL obligations and preserve third-party notices. Do not bundle upstream binaries until provenance and license obligations are reviewed.

## Safe integration sequence

- [ ] Pin and record the exact upstream commit and every submodule revision; do not track a moving branch in a reproducible build.
- [ ] Read the upstream Android build guide and inventory native targets, generated artifacts, third-party licenses and JNI entry points.
- [ ] Confirm the phone's Android API level, ABI, Vulkan version/extensions, available memory and driver capabilities.
- [ ] Build the upstream Android project independently before modifying this launcher.
- [ ] Establish a minimal JNI bridge with explicit lifecycle/error handling; keep the launcher responsive and never report a game as playable before engine confirmation.
- [ ] Add engine diagnostics and a compatibility gate. Unsupported devices must show a clear explanation rather than crash.
- [ ] Test first with a legally obtained, low-demand homebrew/test title or compatible owned dump; record boot status, frame rate, memory, temperature and crashes.
- [ ] Only after repeatable tests, consider enabling game launch in the UI.

## Build and CI policy

- Do not add push-triggered workflows or consume Actions minutes for unverified native builds.
- Prefer manual workflow dispatch after the upstream build procedure is understood.
- Never commit signing keys, game files, firmware, console keys, credentials or local SDK paths.
- Keep this work on `feature/kyty-android-engine`; do not change the default branch or merge automatically.

## Current decision

**Do not copy the engine into the app yet.** First complete the upstream source/submodule/toolchain audit and a separate upstream ARM64 Android build. A launcher-only APK is not a PS5 emulator, and no compatibility claim is made for Adreno 725.
