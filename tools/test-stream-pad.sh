#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p build/test-pad verification
rm -rf build/test-pad/*
javac --release 8 -d build/test-pad src/main/java/com/nttdocomo/ui/PcmStreamPad.java tests/TestPcmStreamPad.java
java -cp build/test-pad com.nttdocomo.ui.TestPcmStreamPad | tee verification/short-stream-pad-tests.txt
