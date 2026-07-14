<p>
<a href="README.md">English README.md</a>&nbsp;&nbsp;&nbsp;
<a href="README_zh.md">中文 README_zh.md</a>
</p>

# TraceFix

TraceFix instruments Android app bytecode at compile time and injects paired `android.os.Trace.beginSection/endSection` calls around methods. These sections show up in Perfetto or other platform trace viewers; TraceFix only adds app-side trace sections and does not replace capture tooling such as Perfetto, `atrace`, or `ProfilingManager`.

<!-- android-performance-ecosystem:start -->
## Android performance ecosystem

This repository is one part of the [Android Performance Ecosystem](https://github.com/Gracker/android-performance-ecosystem): an optional path from instrumentation and capture to analysis, system knowledge, and reproducible cases.

| Stage | Project | Purpose | Address |
| --- | --- | --- | --- |
| Instrument | [TraceFix](https://github.com/Gracker/TraceFix) | Inject app-side android.os.Trace sections at build time so method work is visible at runtime. | [GitHub](https://github.com/Gracker/TraceFix) |
| Capture and measure | [Perfetto Tools](https://github.com/Gracker/perfetto-tools) | Capture repeatable Perfetto traces and collect FPS or Simpleperf measurements. | [GitHub](https://github.com/Gracker/perfetto-tools) |
| Analyze | [SmartPerfetto](https://github.com/Gracker/SmartPerfetto) | Investigate traces with an AI-assisted Web UI, CLI, reports, sessions, comparisons, and evidence workflow. | [GitHub](https://github.com/Gracker/SmartPerfetto) |
| Agent analysis | [Perfetto Skills](https://github.com/Gracker/Perfetto-Skills) | Give agents a portable Perfetto analysis Skill for Android, Linux, and Chromium, with selected assets synchronized through pinned workflows. | [GitHub](https://github.com/Gracker/Perfetto-Skills) |
| Learn | [Android Performance Blog](https://github.com/Gracker/Gracker.github.io) | Teach Perfetto and Systrace analysis through articles, system explanations, and case studies. | [AndroidPerformance.com](https://www.androidperformance.com/) · [GitHub](https://github.com/Gracker/Gracker.github.io) |
| System knowledge | Android Internal Wiki | An alpha knowledge base for Android mechanisms from App to Framework, Native, and Kernel. | **Coming soon** |
| Reproduce | [Trace for Blog (SystraceForBlog)](https://github.com/Gracker/SystraceForBlog) | Provide the Perfetto, Systrace, and related case files used by articles for hands-on reproduction. | [GitHub](https://github.com/Gracker/SystraceForBlog) |
<!-- android-performance-ecosystem:end -->

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

Maintainers can publish from GitHub Actions by opening the `Publish TraceFix` workflow and running it on `master`. Configure these repository secrets first:

| Secret | Required | Notes |
| --- | --- | --- |
| `OSSRH_USERNAME` | Yes | Central Portal token username. `MAVEN_CENTRAL_USERNAME` or `SONATYPE_USERNAME` also work. |
| `OSSRH_PASSWORD` | Yes | Central Portal token password. `MAVEN_CENTRAL_PASSWORD` or `SONATYPE_PASSWORD` also work. |
| `SIGNING_KEY` | Yes | ASCII-armored PGP private key for in-memory signing. |
| `SIGNING_PASSWORD` | Yes | Signing key password. `SIGNING_PASSPHRASE` also works. |
| `SIGNING_KEY_ID` | Optional | Required only when the in-memory key needs an explicit key id. |

Optional repository variables:

| Variable | Default |
| --- | --- |
| `TRACEFIX_CENTRAL_NAMESPACE` | `io.github.gracker` |
| `TRACEFIX_CENTRAL_PUBLISHING_TYPE` | `automatic` |
| `TRACEFIX_CENTRAL_SKIP_FINALIZE` | unset |

The workflow runs the Android 17 build and trace regression before publishing. Local manual publishing can use legacy keyring or system GPG, but the GitHub workflow intentionally uses in-memory signing because GitHub-hosted runners do not have your local GPG keyring.

Manual local publish is also supported:

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
