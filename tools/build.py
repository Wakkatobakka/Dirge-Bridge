#!/usr/bin/env python3
"""Public-safe offline v0.2.4 build.

Builds the bounded v0.2.4 Weapon Aim repair from a caller-supplied v0.2.3
baseline. No private game payload, signing key, signing password, or machine-
specific /mnt/data fallback is embedded in this script.
"""
import argparse
import hashlib
import json
import os
import pathlib
import shutil
import subprocess
import zipfile

ROOT = pathlib.Path(__file__).resolve().parents[1]
from dex_baseline import patch, classes

EXPECTED_BASELINE_SHA256 = "0c5c07c9f81ed1664362ec59b35677b8c736c8f799fae1384b6195626c21fa34"


def run(cmd, log):
    p = subprocess.run([str(x) for x in cmd], cwd=ROOT, capture_output=True, text=True)
    (ROOT / "verification" / log).write_text(p.stdout + p.stderr)
    if p.returncode:
        raise RuntimeError(log + "\n" + p.stdout + p.stderr)
    return p.stdout


pa = argparse.ArgumentParser()
pa.add_argument("--toolchain", type=pathlib.Path, required=True)
pa.add_argument("--baseline", type=pathlib.Path, required=True)
pa.add_argument("--keystore", type=pathlib.Path)
pa.add_argument("--ks-alias")
a = pa.parse_args()

if (a.keystore is None) != (a.ks_alias is None):
    raise SystemExit("--keystore and --ks-alias must be supplied together")

tc = a.toolchain.resolve()
bt = tc / "android-sdk-linux/build-tools/35.0.0"
api = tc / "android-sdk/platforms/android-35/android.jar"
base = a.baseline.resolve()

for needed in [api, bt / "aapt", bt / "d8", bt / "zipalign"]:
    if not needed.exists():
        raise SystemExit(f"Missing toolchain component: {needed}")
if not base.exists():
    raise SystemExit(f"Missing baseline APK: {base}")

base_hash = hashlib.sha256(base.read_bytes()).hexdigest()
if base_hash != EXPECTED_BASELINE_SHA256:
    raise SystemExit(
        "Wrong v0.2.3 baseline. Expected SHA-256 " + EXPECTED_BASELINE_SHA256 +
        ", got " + base_hash
    )

b = ROOT / "build"
(ROOT / "verification").mkdir(exist_ok=True)
(ROOT / "dist").mkdir(exist_ok=True)
for d in ["stubs", "classes", "dex", "gen"]:
    shutil.rmtree(b / d, ignore_errors=True)
    (b / d).mkdir(parents=True, exist_ok=True)

with zipfile.ZipFile(base) as z:
    expected = (ROOT / "assets/expected-payload.properties").read_bytes()
    assert expected == z.read("assets/expected-payload.properties"), "Payload allowlist changed"
    dex = z.read("classes.dex")

replacements = {
    "Lcom/wakka/dirge/core/TwinInput;",
    "Lcom/wakka/dirge/core/TwinInput$Contact;",
}
have = set(classes(dex))
assert replacements <= have, "Missing TwinInput classes in baseline"
filtered = patch(dex, replacements, ((b"0.2.3", b"0.2.4"),))
(b / "baseline-filtered.dex").write_bytes(filtered)
(ROOT / "verification/replaced-classes.txt").write_text("\n".join(sorted(replacements)) + "\n")

run(["javac", "--release", "8", "-cp", api, "-d", b / "stubs", *sorted((ROOT / "compile-only").rglob("*.java"))], "compile-stubs.txt")
run(["jar", "cf", b / "compile-only.jar", "-C", b / "stubs", "."], "jar-stubs.txt")
run(["javac", "--release", "8", "-cp", str(api) + os.pathsep + str(b / "compile-only.jar"), "-d", b / "classes", ROOT / "src/main/java/com/wakka/dirge/core/TwinInput.java"], "javac.txt")

jp = run(["javap", "-classpath", b / "classes", "-c", "-p", "com.wakka.dirge.core.TwinInput"], "twininput-javap.txt")
assert "CameraAssist.PAD_LEFT" in jp and "CameraAssist.PAD_RIGHT" in jp and "getstatic" in jp, "PAD constants were inlined instead of read from runtime CameraAssist"

run(["jar", "cf", b / "changes.jar", "-C", b / "classes", "."], "jar-changes.txt")
run([bt / "d8", "--min-api", "28", "--lib", api, "--output", b / "dex", b / "baseline-filtered.dex", b / "changes.jar"], "d8.txt")
assert len(list((b / "dex").glob("*.dex"))) == 1, "Unexpected multidex"
run([bt / "aapt", "package", "-f", "-M", ROOT / "AndroidManifest.xml", "-S", ROOT / "res", "-A", ROOT / "assets", "-I", api, "-F", b / "base.apk", "-J", b / "gen"], "aapt.txt")
shutil.copy2(b / "base.apk", b / "unsigned.apk")
with zipfile.ZipFile(b / "unsigned.apk", "a", zipfile.ZIP_DEFLATED) as z:
    z.write(b / "dex/classes.dex", "classes.dex")
run([bt / "zipalign", "-f", "4", b / "unsigned.apk", b / "aligned.apk"], "align-build.txt")

out = ROOT / "dist/Dirge_Bridge_v0.2.4-unsigned.apk"
shutil.copy2(b / "aligned.apk", out)
signed = False

if a.keystore:
    apksigner = bt / "apksigner"
    if not apksigner.exists():
        raise SystemExit(f"Missing toolchain component: {apksigner}")
    ks_pass = os.environ.get("DIRGE_KS_PASS")
    key_pass = os.environ.get("DIRGE_KEY_PASS")
    if not ks_pass or not key_pass:
        raise SystemExit("Set DIRGE_KS_PASS and DIRGE_KEY_PASS in your local environment")
    out = ROOT / "dist/Dirge_Bridge_v0.2.4.apk"
    run([
        apksigner, "sign", "--ks", a.keystore, "--ks-key-alias", a.ks_alias,
        "--ks-pass", "env:DIRGE_KS_PASS", "--key-pass", "env:DIRGE_KEY_PASS",
        "--v4-signing-enabled", "false", "--out", out, b / "aligned.apk"
    ], "sign-build.txt")
    run([apksigner, "verify", "--verbose", "--print-certs", out], "apk-signature.txt")
    signed = True

result = {
    "apk": str(out),
    "size": out.stat().st_size,
    "sha256": hashlib.sha256(out.read_bytes()).hexdigest(),
    "baseline_sha256": base_hash,
    "signed": signed,
    "same_payload_allowlist": True,
    "version": "0.2.4",
    "scope": "bounded Weapon Aim bit-bridge repair; public-safe build wrapper",
}
(ROOT / "verification/build-result-local.json").write_text(json.dumps(result, indent=2) + "\n")
print(json.dumps(result, indent=2))
