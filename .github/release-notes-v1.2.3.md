# CronosZip {{VERSION_NAME}} — everything new since 1.0.0

CronosZip is still the same idea: **extract huge archives in place, and let the archive shrink as the files come out**, so you never need room for the archive and the extracted files at the same time. This release keeps that engine and adds the parts that make it usable as a daily Android app: a real file manager, Android document-provider support, and an updater that installs over the old version instead of making you hunt for the APK every time.

---

## ⚠️ One-time reinstall if you are on 1.0.0

If you installed **CronosZip 1.0.0**, you must **uninstall it once** before installing this build.

1.0.0 was a debug APK signed by a throwaway CI key. That key no longer exists, so Android sees this release as a different app and refuses to update it in place. Uninstalling removes the old app and its settings/history, but **your extracted files are not touched** — they live in the folders you chose.

After this one-time reinstall, every later CronosZip update installs directly over the previous one. You will not have to uninstall again.

If you are already on **1.2.x**, install this over the top normally.

---

## Build information

| Field | Value |
|---|---|
| Release tag | `{{TAG}}` |
| Release title | {{TITLE}} |
| App version | `{{VERSION_NAME}}` |
| Version code | `{{VERSION_CODE}}` |
| Source commit | [`{{SHA}}`](https://github.com/{{REPO}}/commit/{{SHA}}) |
| Build workflow run | [{{RUN_ID}}]({{RUN_URL}}) |
| Build timestamp | `{{BUILD_DATE}}` |
| APK asset | `{{ASSET}}` |
| APK size | `{{APK_SIZE}}` (`{{APK_SIZE_BYTES}}` bytes) |
| APK SHA-256 | `{{APK_SHA256}}` |
| Android requirement | Android 8.0 Oreo or newer (API 26+) |
| Target SDK | 35 (Android 15) |
| ABIs | `arm64-v8a`, `armeabi-v7a`, `x86_64` |
| Build type | Release (`assembleRelease`), signed with the CronosZip release keystore |

The APK, its `.sha256` file, and `version.txt` are attached to this release.

---

# What's new since 1.0.0

## 1. An updater that actually updates in place

1.0.0 had no updater at all. This release adds a complete update path:

- **Checks for updates every time the app opens.** No manual “check” step is required, though the Updater screen still has a **Check latest** button.
- Reads the latest GitHub release and picks the APK, `.sha256`, and `version.txt` assets.
- Compares the release's `version.txt` **versionCode** against the installed `longVersionCode`, so a newer build is detected even when the tag name is ambiguous. If `version.txt` is missing, it falls back to semantic version comparison.
- Downloads the APK with progress, then **verifies the SHA-256** against the release's `.sha256` asset. A mismatched download is deleted and never installed.
- **Verifies the signing certificate** of the downloaded APK against the installed app before launching the installer. If the keys differ, it stops and explains that a one-time uninstall is required instead of letting Android throw a confusing `INSTALL_FAILED_UPDATE_INCOMPATIBLE`.
- Launches the system installer through a `FileProvider` URI with `REQUEST_INSTALL_PACKAGES`, so no “exposed file URI” crash on modern Android.
- Shows the current version, latest version, release date, asset size, and SHA-256 preview on the Updater screen.
- Every release now ships `version.txt` alongside the APK specifically so the updater can compare numeric version codes without guessing from tag names.

### Why updates install over the top now

- The project now has a committed release keystore (`app/cronoszip-release.keystore`) used by both debug and release builds.
- `versionCode` is generated from the current time in minutes, so every build is monotonic and higher than the previous one.
- `versionName` is set per release (currently `{{VERSION_NAME}}`).
- The CI workflow builds `assembleRelease` and uploads `cronoszip-release-apk` plus `output-metadata.json`.
- The release workflow publishes the APK, `.sha256`, and `version.txt` and refuses to create a tag that already exists.

---

## 2. One real file manager instead of a bare archive picker

1.0.0 had a storage browser that existed mainly to choose an archive or destination. It is now a proper file manager that happens to also open archives.

### Views

- **List view** for normal phone use.
- **Split view** with two independent panes, each with its own path, storage chips, and navigation. Useful for moving files between two folders without bouncing back and forth.
- Switch between list and split from the same screen; there is no longer a separate “dual pane” screen.

### File operations

- **Copy**
- **Cut / move**
- **Paste**
- **Rename**
- **Delete** (with a confirmation dialog)
- **New folder**
- Folder-first sorting and human-readable sizes/timestamps.
- The clipboard is shared between list and split panes, so you can copy in one pane and paste in the other.

### Storage roots

- Built-in shortcuts for **Internal storage**, **Downloads**, **Documents**, **Movies**, **Music**, **Pictures**, external app storage, internal app storage, and app cache.
- **Add app storage** pins any folder you choose.
- Pinned locations persist across app restarts.
- Unpinning a document-provider folder releases the Android persistable URI grant instead of leaving it behind.

---

## 3. Android document-picker (SAF) support — the big one

This is the part that was missing from earlier builds. CronosZip no longer pretends every location is a normal `File` path. It now speaks Android's Storage Access Framework directly.

### Adding storage

The **Add app storage** dialog now:

- Lists installed apps that expose storage through Android's document picker (DocumentsProviders), with their labels and package names.
- Opens the **real system folder picker** (`ACTION_OPEN_DOCUMENT_TREE`) seeded at that provider's root, so you only confirm the folder once.
- Takes a **persistable URI permission**, so the folder keeps working after a reboot or app restart.
- Also offers **Choose a folder…** for any provider and **Add CronosZip app storage** for the app's own private folder.
- Shows every pinned location with a remove button.

### Browsing document-only locations

Folders that exist only behind the document picker now behave like normal folders:

- List children through `DocumentsContract` with names, MIME types, sizes, and modified times.
- Navigate into subfolders by document ID, not by guessing a filesystem path.
- Navigate **up** correctly, including from a provider's volume root.
- Open non-archive files with the system `ACTION_VIEW` handler using a temporary read grant.
- Open archives directly from a document provider.
- Create folders, rename items, and delete items through the provider's own document API.
- Copy and move files **between document providers**, and **between document providers and normal local folders**.
- Archive opening uses the real filesystem path when the provider maps to device storage (for example an external-storage tree). When the provider has no filesystem path, the archive is copied into the app cache so the native extraction engine can read it.

### Why this matters

On Android 11+, many folders — especially another app's storage, SD-card document trees, and provider-backed locations — cannot be reached by typing a path. The old “type a path” flow silently failed there. The new flow asks Android for access the supported way and then browses the granted tree.

---

## 4. Home screen and settings

- The home screen now covers **both** sides of the app: extraction and file management.
- **Open files** is available from home, not buried in settings.
- Recent archives still show their size, entry count, and status.
- Settings has a single **Files** row that opens the unified file manager.
- Settings has an **Updater** row with the current version and version code.
- Settings still exposes the keep-free reserve, keep-archive default, verify-first default, animations, and All files access.

---

## 5. Build, release, and reliability fixes

- CI now builds a **release APK** instead of a debug APK.
- The build artifact includes `output-metadata.json`, which the release workflow uses to read the real version code.
- The release workflow publishes the APK, `.sha256`, and `version.txt` in one run.
- Release notes can come from `.github/release-notes-<tag>.md`, a manual `notes` input, or GitHub-generated notes.
- Fixed invalid Android backup-rule domains (`cache`) that were failing release lint.
- Fixed missing Compose imports and a stale `appVersionName` reference that broke earlier builds.
- Fixed the updater to run on launch rather than only when its screen is opened.
- Removed the old separate dual-pane screen and its duplicate navigation state.

---

## Upgrade notes

### From 1.0.0

1. Uninstall CronosZip 1.0.0 once.
2. Install `{{ASSET}}`.
3. Your extracted files are untouched. App settings and history start fresh.
4. Future updates install in place.

### From 1.2.x

Install `{{ASSET}}` over the top. The updater can also do it for you.

---

## Installation and verification

1. Download `{{ASSET}}`.
2. Optionally verify it:
   ```sh
   sha256sum {{ASSET}}
   ```
   The result must be:
   ```text
   {{APK_SHA256}}
   ```
3. Open the APK and allow installs from your browser or file manager if Android asks.
4. Launch CronosZip.
5. Grant **All files access** if you want to read or write archives on shared storage.
6. For provider-only folders, open **Files → Add app storage**, pick the storage app or **Choose a folder…**, and confirm the folder once.

---

## Known limitations

- **Document providers do not all behave the same.** Some providers do not support creating, renaming, deleting, or copying items. CronosZip reports the provider's error instead of pretending the operation succeeded.
- **Copying folders between document providers is not supported yet.** Copying individual files works. Copying a folder from local storage into a document provider is also not supported yet.
- **Archives from providers without a filesystem path are copied to the app cache first.** This is required because the native extraction engine works on real paths. Very large archives may need significant free space for that copy.
- Solid RAR archives still must be kept during extraction (the 1.0.0 engine limitation is unchanged).
- `.tar.zst`, `.tar.xz`, and plain non-tar compressed streams are still refused with a clear message.
- In-place extraction still depends on the destination filesystem supporting truncation/punch-hole behavior; the app refuses layouts it cannot do safely.

---

## Thanks

CronosZip is a port of [HatchZip](https://github.com/xevrion/hatchzip) by [xevrion](https://github.com/xevrion). The in-place extraction engine, safety model, and format support come from that project. The Android app, file manager, document-provider layer, and updater are built on top of it.
