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
  --platform 26 \
  --link-libcxx-shared \
  -o "$out" \
  build -p cronoszip-jni --release

echo "Native libraries written to $out"
