<p>
<a href="README.md">English README.md</a>&nbsp;&nbsp;&nbsp;
<a href="README_zh.md">中文 README_zh.md</a>
</p>

# TraceFix
TraceFix 是一款 Android 插件，主要功能是通过编译插桩的形式，丰富 App 进程在 Systrace 中的信息，方便开发在本地通过 Systrace 进行响应速度和流畅度的分析

TraceFix 插件在 Android 项目编译的时候，在函数头和尾通过 ASM 插桩的形式插入 Trace 点，方便后续抓取 Systrace 的时候，可以看到比较丰富的信息，方便进行流畅度和响应速度的 Debug

# Getting Started
1. 在 Android Studio 的项目的 gradle.properties 中配置 TRACEFIX_VERSION 
```
TRACEFIX_VERSION=0.0.4
```

2. 在需要插桩的 Module 的 build.gradle 添加插件 com.androidperf:tracefix 信息，（如果 Module 里面没有 buildscript ，可以加到 root 目录下面的 build.gradle 中，或者在子 Module 里面复制粘贴下面内容到 build.gradle 文件中 ）点击 Sync
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

3. 如果你的 Module 里面没有使用 AndroidX，那么需要在需要插桩的 Module 的 build.gradle 添加 AndroidX 依赖（后续会针对没有 AndroidX 的 Module 做兼容）
```
dependencies {
    implementation 'androidx.appcompat:appcompat:1.2.0'
}
```

4. 在 module 的 build.gradle 文件中添加 plugin
```
apply plugin: 'auto-add-systrace'
```

5. 编译和安装 apk，然后使用命令行抓取 Systrace 文件, 必须要加 -a 并提供包名，否则 Systrace 中不会显示 ：" -a your-package-name" 
```
python /path-to-your-systrace/systrace.py -a your-package-name

for example
python /mnt/d/Android/platform-tools/systrace/systrace.py -a com.android.settings
```

6. 在 Chrome 或者 https://ui.perfetto.dev/#!/viewer 打开 Systrace，找到对应的应用进程查看

## TODO
1. 加入无 AndroidX 版本
2. 加入完整包名
3. 加入 Method Map 功能（防止混淆后没法对应）
4. 黑白名单控制功能
5. Init 函数优化
6. get、set 函数优化
7. README 添加 Version 标志

# Module 说明
## android-systrace-sample-remote-debug
远程插件调试 Demo，不依赖本地的插件，用来测试远程发布的插件是否正常

## android-systrace-sample-local-debug
本地插件调试 Demo，插件发布到本地后，用来本地快速调试验证效果

## android-systrace-sample-jetpack
单独编译的开源项目，需要单独使用 AS 打开，用来做测试，由于是实际项目，所以插桩内容比较多，也是用来做 Debug 的

## android-systrace-plugin
核心插件实现

# 插桩后代码差异 
## 使用 TraceFix 插件前
App 代码

![Demo](/pic/before_trace_tag_add.png)

## 使用 TraceFix 插件后
App 代码的出口和入口都有自动进行插桩

![Demo](/pic/after_trace_tag_add.png)

# Systrace 结果展示
插桩后，抓取 Systrace 可以看到，App 自己加入的函数会被加上 TraceTag，可以看到更加详细的信息 

## Demo 项目
![Demo](/pic/systrace_demo.png)

## 实际项目
可以看到插桩后，比原生的要多显示很多自己加的代码逻辑

![Demo](/pic/systrace_app.png)