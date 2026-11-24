# In-app update system

This is a required product feature, not an optional later enhancement.

## User experience

- Add a visible **Check for updates** action in the app's Settings/About screen (and optionally a small update indicator on the home screen).
- On tap, query the GitHub Releases API for the latest **published, non-draft, non-prerelease** release in the official app repository.
- Compare the release version with the installed version using Android `versionCode` / a strictly parsed semantic version. Never downgrade.
- If current, show “You are up to date”. If newer, show version, release notes, and download size before the user chooses to install.
- Download the APK over HTTPS with progress, cancellation, retry, and a clear failure state.
- Verify the downloaded APK against a SHA-256 digest and a detached Ed25519 signature whose public key is pinned in the app. The release workflow must sign the manifest; never trust a checksum fetched from the same release without an independent signature.
- Verify the APK package name matches the installed app, the signing certificate matches the installed app's signing identity, and the target version is newer before handing it to Android.
- Install through Android's supported package installer flow (PackageInstaller / system confirmation as required). Do not promise silent installation: ordinary Android apps cannot silently update themselves without privileged/device-owner management.
- Preserve user data and settings across a normal in-place update. Never delete user game folders, saves, firmware, or configuration as part of an update.

## Release contract

Publish one stable release per app version with:
- A versioned APK asset (for example, `ps5-game-hub-android-vX.Y.Z.apk`).
- A machine-readable `update-manifest.json` containing app ID, version name, version code, minimum supported Android version, APK asset name, APK SHA-256, and release notes URL.
- A detached signature for the exact manifest bytes (for example, `update-manifest.json.sig`) created by the release workflow using a protected signing key.
- A pinned verification public key embedded in the app. Keep private signing keys only in protected GitHub Actions secrets; never commit them or print them in logs.

The updater must reject malformed manifests, unsupported algorithms, missing signatures, hash mismatches, package ID mismatches, signing-certificate mismatches, expired/invalid metadata if expiry is adopted, and downgrade attempts. Use strict HTTPS and do not follow arbitrary manifest-provided URLs; resolve assets only from the configured official repository/release.

## Android implementation notes

- Use AndroidX/Jetpack components already adopted by the project; keep network, parsing, hashing, signature verification, and installation logic in a dedicated updater module/class rather than the Activity.
- Use a maintained HTTP client and stream downloads to app-private cache; do not load the full APK into memory.
- Use WorkManager only if background/retry behavior is needed. A user-triggered foreground download should expose progress and cancellation.
- For Android 8+, check/request the platform's “Install unknown apps” permission for this app only when needed, then launch the system package installer. The user must approve installation.
- Handle Android package visibility and FileProvider/PackageInstaller requirements correctly; never expose a world-readable APK file.
- Do not use a self-updater that downloads executable code or native libraries outside a signed APK update.

## Security and operational requirements

- The updater endpoint is fixed to the official repository: `loudply0-eng/PS5-Game-Hub-Android`.
- Do not accept arbitrary repository names, URLs, redirects to unrelated hosts, or update channels from remote manifest content.
- Keep GitHub API rate limits in mind; cache release metadata briefly and show a retry action on network errors.
- A failed update must leave the currently installed app intact.
- Add tests for version comparison, malformed manifest rejection, invalid signature, wrong hash, wrong package ID, wrong signing certificate, downgrade rejection, interrupted download, and no-update state.
- CI must produce the APK and signed manifest only after a successful build. Do not publish a release for a failed or unverified build.

## Current status

This document defines the required design. It does **not** mean the updater has been implemented or tested yet. The existing app remains a launcher shell until the native engine and updater are actually integrated and verified.
