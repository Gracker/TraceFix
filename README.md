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

# TODO

1. Add white list and black list
2. Class name opt
3. Init method opt
4. Disable get、set

# Module

## android-systrace-sample-remote-debug

Demo module , just for remote test

## android-systrace-sample-local-debug

Demo module , just for local test

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