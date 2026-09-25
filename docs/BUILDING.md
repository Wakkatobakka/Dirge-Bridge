# Building Dirge Bridge v0.2.4

The public-safe build script deliberately does **not** contain a signing key, signing password, private game files, or hard-coded local machine paths.

## Required local inputs

1. Android SDK/toolchain containing Android 35 and Build Tools 35.0.0.
2. A clean v0.2.3 Dirge Bridge APK baseline.
   - Expected SHA-256: `0c5c07c9f81ed1664362ec59b35677b8c736c8f799fae1384b6195626c21fa34`
3. If you want an installable signed APK, provide your own Android signing keystore and passwords locally.

No Lost Episode game payload is required to compile the bridge APK.

## Example

```text
python tools/build.py --toolchain <toolchain-folder> --baseline <Dirge_Bridge_v0.2.3.apk>
```

That produces an aligned **unsigned** APK in `dist/`.

To sign during the same build, also provide:

```text
--keystore <your-keystore.jks> --ks-alias <alias>
```

Set the passwords in the local environment variables:

```text
DIRGE_KS_PASS
DIRGE_KEY_PASS
```

Never commit a keystore or password to the repository.
