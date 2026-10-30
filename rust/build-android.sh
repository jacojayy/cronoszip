#!/usr/bin/env sh
# Build libcronoszip_jni.so for Android and put it where Gradle packs it.
#
# Needs: rustup, the Android NDK, cargo-ndk, and the four Rust targets:
#   rustup target add aarch64-linux-android armv7-linux-androideabi x86_64-linux-android
#   cargo install cargo-ndk
#
# ANDROID_NDK_HOME (or ANDROID_NDK_ROOT) must point at the NDK. The app ships
# arm64-v8a, armeabi-v7a and x86_64; add i686-linux-android for 32-bit x86.
set -eu

here=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
out="$here/../app/src/main/jniLibs"

cd "$here"
cargo ndk \
  -t arm64-v8a \
  -t armeabi-v7a \
  -t x86_64 \
  -p 26 \
  -o "$out" \
  build -p cronoszip-jni --release

# unrar is C++ and links against the NDK's shared libc++, so it must be
# packaged next to libcronoszip_jni.so or System.loadLibrary fails at runtime.
ndk="${ANDROID_NDK_HOME:-${ANDROID_NDK_ROOT:-}}"
for abi in arm64-v8a armeabi-v7a x86_64; do
  case "$abi" in
    arm64-v8a) triples="aarch64-linux-android" ;;
    armeabi-v7a) triples="arm-linux-androideabi armv7a-linux-androideabi" ;;
    x86_64) triples="x86_64-linux-android" ;;
  esac
  for triple in $triples; do
    lib=$(find "$ndk/toolchains/llvm/prebuilt" -path "*/sysroot/usr/lib/$triple/libc++_shared.so" -print -quit 2>/dev/null || true)
    if [ -n "$lib" ]; then
      cp "$lib" "$out/$abi/libc++_shared.so"
      break
    fi
  done
done

echo "Native libraries written to $out"
