<p>
<a href="README.md">English README.md</a>&nbsp;&nbsp;&nbsp;
<a href="README_zh.md">中文 README_zh.md</a>
</p>

# TraceFix

Auto add systrace Tag when project compiling

# Getting Started

1. Configure TRACEFIX_VERSION in gradle.properties.

```
TRACEFIX_VERSION=0.0.6
```

2. Add matrix-gradle-plugin in your build.gradle(Root project or module, Make sure it looks like
   below):

```
buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("io.github.gracker:TraceFix:${TRACEFIX_VERSION}"){ changing = true }
    }
}
```

3. Add dependencies to your app/build.gradle.

```
apply plugin: 'auto-add-systrace'
```

4. Build and install apk , run systrace tools, " -a your-package-name"  is Needed！

```
python /path-to-your-systrace/systrace.py -a your-package-name

#for example
python /mnt/d/Android/platform-tools/systrace/systrace.py -a com.android.settings
```

5. Open trace file on Chrome or https://ui.perfetto.dev/#!/viewer

# Compatibility Matrix (Verified)

| Scenario | AGP | Gradle | Plugin Source | Module |
| --- | --- | --- | --- | --- |
| Low-version local demo | 7.4.2 | 8.4 | Local (`mavenLocal`) | `android-systrace-sample-local-debug` |
| Low-version remote demo | 7.4.2 | 8.4 | Remote repository | `android-systrace-sample-low-remote-debug` |
| High-version local demo | 8.3.2 | 8.4 | Local (`mavenLocal`) | `android-systrace-sample-high-local-debug` |
| High-version remote demo | 8.3.2 | 8.4 | Remote repository | `android-systrace-sample-remote-debug` |

Run all demos and verify bytecode instrumentation:

```bash
LOW_AGP_VERSION=7.4.2 HIGH_AGP_VERSION=8.3.2 ./scripts/verify-compatibility.sh
```

To load remote demo modules in Android Studio/Gradle directly, add:

```bash
-PTRACEFIX_ENABLE_REMOTE_DEMOS=true
```

When verifying remote-plugin demos, use these properties as well (or run the script):

```bash
-PTRACEFIX_VERSION_REMOTE=1.0 -PTRACEFIX_REMOTE_REPO_URL=file://$PWD/build/tracefix-remote-repo
```

The script checks:

1. `Trace.beginSection` was injected.
2. `Trace.endSection` was injected.
3. Exception path exists (`athrow`) in transformed methods (`traceExceptionDemo`).

# Version Strategy

1. `TRACEFIX_AGP_VERSION` controls AGP in this repo.
2. `TRACEFIX_AGP_API_VERSION` controls the `gradle-api` compile dependency for plugin binary compatibility (default follows `TRACEFIX_AGP_VERSION`, fallback `7.4.2`).
3. For AGP versions before instrumentation API support, keep a separate legacy Transform-based artifact/branch. Do not mix both implementations in one binary.

# TODO

1. Add white list and black list
2. Class name opt
3. Init method opt
4. Disable get、set

# Module

## android-systrace-sample-local-debug

Low-version local-plugin demo (AGP 7.4.2).

## android-systrace-sample-low-remote-debug

Low-version remote-plugin demo (AGP 7.4.2).

## android-systrace-sample-high-local-debug

High-version local-plugin demo (AGP 8.3.2).

## android-systrace-sample-remote-debug

High-version remote-plugin demo (AGP 8.3.2).

## android-systrace-sample-jetpack

Demo module , just for test

## android-systrace-plugin

plugin module , add trace tag when entering a method and exiting a method

# Code Change

## Before auto add trace tag

![Demo](/pic/before_trace_tag_add.png)

### After auto add trace tag

![Demo](/pic/after_trace_tag_add.png)

# Systrace

## Demo Project

![Demo](/pic/systrace_demo.png)

![Demo](/pic/systrace_app.png)
