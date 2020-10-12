<p>
<a href="README.md">English README.md</a>&nbsp;&nbsp;&nbsp;
<a href="README_zh.md">中文 README_zh.md</a>
</p>

# TraceFix
TraceFix 插件在 Android 项目编译的时候，在函数头和尾通过 ASM 插桩的形式插入 Trace 点，方便后续抓取 Systrace 的时候，可以看到比较丰富的信息，方便进行流畅度和响应速度的 Debug

# Getting Started
1. 在 Android Studio 的项目的 gradle.properties 中配置 TRACEFIX_VERSION 
```
TRACEFIX_VERSION=0.0.2
```

2. 在子项目的 build.gradle 添加插件 matrix-gradle-plugin 信息，点击 Sync
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


## TODO
1. 加入无 AndroidX 版本
2. 加入完整包名
3. 加入 Method Map 功能（防止混淆）
4. 改造项目，一个作为本地调试版本，一个作为远程发布调试版本
5. 黑白名单控制功能
6. Init 函数优化
7. get、set 函数优化
8. README 添加 CN 版本
9. README 添加 Version 标志

## Module
### andrdoi-systrace-sample-kt 
远程插件调试 Demo，不依赖本地的插件

### andrdoi-systrace-sample
本地插件调试 Demo，用来开发调试

### android-systrace-sample-jetpack
单独编译的开源项目，用来做测试（插桩内容比较多）

### android-systrace-plugin
核心插件实现

## 
### 使用 TraceFix 插件之前的 Systrace 图对应的 App 部分
![Demo](/pic/before_trace_tag_add.png)

### 使用 TraceFix 插件之后的 Systrace 图对应的 App 部分
![Demo](/pic/after_trace_tag_add.png)

## Demo
Systrace 
![Demo](/pic/systrace_demo.png)