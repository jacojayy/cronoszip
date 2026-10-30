<h1 align="center">CronosZip</h1>

<p align="center">
  Extract huge archives in place, on Android. The archive shrinks as your files hatch,<br>
  so you only need free space for the output, not for the output <em>and</em> the archive.
</p>

<h4 align="center">
  <a href="#installation">Installation</a> |
  <a href="#usage">Usage</a> |
  <a href="#is-cronoszip-safe">Safety</a> |
  <a href="#how-it-works">How it works</a>
</h4>

<p align="center">
  <img src="https://img.shields.io/badge/platform-android-blue.svg" alt="Android" />
  <a href="#license"><img src="https://img.shields.io/badge/license-MIT%20OR%20Apache--2.0-blue.svg" alt="MIT or Apache-2.0 licensed" /></a>
</p>

CronosZip is a free, open-source archive extractor for Android, a port of [HatchZip](https://github.com/xevrion/hatchzip). It extracts ZIP, 7z, RAR, tar and split archives in place, for when there is not enough storage to extract an archive next to itself, such as a large game download on a nearly full device.

A 180 GB game download that unpacks to 200 GB needs 380 GB of storage with WinRAR, 7-Zip or `unzip`, because the archive and everything extracted from it have to exist at the same time. CronosZip needs a little over 200 GB. It extracts the archive from the end and cuts each finished part off the archive file as it goes, so the space the archive gives back is reused for the next files.

It checks that the extraction will fit before it changes anything, keeps a safety margin of free space the whole way through, and can be stopped or crash at any moment and pick up where it left off. It reads ZIP, 7z, tar, `.tar.gz`, RAR volume sets and split archives.

## When there is not enough storage to extract

The same 200 MB of game data (21 files), packed by the real tools, extracted on a 220 MiB volume with about 50 MB free:

| Archive | `unzip` / 7-Zip | CronosZip |
|---|---|---|
| ZIP, 180 MB | `unzip` ran out of space at file 6 of 21 | Finished, every file byte-identical |
| 7z from 7-Zip 25.01, `-ms=20m` (10 solid blocks) | Needs 380 MB | Finished, every file byte-identical |
| 7z split into 5 volumes of 40 MB | Needs 380 MB | Finished; volumes deleted one at a time |
| tar from GNU tar, 200 MB | Needs 400 MB | Finished, every file byte-identical |

| | Result |
|---|---|
| Works on | Any Android filesystem that can shrink a file, including internal storage and adoptable SD cards |

The extra space CronosZip needs beyond the output is about the size of the largest single unit it can free at once: one file for ZIP and tar, one solid block for 7z. `cronoszip check` computes the exact figure for any archive.

## Installation

### Build from source

With [Rust](https://rustup.rs) under [Termux](https://termux.dev) or an Android NDK toolchain:

```sh
git clone https://github.com/jacojayy/cronoszip.git
cd cronoszip
cargo build --release
```

The binary is `target/release/cronoszip`. Building with `--features rar` adds RAR support, which compiles UnRAR's C++ source under its own [license](#license) and needs a C++ toolchain.

### With Cargo

```sh
cargo install cronoszip
```

The engine is a library too: `cronoszip-core`. It reads every format but RAR; `cargo install cronoszip --features rar` adds RAR support.

## Usage

### See whether it fits

```sh
cronoszip check game.zip
```

```
game.zip  180.2 MB zip, 200.0 MB uncompressed in 22 entries
  available                    50.5 MB
  standard extraction needs    200.0 MB
  required                     29.9 MB (+5.0 MB reserved)
  available after              30.6 MB
```

`check` reads the archive's index and the storage's free space and changes nothing. "Standard extraction needs" is what extracting while keeping the archive requires; when that fits too, `check` says so. `cronoszip check game.zip --keep-archive` shows the plan for keeping it.

### Extract

```sh
cronoszip extract game.zip                 # into ./game/
cronoszip extract game.zip -o /sdcard/Games/Foo # somewhere else
cronoszip x game.7z.001                    # split archive; any part's name works
```

CronosZip prints the same space plan, asks for confirmation, then extracts. When the extraction is complete, the archive has been removed and the files are in the output folder. For a split archive, each volume is deleted as soon as everything in it is extracted.

| Option | Effect |
|---|---|
| `-o, --output <dir>` | Output folder. Defaults to a folder named after the archive, next to it. |
| `--keep-archive` | Keep the archive: a normal extraction that removes nothing. Space is required for all extracted files. |
| `--replace` | Replace files that already exist in the output folder. |
| `--skip-existing` | Keep files that already exist in the output folder instead of the archive's copies. |
| `--keep-free <size>` | Free space CronosZip never uses, such as `500MB` or `10GB`. Default `2GB`, so storage never fills to zero. |
| `--verify-first` | Read the whole archive and check every file's checksum before deleting anything. |
| `-y, --yes` | Skip the confirmation prompt. |
| `-q, --quiet` | Print only errors and prompts. |

### Keep the archive

```sh
cronoszip extract game.zip --keep-archive
```

When the storage has room for the archive and all its files, `--keep-archive` extracts like any other tool and leaves the archive byte for byte as it was. The space check, path and collision checks, checksums and password handling are the same as for an extraction in place.

### Stop and resume

Interrupt the process and CronosZip finishes the files it is writing, saves its progress and exits. To continue, run the same command again:

```sh
cronoszip extract game.zip
```

The same applies after a crash or running out of space because something else filled the storage. Progress lives in two small files next to the archive, `game.zip.cronoszip` and `game.zip.cronoszip-tail`. **Do not delete them while an extraction is unfinished**: they hold the archive's index, which is no longer inside the archive itself.

### Stop for good

To give up on an unfinished extraction, restore the archive:

```sh
cronoszip restore game.zip
```

What is left of the archive becomes a normal archive again, holding exactly the entries not yet extracted, which 7-Zip, WinRAR, `unzip` or `tar` open as usual. The files already extracted stay in the output folder.

### List and test

```sh
cronoszip list game.7z    # what's inside, with sizes
cronoszip test game.7z    # read every file and verify its checksum, writing nothing
```

### Exit status

| Code | Meaning |
|---|---|
| 0 | Success |
| 1 | Failure. If extraction had started, progress is saved. |
| 2 | Invalid command line |
| 3 | Not enough free space. Nothing was changed. |
| 4 | The archive is damaged. Nothing was deleted. |
| 5 | The archive is password protected and no correct password was given. |
| 130 | Paused. Run the same command to continue. |

## Is CronosZip safe

CronosZip destroys the archive as it extracts, so it is built around never losing a file that has not safely landed on disk.

- **Nothing changes until the plan fits.** The space check runs before the first byte is touched, and it replays the whole extraction to find the moment that needs the most free space, not just the total.
- **A file is written, flushed to the physical disk and renamed into place before the part of the archive holding it is cut off.** A crash at any point leaves either the file or its bytes in the archive, never neither.
- **Damage stops the run where it is.** Every file's CRC-32 is checked as it is extracted. A damaged file is not written, and it stays in the archive along with everything before it.
- **Hostile archives are refused before anything is deleted:** paths that escape the output folder, entries that overlap each other, entries that appear twice, and links where other entries need a folder. Setuid and setgid bits are never restored, and symlinks are created only after every regular file is written, so nothing can be written through one.
- **A kept archive stays as it was.** With `--keep-archive`, the code that shrinks, frees or deletes archive bytes refuses to run for the whole extraction.

What it cannot protect against: if the download itself is damaged, the files before the damaged spot are extracted and the archive is partly gone. Use `--verify-first` when the archive can't be downloaded again.

## Supported formats

| Format | Status | Freed in steps of |
|---|---|---|
| ZIP, ZIP64, self-extracting `.exe` ZIPs | Supported. Stored, Deflate, Deflate64, bzip2, LZMA, PPMd, XZ and Zstandard, including entries past the 4 GiB mark. | One file |
| 7z, solid and non-solid | Supported. LZMA2, LZMA, BCJ, bzip2, Deflate, PPMd and Zstandard. | One solid block |
| tar (uncompressed) | Supported, including long names, symlinks and hard links | One file |
| `.tar.gz`, `.tgz` | Supported, including multi-member gzip and split sets. Extracted from the front, freeing the compressed bytes behind the decoder where the filesystem can punch holes. | 256 MiB of output |
| Split archives (`.001`, `.002`, ...) of ZIP or 7z | Supported | One file or block; each volume is deleted once emptied |
| Password-protected ZIP (ZipCrypto and AES) and 7z (AES, including encrypted file lists) | Supported. CronosZip asks for the password, or takes `--password` or `CRONOSZIP_PASSWORD`, and proves it on one file before anything is deleted. | Same as unencrypted |
| RAR, RAR 5 and RAR 4, single files and volume sets | Supported with the `rar` feature, decoded by UnRAR (see [License](#license)). Password-protected files work as for ZIP. | One file; a volume once every file that reaches into it is out |
| Solid RAR | Extracted only while the archive is kept (`--keep-archive`): a solid archive decodes front to back in one pass and cannot be freed as it goes or resumed mid-way. | |
| `.tar.zst`, `.tar.xz` | Refused before extraction, planned. | |

## How it works

ZIP, 7z and tar all store their contents as a sequence of independent pieces: files for ZIP and tar, solid blocks for 7z. ZIP and 7z keep their index at the very end. CronosZip saves that index to a side file, then extracts pieces starting from the last one in the archive. After each batch it truncates the archive to where that batch began, which every filesystem supports; for a split archive, that means deleting whole volumes and shortening one. RAR stores files one after another like tar, so it is freed file by file too. A `.tar.gz` is one compressed stream that can only be decoded from the front, so CronosZip extracts it front to back and punches holes in the compressed bytes it has used, at resume points it can restart from after a crash.

## About this port

CronosZip is a port of [HatchZip](https://github.com/xevrion/hatchzip) by [xevrion](https://github.com/xevrion) to Android. HatchZip is a free, open-source archive extractor for Windows, macOS and Linux; the core engine is the same. Full documentation for the upstream project is at [hatchzip.dev](https://hatchzip.dev).

## License

CronosZip is licensed under either of [Apache License, Version 2.0](LICENSE-APACHE) or [MIT license](LICENSE-MIT), at your option, the same dual license as HatchZip.

RAR archives are decoded by the UnRAR source code, which is freeware under its own [license](LICENSE-UNRAR): free to use for extracting RAR archives, but not to build a RAR compatible archiver. It is compiled in only with the `rar` cargo feature, which is off by default.
