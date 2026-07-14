<p>
<a href="README.md">English README.md</a>&nbsp;&nbsp;&nbsp;
<a href="README_zh.md">中文 README_zh.md</a>
</p>

# TraceFix

TraceFix 是一个 Android 编译期字节码插桩插件，会在方法前后自动插入成对的 `android.os.Trace.beginSection/endSection`。这些 section 可以在 Perfetto 或其他平台 trace viewer 中看到；TraceFix 只负责增加 App 侧 trace section，不替代 Perfetto、`atrace`、`ProfilingManager` 等采集工具。

<!-- android-performance-ecosystem:start -->
## Android 性能分析生态

本仓库属于 [Android Performance Ecosystem](https://github.com/Gracker/android-performance-ecosystem)：它把可选插桩、采集、分析、系统知识与可复现案例连接成一套完整路径。

| 阶段 | 项目 | 作用 | 地址 |
| --- | --- | --- | --- |
| 插桩 | [TraceFix](https://github.com/Gracker/TraceFix) | 在编译期注入 App 侧 android.os.Trace section，让方法执行在运行时 Trace 中可见。 | [GitHub](https://github.com/Gracker/TraceFix) |
| 采集与测量 | [Perfetto Tools](https://github.com/Gracker/perfetto-tools) | 抓取可复现的 Perfetto Trace，并采集 FPS 或 Simpleperf 测量结果。 | [GitHub](https://github.com/Gracker/perfetto-tools) |
| 分析 | [SmartPerfetto](https://github.com/Gracker/SmartPerfetto) | 通过 AI 辅助 Web UI、CLI、报告、会话、对比和证据工作流分析 Trace。 | [GitHub](https://github.com/Gracker/SmartPerfetto) |
| Agent 分析 | [Perfetto Skills](https://github.com/Gracker/Perfetto-Skills) | 为 Agent 提供可移植的 Android、Linux、Chromium Perfetto 分析 Skill，并通过固定版本流程同步选定资产。 | [GitHub](https://github.com/Gracker/Perfetto-Skills) |
| 学习 | [Android Performance Blog](https://github.com/Gracker/Gracker.github.io) | 通过文章、系统原理和案例复盘讲解 Perfetto 与 Systrace 分析。 | [GitHub](https://github.com/Gracker/Gracker.github.io) · [网站](https://www.androidperformance.com/) |
| 系统知识 | Android Internal Wiki | 处于 alpha 阶段的 Android 系统知识库，覆盖 App、Framework、Native 与 Kernel 机制。 | **Coming soon** |
| 复现 | [Trace for Blog (SystraceForBlog)](https://github.com/Gracker/SystraceForBlog) | 提供文章使用的 Perfetto、Systrace 及相关案例文件，支持动手复现。 | [GitHub](https://github.com/Gracker/SystraceForBlog) |
<!-- android-performance-ecosystem:end -->

## Android 17 基线

TraceFix `0.2.x` 面向 Android 17 / API 37 code base：

| 组件 | 版本 |
| --- | --- |
| Artifact | `io.github.gracker:TraceFix:0.2.0` |
| Android Gradle Plugin | `9.1.1` |
| Gradle | `9.3.1` |
| Compile SDK / Target SDK | `37` |
| Java | `17` |

`0.1.x` 是旧的 AGP 7.4/8.3 兼容线。Android 17 / AGP 9.1.x 项目使用 `0.2.x`；只有旧构建还需要之前兼容矩阵时，才继续使用 `0.1.x`。

## 先看怎么选

| 你的目标 | 选择 | 看这一节 |
| --- | --- | --- |
| 在业务 App 里接入 TraceFix | 远程插件 | 「远程插件用法」 |
| 在本仓库开发/调试插件 | 本地插件（`mavenLocal`） | 「本地插件用法」 |
| 验证 Android 17 行为 | trace regression 脚本 | 「兼容性验证」 |

## 远程插件用法

Maven Central artifact：

```groovy
classpath("io.github.gracker:TraceFix:0.2.0")
```

module 或 root `build.gradle` 示例：

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

可选配置：

```groovy
traceFix {
    enabled = true

    // include 为空表示全部启用；exclude 优先级高于 include。
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

TraceFix 会跳过 abstract/native 方法，因为它们没有可插桩的方法体。synthetic/bridge 方法默认跳过，避免把编译器生成的方法大量写进 trace；只有明确需要观察编译器生成代码时再打开。

section 名称保留可读的类名/方法名，并追加稳定 hash 后缀，同时限制在 Android trace section 的 127 个 UTF-16 code unit 上限内。

## 本地插件用法

先发布到本地 Maven：

```bash
./gradlew :android-systrace-plugin:publishToMavenLocal
```

构建本地 demo：

```bash
./gradlew -PTRACEFIX_ENABLE_DEMOS=true \
  :android-systrace-sample-high-local-debug:assembleDebug \
  :android-systrace-sample-high-local-debug:assembleRelease
```

demo module 默认不会参与 settings，这样日常开发只构建插件本身，不会被 sample app 配置拖住。

## 兼容性验证

运行 Android 17 trace regression：

```bash
./scripts/verify-compatibility.sh
```

脚本会验证：

1. AGP 9.1.1 的本地 Java fixture，debug 和开启 R8 的 release。
2. 通过 file-backed Maven 仓库拉取插件的远端 Kotlin fixture，debug 和开启 R8 的 release。
3. 通过 `javap` 做方法级字节码断言，包括普通返回和异常路径。
4. 边界情况：重载方法、synchronized 方法、长方法名、interface default 方法、abstract class 中的 concrete 方法、跳过 abstract/native 方法、跳过 Kotlin synthetic `$default` 方法。

远端 demo 可以手动这样构建：

```bash
./gradlew -PTRACEFIX_ENABLE_REMOTE_DEMOS=true \
  -PTRACEFIX_VERSION_REMOTE=0.2.0-local \
  -PTRACEFIX_REMOTE_REPO_URL=file://$PWD/build/tracefix-remote-repo \
  :android-systrace-sample-remote-debug:assembleDebug \
  :android-systrace-sample-remote-debug:assembleRelease
```

## 发布到 Maven Central

维护者可以在 GitHub Actions 里打开 `Publish TraceFix` workflow，并在 `master` 上手动运行发布。先配置这些 repository secrets：

| Secret | 必需 | 说明 |
| --- | --- | --- |
| `OSSRH_USERNAME` | 是 | Central Portal token username。也支持 `MAVEN_CENTRAL_USERNAME` 或 `SONATYPE_USERNAME`。 |
| `OSSRH_PASSWORD` | 是 | Central Portal token password。也支持 `MAVEN_CENTRAL_PASSWORD` 或 `SONATYPE_PASSWORD`。 |
| `SIGNING_KEY` | 是 | 用于 in-memory signing 的 ASCII-armored PGP 私钥。 |
| `SIGNING_PASSWORD` | 是 | 签名 key 密码。也支持 `SIGNING_PASSPHRASE`。 |
| `SIGNING_KEY_ID` | 可选 | 只有内存私钥需要显式 key id 时才配置。 |

可选 repository variables：

| Variable | 默认值 |
| --- | --- |
| `TRACEFIX_CENTRAL_NAMESPACE` | `io.github.gracker` |
| `TRACEFIX_CENTRAL_PUBLISHING_TYPE` | `automatic` |
| `TRACEFIX_CENTRAL_SKIP_FINALIZE` | 不设置 |

发布 workflow 会先运行 Android 17 build 和 trace regression，再发布 artifact。本地手动发布仍然可以使用 legacy keyring 或系统 GPG；GitHub workflow 刻意使用 in-memory signing，因为 GitHub-hosted runner 没有你的本地 GPG keyring。

也可以在本地手动发布：

1. 配置 Central Portal token：

```bash
export OSSRH_USERNAME=... # https://central.sonatype.com/usertoken 的 token username
export OSSRH_PASSWORD=... # https://central.sonatype.com/usertoken 的 token password
```

也可以放在根目录 `local.properties`：

```properties
ossrhUsername=...
ossrhPassword=...
```

2. 配置签名：

```bash
export SIGNING_KEY='-----BEGIN PGP PRIVATE KEY BLOCK-----...'
export SIGNING_PASSWORD=...
export SIGNING_KEY_ID=... # 可选
```

也支持 Gradle 传统签名配置和系统 GPG：

```properties
signing.keyId=...
signing.password=...
signing.secretKeyRingFile=/path/to/secring.gpg

# 或
useGpgCmd=true
```

3. 先做发版配置检查：

```bash
./gradlew :android-systrace-plugin:verifyReleasePublishConfig
```

4. 发布 release artifact：

```bash
./gradlew :android-systrace-plugin:publishReleaseToCentral
```

兼容别名命令：

```bash
./gradlew :android-systrace-plugin:publishReleaseToSonatype
```

发布后验证 Maven Central：

```bash
curl -I https://repo1.maven.org/maven2/io/github/gracker/TraceFix/0.2.0/TraceFix-0.2.0.pom
```
