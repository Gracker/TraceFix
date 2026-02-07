<p>
<a href="README.md">English README.md</a>&nbsp;&nbsp;&nbsp;
<a href="README_zh.md">中文 README_zh.md</a>
</p>

# TraceFix

TraceFix 是一款 Android 插件，主要功能是通过编译插桩的形式，丰富 App 进程在 Systrace 中的信息，方便开发在本地通过 Systrace 进行响应速度和流畅度的分析

TraceFix 插件在 Android 项目编译的时候，在函数头和尾通过 ASM 插桩的形式插入 Trace 点，方便后续抓取 Systrace
的时候，可以看到比较丰富的信息，方便进行流畅度和响应速度的 Debug

# Getting Started

1. 在 Android Studio 的项目的 gradle.properties 中配置 TRACEFIX_VERSION

```
TRACEFIX_VERSION=0.0.6
```

2. 在需要插桩的 Module 的 build.gradle 添加插件 com.androidperf:tracefix 信息，（如果 Module 里面没有 buildscript ，可以加到
   root 目录下面的 build.gradle 中，或者在子 Module 里面复制粘贴下面内容到 build.gradle 文件中 ）点击 Sync

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

3. 在 module 的 build.gradle 文件中添加 plugin

```
apply plugin: 'auto-add-systrace'
```

4. 编译和安装 apk，然后使用命令行抓取 Systrace 文件, 必须要加 -a 并提供包名，否则 Systrace 中不会显示 ：" -a your-package-name"

```
python /path-to-your-systrace/systrace.py -a your-package-name

for example
python /mnt/d/Android/platform-tools/systrace/systrace.py -a com.android.settings
```

5. 在 Chrome 或者 https://ui.perfetto.dev/#!/viewer 打开 Systrace，找到对应的应用进程查看

## 兼容矩阵（已验证）

| 场景 | AGP | Gradle | 插件来源 | Module |
| --- | --- | --- | --- | --- |
| 低版本本地插件 Demo | 7.4.2 | 8.4 | 本地仓库（`mavenLocal`） | `android-systrace-sample-local-debug` |
| 低版本远程插件 Demo | 7.4.2 | 8.4 | 远程仓库 | `android-systrace-sample-low-remote-debug` |
| 高版本本地插件 Demo | 8.3.2 | 8.4 | 本地仓库（`mavenLocal`） | `android-systrace-sample-high-local-debug` |
| 高版本远程插件 Demo | 8.3.2 | 8.4 | 远程仓库 | `android-systrace-sample-remote-debug` |

执行四组 Demo 并验证插桩结果：

```bash
LOW_AGP_VERSION=7.4.2 HIGH_AGP_VERSION=8.3.2 ./scripts/verify-compatibility.sh
```

如果要在 Android Studio/Gradle 中直接加载远程 Demo 模块，需要加：

```bash
-PTRACEFIX_ENABLE_REMOTE_DEMOS=true
```

如果要验证远程插件 Demo，还需要补充（或直接执行脚本）：

```bash
-PTRACEFIX_VERSION_REMOTE=1.0 -PTRACEFIX_REMOTE_REPO_URL=file://$PWD/build/tracefix-remote-repo
```

脚本会检查：

1. `Trace.beginSection` 已插入。
2. `Trace.endSection` 已插入。
3. 变换后方法中存在异常路径（`athrow`，方法名 `traceExceptionDemo`），确保抛异常时也会执行 `Trace.endSection`。

## 版本兼容策略

1. 通过 `TRACEFIX_AGP_VERSION` 切换仓库使用的 AGP 版本。
2. 通过 `TRACEFIX_AGP_API_VERSION` 指定插件编译期 `gradle-api` 版本（默认跟随 `TRACEFIX_AGP_VERSION`，兜底 `7.4.2`），用于提升插件二进制兼容性。
3. 对于更老的 AGP（仅 Transform 时代），建议维护单独的 legacy 分支或 artifact，不要和新实现混在同一个二进制里。

## TODO

1. 加入完整包名
2. 加入 Method Map 功能（防止混淆后没法对应）
3. 黑白名单控制功能
4. Init 函数优化
5. get、set 函数优化
6. README 添加 Version 标志

# Module 说明

## android-systrace-sample-local-debug

低版本本地插件 Demo（AGP 7.4.2）。

## android-systrace-sample-low-remote-debug

低版本远程插件 Demo（AGP 7.4.2）。

## android-systrace-sample-high-local-debug

高版本本地插件 Demo（AGP 8.3.2）。

## android-systrace-sample-remote-debug

高版本远程插件 Demo（AGP 8.3.2）。

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
