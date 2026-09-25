#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
MLD="${1:?titlemusic.mld path required}"
mkdir -p build/test-audio verification
rm -rf build/test-audio/*
javac --release 8 -d build/test-audio src/main/java/com/nttdocomo/ui/{G72616,MldDecoder,PcmResampler}.java tests/{TestMldDecoder,TestPcmResampler}.java
java -cp build/test-audio com.nttdocomo.ui.TestMldDecoder "$MLD" | tee verification/mld-g726-tests.txt
java -cp build/test-audio TestPcmResampler | tee verification/pcm-resampler-tests.txt
