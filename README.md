<p>
<a href="README.md">English README.md</a>&nbsp;&nbsp;&nbsp;
<a href="README_zh.md">中文 README_zh.md</a>
</p>

# TraceFix

TraceFix instruments Android app bytecode at compile time and injects paired `android.os.Trace.beginSection/endSection` calls around methods. These sections show up in Perfetto or other platform trace viewers; TraceFix only adds app-side trace sections and does not replace capture tooling such as Perfetto, `atrace`, or `ProfilingManager`.

## Android 17 Baseline

TraceFix `0.2.x` targets the Android 17 / API 37 code base:

| Component | Version |
| --- | --- |
| Artifact | `io.github.gracker:TraceFix:0.2.0` |
| Android Gradle Plugin | `9.1.1` |
| Gradle | `9.3.1` |
| Compile SDK / Target SDK | `37` |
| Java | `17` |

The `0.1.x` line was the legacy AGP 7.4/8.3 compatibility line. Use `0.2.x` for Android 17 / AGP 9.1.x projects, and keep `0.1.x` only for older builds that still need the previous compatibility matrix.

## Choose This First

| Your Goal | Choose | Use This |
| --- | --- | --- |
| Integrate TraceFix into your app | Remote plugin | "Remote Plugin" |
| Develop or debug plugin in this repo | Local plugin (`mavenLocal`) | "Local Plugin" |
| Verify Android 17 behavior | Trace regression script | "Compatibility Verification" |

## Remote Plugin

Maven Central artifact:

```groovy
classpath("io.github.gracker:TraceFix:0.2.0")
```

Example module or root `build.gradle`:

```groovy
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("io.github.gracker:TraceFix:0.2.0")
    }
}

apply plugin: 'com.android.application'
apply plugin: 'auto-add-systrace'
```

Optional configuration:

```groovy
traceFix {
    enabled = true

    // Empty include lists mean "all"; excludes win over includes.
    includedVariants = []
    excludedVariants = []
    includedClassPrefixes = []
    excludedClassPrefixes = [
        'com.example.generated.'
    ]

    traceConstructors = true
    traceClassInitializers = true
    traceSyntheticMethods = false
    traceBridgeMethods = false
}
```

TraceFix skips abstract and native methods because they have no instrumentable method body. Synthetic and bridge methods are skipped by default to avoid adding noisy sections for compiler-generated methods; enable them only when you intentionally want compiler-generated code in traces.

Section names are readable method names with a stable hash suffix, capped to Android's 127 UTF-16 code-unit trace section limit.

## Local Plugin

Publish the plugin to local Maven:

```bash
./gradlew :android-systrace-plugin:publishToMavenLocal
```

Build local demo modules with:

```bash
./gradlew -PTRACEFIX_ENABLE_DEMOS=true \
  :android-systrace-sample-high-local-debug:assembleDebug \
  :android-systrace-sample-high-local-debug:assembleRelease
```

Demo modules are not included by default, so the plugin can build without pulling sample app configuration into normal development.

## Compatibility Verification

Run the Android 17 trace regression:

```bash
./scripts/verify-compatibility.sh
```

The script verifies:

1. Local Java fixture on AGP 9.1.1, debug and minified release.
2. Remote Kotlin fixture through a file-backed Maven repository, debug and minified release.
3. Method-level bytecode instrumentation via `javap`, including normal returns and exception paths.
4. Edge cases: overloads, synchronized methods, long method names, interface default methods, concrete methods in abstract classes, skipped abstract/native methods, and skipped Kotlin synthetic `$default` methods.

Remote demo modules can be built manually with:

```bash
./gradlew -PTRACEFIX_ENABLE_REMOTE_DEMOS=true \
  -PTRACEFIX_VERSION_REMOTE=0.2.0-local \
  -PTRACEFIX_REMOTE_REPO_URL=file://$PWD/build/tracefix-remote-repo \
  :android-systrace-sample-remote-debug:assembleDebug \
  :android-systrace-sample-remote-debug:assembleRelease
```

## Publish To Maven Central

1. Configure Central Portal token credentials:

```bash
export OSSRH_USERNAME=... # token username from https://central.sonatype.com/usertoken
export OSSRH_PASSWORD=... # token password from https://central.sonatype.com/usertoken
```

Or use root `local.properties`:

```properties
ossrhUsername=...
ossrhPassword=...
```

2. Configure signing:

```bash
export SIGNING_KEY='-----BEGIN PGP PRIVATE KEY BLOCK-----...'
export SIGNING_PASSWORD=...
export SIGNING_KEY_ID=... # optional
```

Legacy Gradle signing properties and system GPG are also supported:

```properties
signing.keyId=...
signing.password=...
signing.secretKeyRingFile=/path/to/secring.gpg

# or
useGpgCmd=true
```

3. Verify publish configuration:

```bash
./gradlew :android-systrace-plugin:verifyReleasePublishConfig
```

4. Publish release artifact:

```bash
./gradlew :android-systrace-plugin:publishReleaseToCentral
```

Compatibility alias:

```bash
./gradlew :android-systrace-plugin:publishReleaseToSonatype
```

After publishing, verify Maven Central:

```bash
curl -I https://repo1.maven.org/maven2/io/github/gracker/TraceFix/0.2.0/TraceFix-0.2.0.pom
```
