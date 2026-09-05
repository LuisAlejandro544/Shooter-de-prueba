#!/bin/bash
set -e

echo "Compilando Rust para arquitecturas Android..."
export PATH="$HOME/.cargo/bin:$PATH"

cargo build --manifest-path app/src/main/rust/polystrike_core/Cargo.toml --target aarch64-linux-android --target x86_64-linux-android

mkdir -p app/src/main/jniLibs/arm64-v8a app/src/main/jniLibs/x86_64
cp app/src/main/rust/polystrike_core/target/aarch64-linux-android/debug/libpolystrike_core.so app/src/main/jniLibs/arm64-v8a/
cp app/src/main/rust/polystrike_core/target/x86_64-linux-android/debug/libpolystrike_core.so app/src/main/jniLibs/x86_64/

echo "Rust libs compiladas e integradas correctamente en jniLibs."
