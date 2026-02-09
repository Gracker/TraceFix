<p>
<a href="README.md">English README.md</a>&nbsp;&nbsp;&nbsp;
<a href="README_zh.md">中文 README_zh.md</a>
</p>

# TraceFix

TraceFix 是一个编译期字节码插桩插件，会在方法前后自动插入 `android.os.Trace.beginSection/endSection`。

## 先看怎么选

| 你的目标 | 选择 | 看这一节 |
| --- | --- | --- |
| 在业务 App 里接入 TraceFix | 远程插件（推荐） | 「远程插件用法」 |
| 在本仓库开发/调试插件 | 本地插件（`mavenLocal`） | 「本地插件用法」 |
| 验证高低版本兼容性 | 2x2 Demo 矩阵 | 「兼容性验证」 |

## 远程插件用法（推荐）

1. 在 `gradle.properties` 配置版本：

```properties
TRACEFIX_VERSION=0.1.0
```

2. 在 module 或 root 的 `build.gradle` 添加插件 classpath：

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

3. 在 app module 应用插件：

```groovy
apply plugin: 'auto-add-systrace'
```

4. 编译后用 Perfetto 查看结果：`https://ui.perfetto.dev/#!/viewer`。

## 发布到 Maven Central（维护者）

1. 配置 Sonatype 账号（任选一种来源）：

- 环境变量：

```bash
export OSSRH_USERNAME=...
export OSSRH_PASSWORD=...
```

- 或根目录 `local.properties`（兼容旧方式）：

```properties
ossrhUsername=...
ossrhPassword=...
```

2. 配置签名（任选一种）：

- 内存私钥：

```bash
export SIGNING_KEY='-----BEGIN PGP PRIVATE KEY BLOCK-----...'
export SIGNING_PASSWORD=...
export SIGNING_KEY_ID=... # 可选
```

- Gradle 传统签名配置（`~/.gradle/gradle.properties` 或 `local.properties`）：

```properties
signing.keyId=...
signing.password=...
signing.secretKeyRingFile=/path/to/secring.gpg
```

- 或使用系统 GPG：

```properties
useGpgCmd=true
```

3. 先做配置检查：

```bash
./gradlew :android-systrace-plugin:verifyReleasePublishConfig
```

4. 执行发布：

```bash
./gradlew :android-systrace-plugin:publishReleaseToSonatype
```

## 本地插件用法（仓库开发）

1. 先发布到本地 Maven：

```bash
./gradlew :android-systrace-plugin:publishToMavenLocal
```

2. 使用本地 Demo 模块验证：
- 低版本 AGP 本地插件 Demo：`android-systrace-sample-local-debug`
- 高版本 AGP 本地插件 Demo：`android-systrace-sample-high-local-debug`

## 兼容性验证（2x2）

一条命令跑完高低版本 × 本地/远程插件：

```bash
LOW_AGP_VERSION=7.4.2 HIGH_AGP_VERSION=8.3.2 ./scripts/verify-compatibility.sh
```

脚本会检查：

1. 已插入 `Trace.beginSection`。
2. 已插入 `Trace.endSection`。
3. 异常路径可配对（变换后 `traceExceptionDemo` 存在 `athrow` 路径）。

## Demo 矩阵

| AGP | 本地插件 | 远程插件 |
| --- | --- | --- |
| 7.4.2 | `android-systrace-sample-local-debug` | `android-systrace-sample-low-remote-debug` |
| 8.3.2 | `android-systrace-sample-high-local-debug` | `android-systrace-sample-remote-debug` |

远程 Demo 额外注意：

1. 同步/构建时需要开启远程 Demo 模块：

```bash
-PTRACEFIX_ENABLE_REMOTE_DEMOS=true
```

2. 需要指定远程插件版本和仓库地址：

```bash
-PTRACEFIX_VERSION_REMOTE=1.0 -PTRACEFIX_REMOTE_REPO_URL=file://$PWD/build/tracefix-remote-repo
```

## 版本兼容策略

1. 用 `TRACEFIX_AGP_VERSION` 切换仓库构建使用的 AGP。
2. 用 `TRACEFIX_AGP_API_VERSION` 指定插件编译期 `gradle-api` 版本（默认跟随 `TRACEFIX_AGP_VERSION`，兜底 `7.4.2`）。
3. 对于更老的 AGP（只有 Transform API），建议单独维护 legacy 分支或 artifact。
