<p>
<a href="README.md">English README.md</a>&nbsp;&nbsp;&nbsp;
<a href="README_zh.md">中文 README_ZH.md</a>
</p>

# TraceFix
Auto add  systrace Tag when project compiling 

# Getting Started
1. Configure TRACEFIX_VERSION in gradle.properties.
```
TRACEFIX_VERSION=0.0.2
```

2. Add matrix-gradle-plugin in your build.gradle(Root project or module, Make sure it looks like below):
```
buildscript {
    repositories {
        google()
        jcenter()
    }

    dependencies {
        classpath("com.androidperf:tracefix:${TRACEFIX_VERSION}"){ changing = true }
        implementation 'androidx.appcompat:appcompat:1.2.0'
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

for example
python /mnt/d/Android/platform-tools/systrace/systrace.py -a com.android.settings
```

5. Open trace file on Chrome or https://ui.perfetto.dev/#!/viewer


## DOTO
1. Add white list and black list 
2. Class name opt
3. Init method opt
4. Disable get、set 

## Module
### andrdoi-systrace-sample-kt 
demo module , just for test

### andrdoi-systrace-sample
demo module , just for test

### android-systrace-sample-jetpack
demo module , just for test

### android-systrace-plugin
plugin module , add trace tag when entering a method and exiting a method

## 
### Before auto add trace tag
![Demo](/pic/before_trace_tag_add.png)

### After auto add trace tag
![Demo](/pic/after_trace_tag_add.png)

## Demo
systrace 
![Demo](/pic/systrace_demo.png)