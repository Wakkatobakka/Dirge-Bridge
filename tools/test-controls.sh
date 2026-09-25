#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p build/test-controls verification
rm -rf build/test-controls/*
javac --release 8 -d build/test-controls compile-only/com/nttdocomo/ui/Image.java compile-only/com/wakka/dirge/core/{Host,Diag}.java src/main/java/com/wakka/dirge/core/{TwinLayout,TwinInput,CameraAssist}.java tests/{TestTwinControls,TestCameraAssist}.java
java -cp build/test-controls TestTwinControls | tee verification/twin-controls-tests.txt
java -cp build/test-controls TestCameraAssist | tee verification/camera-assist-unit-tests.txt
