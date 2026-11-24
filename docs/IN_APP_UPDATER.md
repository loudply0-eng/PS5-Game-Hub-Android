# In-app update system

The in-app updater is a required product feature. The Android app now has an implementation draft on the integration branch; it still needs a successful CI build and device testing before it can be called production-ready.

## User experience

- The app automatically checks for updates when it opens; a visible **Check for updates** action also allows a manual check. Both query the official repository's GitHub Releases API.
- Only the latest stable published release is considered. Draft and prerelease releases are rejected.
- The release tag is parsed as a numeric version and compared with the installed app version. Equal or older versions are not installed.
- When a newer version exists, the app displays the version and release notes and asks the user before downloading.
- The APK is downloaded into app-private cache, streamed to disk, and checked against the SHA-256 digest returned by GitHub's release-asset API.
- The app only accepts the configured GitHub repository and GitHub release asset hosts. Unexpected hosts, missing/invalid digests, failed downloads, and hash mismatches are rejected.
- The verified APK is handed to Android's package installer using a non-exported FileProvider. Android performs the package/signing-certificate compatibility check against the already-installed app and requires the user's approval.
- A normal in-place Android package update preserves the app's private data. The updater must never delete user game folders, saves, firmware, or configuration.

## Release contract

Every stable release intended for in-app delivery must include:
- A versioned APK asset ending in `.apk`.
- A GitHub release asset SHA-256 digest (the GitHub API `digest` field must be present and formatted as `sha256:<64 hex characters>`).
- A tag matching the app's numeric version format, such as `v0.2.0`.
- APK signed with the same Android signing identity as the installed app. Android's package installer rejects a package whose signing identity is incompatible.

The release process must protect the Android signing keystore and passwords in GitHub Actions secrets. Never commit signing keys, print secrets in logs, or reuse a debug signing key for public releases. The Android CI workflow builds a debug APK for verification. A separate tag-triggered release workflow builds and publishes a signed release APK. It requires these repository Actions secrets: `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, and `ANDROID_KEY_PASSWORD`. Do not create a public release until those secrets contain the same long-term signing identity used for the installed app.

## Android platform behavior

- Android does not allow an ordinary app to silently replace itself. The updater can download and verify the APK, then launch the system installer; the user must approve the installation.
- On Android 8+, the user may need to allow this app to install unknown apps in system settings.
- APKs are stored only in app-private cache and exposed through a scoped FileProvider URI, not a world-readable file path.
- The updater does not execute downloaded scripts, dex files, or native libraries outside the signed APK package.

## Security boundaries and follow-up hardening

- GitHub HTTPS and the release asset digest protect transport integrity and detect corrupted or substituted bytes. The Android package installer independently enforces the installed package's signing identity.
- A detached signed update manifest can be added as an additional provenance layer, with a pinned public key and a private signing key held only in protected CI secrets. Do not enable such a scheme until its keypair, rotation/recovery procedure, and CI signing implementation are in place.
- Add tests for version comparison, malformed tags, missing digest, invalid digest, wrong hash, unexpected download host, interrupted download, no-update state, and Android installer rejection for a mismatched signing certificate.
- CI must pass before a release is published. Never publish a release for a failed or unverified build.

## Current status

The in-app automatic check, manual check, version comparison, APK download, SHA-256 verification, and Android installer handoff are implemented on the feature branch. The debug CI build has passed, but the latest changes still need CI validation and physical-device testing. The signed release workflow is present but cannot publish until the four signing secrets are configured. A published stable release with a correctly signed APK and GitHub asset digest is required before the updater can install a real update.
