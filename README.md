<p>
<a href="README.md">English README.md</a>&nbsp;&nbsp;&nbsp;
<a href="README_zh.md">中文 README_zh.md</a>
</p>

# TraceFix

TraceFix instruments bytecode at compile time and injects `android.os.Trace.beginSection/endSection` around methods.

## Choose This First

| Your Goal | Choose | Use This |
| --- | --- | --- |
| Integrate TraceFix into your app | Remote plugin (recommended) | "Remote Plugin" section |
| Develop or debug plugin in this repo | Local plugin (`mavenLocal`) | "Local Plugin" section |
| Verify compatibility across AGP versions | 2x2 demo matrix | "Compatibility Verification" section |

## Remote Plugin (Recommended)

1. Set plugin version in `gradle.properties`:

```properties
TRACEFIX_VERSION=0.1.0
```

2. Add plugin classpath in module or root `build.gradle`:

```groovy
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("io.github.gracker:TraceFix:${TRACEFIX_VERSION}") { changing = true }
    }
}
```

3. Apply plugin in app module:

```groovy
apply plugin: 'auto-add-systrace'
```

4. Build your app and inspect traces in Perfetto (`https://ui.perfetto.dev/#!/viewer`).

## Publish To Maven Central (Maintainers)

1. Configure Sonatype credentials (choose one source):

- Environment variables:

```bash
export OSSRH_USERNAME=...
export OSSRH_PASSWORD=...
```

- Or root `local.properties` (legacy):

```properties
ossrhUsername=...
ossrhPassword=...
```

2. Configure signing (choose one source):

- In-memory key:

```bash
export SIGNING_KEY='-----BEGIN PGP PRIVATE KEY BLOCK-----...'
export SIGNING_PASSWORD=...
export SIGNING_KEY_ID=... # optional
```

- Gradle legacy signing properties (`~/.gradle/gradle.properties` or `local.properties`):

```properties
signing.keyId=...
signing.password=...
signing.secretKeyRingFile=/path/to/secring.gpg
```

- Or use system GPG command:

```properties
useGpgCmd=true
```

3. Verify publish configuration:

```bash
./gradlew :android-systrace-plugin:verifyReleasePublishConfig
```

4. Publish release artifact:

```bash
./gradlew :android-systrace-plugin:publishReleaseToSonatype
```

## Local Plugin (For Repo Development)

1. Publish plugin to local Maven:

```bash
./gradlew :android-systrace-plugin:publishToMavenLocal
```

2. Use local demo modules:
- Low AGP local demo: `android-systrace-sample-local-debug`
- High AGP local demo: `android-systrace-sample-high-local-debug`

## Compatibility Verification (2x2)

Run full verification (low/high AGP x local/remote plugin):

```bash
LOW_AGP_VERSION=7.4.2 HIGH_AGP_VERSION=8.3.2 ./scripts/verify-compatibility.sh
```

Checks included:

1. `Trace.beginSection` was injected.
2. `Trace.endSection` was injected.
3. Exception path is handled (`athrow` path exists in transformed `traceExceptionDemo`).

## Demo Matrix

| AGP | Local Plugin | Remote Plugin |
| --- | --- | --- |
| 7.4.2 | `android-systrace-sample-local-debug` | `android-systrace-sample-low-remote-debug` |
| 8.3.2 | `android-systrace-sample-high-local-debug` | `android-systrace-sample-remote-debug` |

Notes for remote demo modules:

1. Enable remote demo modules when syncing/building:

```bash
-PTRACEFIX_ENABLE_REMOTE_DEMOS=true
```

2. Provide remote artifact version and repository URL:

```bash
-PTRACEFIX_VERSION_REMOTE=1.0 -PTRACEFIX_REMOTE_REPO_URL=file://$PWD/build/tracefix-remote-repo
```

## Version Strategy

1. `TRACEFIX_AGP_VERSION` selects AGP used by this repo build.
2. `TRACEFIX_AGP_API_VERSION` selects plugin compile-time `gradle-api` dependency (defaults to `TRACEFIX_AGP_VERSION`, fallback `7.4.2`).
3. For pre-instrumentation-era AGP (Transform API only), keep a separate legacy branch/artifact.
