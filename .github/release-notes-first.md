# CronosZip {{TAG}} — the first Android release

**Extract huge archives in place. The archive shrinks as your files hatch, so you only need room for the files you are unpacking — not for the files *and* the archive at the same time.**

CronosZip is a free, open-source Android archive extractor and a port of [HatchZip](https://github.com/xevrion/hatchzip) by [xevrion](https://github.com/xevrion). This is the first public Android build: a native Rust extraction engine packed into a Jetpack Compose app, with support for ZIP, 7z, RAR, tar, `.tar.gz` and split archives.

This release is intentionally a **debug APK**. It is not Play Store-signed and it does not auto-update. See [Installation](#installation) and [Known limitations](#known-limitations) before installing.

---

## Build information

| Field | Value |
|---|---|
| Release tag | `{{TAG}}` |
| Release title | {{TITLE}} |
| Source commit | [`{{SHA}}`](https://github.com/{{REPO}}/commit/{{SHA}}) |
| Build workflow run | [{{RUN_ID}}]({{RUN_URL}}) |
| Build timestamp | `{{BUILD_DATE}}` |
| APK asset | `{{ASSET}}` |
| APK size | `{{APK_SIZE}}` (`{{APK_SIZE_BYTES}}` bytes) |
| APK SHA-256 | `{{APK_SHA256}}` |
| Android requirement | Android 8.0 Oreo or newer (API 26+) |
| Target SDK | 35 (Android 15) |
| ABIs | `arm64-v8a`, `armeabi-v7a`, `x86_64` |
| Build type | Debug (`assembleDebug`), debug-signed |
| Version | `versionCode 1`, `versionName 1.0.0` |

The APK is attached directly to this release. A matching `.sha256` file is attached beside it.

---

## The problem CronosZip solves

A 180 GB archive that unpacks to 200 GB normally needs **380 GB** of storage: 180 GB for the archive plus 200 GB for the extracted files, all alive at the same time. On a phone or tablet that is already almost full, that is often impossible even though the unpacked files themselves would fit.

CronosZip extracts the archive from the end and cuts away each finished part as it goes. The space released by the archive is immediately available for the next files. For the same 180 GB → 200 GB example, CronosZip needs a little over **200 GB**, not 380 GB.

This is not a cache trick, a cloud service, or a promise to clean up afterwards. The archive file itself is truncated as entries are completed, on your own storage, under your own control.

---

## Highlights in 1.0.0

### In-place extraction that actually gives space back

- Extracts ZIP, 7z, RAR, tar, `.tar.gz`, and split archive sets in place.
- Frees the archive progressively: one file at a time for ZIP/tar/RAR, one solid block at a time for 7z, and volumes one at a time for split sets.
- Reuses the reclaimed bytes for the remaining output instead of waiting until the end.
- Keeps working on normal internal storage and adoptable SD cards, including Android shared storage.

### A space plan before anything is touched

- Reads the archive index and the destination filesystem's free space first.
- Replays the extraction to account for peak usage, not just the final total.
- Computes the keep-free safety reserve you configure (default: 2 GB).
- Refuses safely when the plan does not fit; no archive bytes are modified before the check passes.
- Reports entries, archive size, uncompressed output size, free space, required space, and free space after extraction.

### Crash-safe, resume-safe extraction

- Writes each file to a temporary name, flushes it to the filesystem, and renames it into place before cutting that part out of the archive.
- A crash, force-stop, low-memory kill, or full disk leaves either the completed file or the still-needed archive bytes — never neither.
- Saves progress in two small side files next to the archive: `.hatchzip` and `.hatchzip-tail`.
- Detects an unfinished extraction and offers to continue it from the saved journal.
- Can resume after the device runs out of space because something else filled the disk.
- Supports deliberate pause and resume from the Android UI.

### Restore instead of losing the archive

- If you decide to give up on an unfinished extraction, **Restore archive** rebuilds a normal archive containing exactly the entries that were not yet extracted.
- The rebuilt archive opens in 7-Zip, WinRAR, `unzip`, `tar`, and other standard tools.
- Files that already finished extracting stay in the output folder.
- Restore gives you an exit path instead of leaving a partially consumed archive that only CronosZip understands.

### Archive inspection before extraction

- Shows the archive format, entry count, compressed size, uncompressed size, and whether it is encrypted.
- Lists archive entries with paths, sizes, and types.
- Search through entries directly in the app.
- Run a checksum test over the archive without writing extracted files.
- Detect overlapping entries, duplicate names, unsafe paths, and other layouts that in-place extraction cannot handle safely.

### Passwords

- Supports password-protected ZIP (ZipCrypto and AES) and 7z (AES, including encrypted file lists).
- Supports password-protected RAR files through UnRAR.
- Asks for the password in the app, and distinguishes a missing password from a wrong password.
- Verifies the password on an entry before deleting archive bytes, so a typo cannot destroy the archive.
- NEVER stores the password in the release notes, logs, or crash reports.

### Split archives and volume sets

- Opens ZIP and 7z split sets such as `.001`, `.002`, `.003`, and so on.
- Opens multi-volume RAR sets.
- Deletes each volume only after everything that reaches into it has been extracted.
- Lets you start from any part of a split set; CronosZip finds the complete set.

### Android-first interface

- Jetpack Compose UI designed for one-handed phone use.
- Dark glass-style interface with a liquid animated background.
- Home screen with recent archives, safety reminders, and a clear path to start.
- Built-in storage browser for archives and destination folders.
- Extraction plan screen with a plain-language space report.
- Live extraction screen with entry name, bytes completed, total bytes, commit progress, and archive size.
- Settings screen for the keep-free reserve, keep-archive default, verify-first default, animations, and All files access.
- Graceful handling of Android 11+ scoped storage via `MANAGE_EXTERNAL_STORAGE` for shared-storage locations.

### Safety checks built into the engine

- Rejects archive entries whose paths escape the destination folder.
- Rejects entries that overlap each other.
- Rejects archives with duplicate entry names where extraction would be ambiguous.
- Rejects links that would interfere with real files or folders.
- Never restores setuid or setgid bits.
- Creates symlinks only after regular files are written, so nothing can be written through a link.
- Verifies each entry's CRC-32 while extracting.
- Stops at damaged data and leaves the damaged entry in the archive, together with everything still behind it.
- With **Keep archive** enabled, the engine refuses all code paths that shrink, truncate, punch, or delete archive bytes.
- Optional **Verify first** mode reads and checks every entry before deleting anything.

### Format support

| Format | Status in 1.0.0 |
|---|---|
| ZIP / ZIP64 | Supported. Stored, Deflate, Deflate64, bzip2, LZMA, PPMd, XZ, and Zstandard entries. Entries past the 4 GiB mark are supported. |
| Self-extracting `.exe` ZIPs | Supported. |
| 7z, solid and non-solid | Supported. LZMA2, LZMA, BCJ, bzip2, Deflate, PPMd, and Zstandard. Solid archives are freed one solid block at a time. |
| RAR 4 and RAR 5 | Supported, including volume sets and password-protected files, via the bundled UnRAR decoder. |
| Solid RAR | Supported only while keeping the archive. A solid RAR must decode front to back and cannot be resumed mid-way or freed progressively. |
| tar | Supported, including long names, symlinks, and hard links. |
| `.tar.gz` / `.tgz` / `.taz` | Supported, including multi-member gzip and split sets. Extracted from the front, freeing compressed bytes behind the decoder where the filesystem can punch holes. |
| Split `.001`, `.002`, ... sets of ZIP or 7z | Supported. |
| `.tar.zst` / `.tar.xz` | Not supported yet. Refused before extraction with a clear message. |
| Plain `.gz` / `.zst` / `.xz` / `.bz2` streams that are not tar archives | Not supported for in-place extraction. Refused with a clear message. |

---

## What changed on the way to this release

This is the first release, so the full history is included here.

### Android application

- Added the Android Jetpack Compose application and navigation flow: Home, file browser, archive details, extraction setup, live extraction, and settings.
- Added the glass-style theme, wordmark, animated liquid background, cards, chips, buttons, and status pills.
- Added recent-archive history with local persistence.
- Added settings persistence for keep-free space, keep-archive, verify-first, and animations.
- Added All files access handling for shared storage on modern Android.
- Added storage-root discovery for internal storage, SD cards, and app-accessible folders.
- Added archive filtering in the file browser, including split-volume suffixes.
- Added password prompts and wrong-password feedback.
- Added restore-archive confirmation and result reporting.
- Added live progress events for bytes, committed archive bytes, current entry, phase, and totals.
- Added cancellation/pause handling that leaves the journal resumable.
- Added friendly native error mapping for password errors, no-space, existing files, damaged archives, busy archives, and unsupported in-place layouts.
- Replaced colored card and button shadows with border/glow treatments to remove gray square artifacts on Android 8–9.
- Fixed arrow icons to use auto-mirrored variants for right-to-left layouts.

### Native Rust engine and JNI bridge

- Ported the HatchZip Rust core as `hatchzip-core` and built the `cronoszip-jni` bridge.
- Added `nativeCheck`, `nativeList`, `nativeTest`, `nativeExtract`, `nativeRestore`, and `nativeCancel`.
- Added a JSON envelope (`ok`, `code`, `message`, `data`) shared by all native calls.
- Added stable error codes: `PASSWORD_NEEDED`, `WRONG_PASSWORD`, `NO_SPACE`, `EXISTS`, `IN_PLACE_REFUSED`, `BUSY`, `DAMAGED`, and `FAILED`.
- Added Android-specific filesystem behavior, including `fallocate64` for 32-bit ABI compatibility and FUSE-aware sync behavior.
- Added an Android shared-storage workaround: FUSE-backed volumes often return `ENOSYS` for `flock`, so the advisory archive lock is skipped there instead of failing the extraction.
- Fixed a Rust edition 2021 let-chain compile failure in the JNI layer.
- Fixed the Android RAR path type to use narrow `c_char` pointers required by UnRAR's C interface.

### RAR / UnRAR

- Vendored and patched `unrar-ng-sys` for Android NDK builds.
- Added target-based build flags for the UnRAR C++ sources.
- Fixed the `UNIX_TIME_NS` path so Android builds do not depend on `lutimes` before API 26.
- Linked against `c++_shared` and packaged the NDK C++ runtime in the APK.
- Enabled RAR support in the Android build.

### Build and CI

- Added a GitHub Actions workflow that builds the Rust native libraries and the debug APK.
- Added Rust targets for `aarch64-linux-android`, `armv7-linux-androideabi`, and `x86_64-linux-android`.
- Added NDK 27.0.12077973 setup, JDK 17, Gradle, and cargo-ndk setup.
- Fixed the `cargo-ndk` platform flag (uppercase `-P`) and libc++ linking.
- Added native and Gradle build-log capture with concise failure annotations.
- Added artifact upload for `cronoszip-debug-apk`.
- Added this release workflow, which can publish the latest successful build artifact as a GitHub Release.
- Verified the release artifact ZIP extracts on Android shared storage with the in-place engine.
- Verified ZIP extraction with a real 22 MB artifact archive on `/storage/emulated/0/Download`.

---

## Installation

### Requirements

- Android 8.0 (API 26) or newer.
- One of the included ABIs: `arm64-v8a` (most modern phones), `armeabi-v7a` (older 32-bit phones), or `x86_64` (emulators and some tablets).
- Enough free space for the space plan shown by the app.
- All files access if you want to extract archives outside the app's private folder.

### Steps

1. Download `{{ASSET}}` from this release.
2. Optionally verify it:
   ```sh
   sha256sum {{ASSET}}
   ```
   The result must be:
   ```text
   {{APK_SHA256}}
   ```
3. Open the APK on the Android device. Android will ask you to allow installs from your browser or file manager; this is expected for a debug-signed APK distributed outside the Play Store.
4. Launch CronosZip.
5. Open **Settings** and grant **All files access** if you plan to read or modify archives on shared storage such as `Download` or an SD card.
6. Tap **Get started**, choose an archive, review the extraction plan, choose the destination, and confirm.

### First run advice

- Try a small ZIP first and confirm the extracted files look correct.
- Check the plan's free-space figures before starting a large archive.
- Keep the default 2 GB keep-free reserve unless you deliberately want to use more of the disk.
- Do not delete `.hatchzip` or `.hatchzip-tail` side files while an extraction is unfinished.
- If an extraction is interrupted, reopen the same archive and continue from the plan screen.
- If you want to abandon an unfinished extraction, use **Restore archive** to rebuild the remaining entries.

---

## Safety model

CronosZip deletes parts of the archive while extracting, so the engine is built around a simple rule: **never lose data that has not safely landed on disk.**

1. The space plan runs before the first byte is modified.
2. Files are written under temporary names, flushed, and renamed into place before archive bytes are released.
3. Checksums are verified as entries are decoded.
4. A damaged entry stops the run and stays in the archive with everything behind it.
5. Unsafe paths, duplicate entries, and overlapping entries are refused before deletion.
6. Keep-archive mode disables every archive-shrinking path.
7. Restore can turn the remaining bytes back into a standard archive.

What CronosZip cannot protect against: if the source download is already damaged, the entries before the damage may extract and the archive may already be partly consumed. Use **Verify first** when an archive cannot be downloaded again.

---

## Known limitations

- **Debug APK only.** This build is debug-signed. It is not suitable for Play Store distribution, and future differently signed builds may require uninstalling/reinstalling (which can erase app-private settings if Android refuses an in-place update).
- **No auto-update.** Releases must be downloaded and installed manually.
- **No release signing or reproducible release pipeline yet.** The engine and UI are built from the tagged source, but the APK is a debug artifact.
- **Android 8.0 minimum.** API 26 is required. Android 7.x and older are not supported.
- **No 32-bit x86 APK.** Only `arm64-v8a`, `armeabi-v7a`, and `x86_64` are packaged.
- **Solid RAR can only be kept.** A solid RAR cannot be freed progressively or resumed mid-way in 1.0.0; use keep-archive mode.
- **`.tar.zst` and `.tar.xz` are not supported yet.** They are refused before extraction.
- **Compressed streams that are not tar archives are not supported.** Plain `.gz`, `.zst`, `.xz`, and `.bz2` files are not archives in the sense CronosZip can unpack in place.
- **In-place extraction depends on the filesystem.** Android internal storage and adoptable SD cards generally work. Some exotic or read-only filesystems cannot shrink files; the app will report that the plan cannot run.
- **Shared-storage lock behavior differs.** FUSE shared storage may not support `flock`. CronosZip treats that as a missing advisory lock and continues; do not run two extractions on the same archive at the same time.
- **`.tar.gz` streaming depends on punch-hole support.** Where the filesystem cannot punch holes, the compressed bytes may not be reclaimed progressively, so the space advantage can shrink.
- **Very large archives take time.** Extraction speed depends on the CPU, storage, archive compression, and solid-block size. The app is designed to survive interruption, but Android may still kill long background work if the device is under severe memory pressure.
- **No archive *creation*.** CronosZip extracts archives. It is not a ZIP/7z/RAR compressor.
- **No root-only features.** Everything runs with normal Android app permissions.
- **RAR is decoder-only.** RAR support is provided by UnRAR under its own license; CronosZip is not a RAR-compatible archiver.
- **First release may have rough edges.** Please include the archive format, Android version, device model, exact error text, and whether the archive was on internal or shared storage when reporting a problem.

---

## Verification and checksums

The release contains:

- `{{ASSET}}` — the installable debug APK.
- `{{ASSET}}.sha256` — SHA-256 checksum for the APK.

Expected SHA-256:

```text
{{APK_SHA256}}
```

On Linux, Termux, or macOS:

```sh
sha256sum {{ASSET}}
```

On Windows PowerShell:

```powershell
Get-FileHash .\{{ASSET}} -Algorithm SHA256
```

Compare the output with the checksum above before installing.

---

## Project, source, and licenses

- Repository: [`{{REPO}}`](https://github.com/{{REPO}})
- Upstream engine and inspiration: [HatchZip](https://github.com/xevrion/hatchzip) by [xevrion](https://github.com/xevrion)
- CronosZip is licensed under **MIT OR Apache-2.0**, at your option.
- RAR decoding uses the UnRAR source, freeware under its own license: free for extracting RAR archives, not for building a RAR-compatible archiver.
- The Android UI is built with Jetpack Compose and Material 3.
- The extraction engine is Rust, compiled for Android with the NDK.

If CronosZip saves your storage or saves a download you could not otherwise unpack, consider reporting bugs and edge cases so the next release can be even more boring and reliable.

**Thank you for trying the first CronosZip Android release.**
