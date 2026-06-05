# 更新日志

## 0.2.0 - 2026-06-05

### Android 17
- 默认构建和验证基线升级到 Android 17 / API 37，使用 AGP 9.1.1、Gradle 9.3.1、Kotlin 2.3.21 和 Java 17。
- Demo module 默认不参与配置；只有显式传入 `TRACEFIX_ENABLE_DEMOS` 或 `TRACEFIX_ENABLE_REMOTE_DEMOS` 时才启用，避免旧 sample app 影响日常插件构建。

### 插桩能力
- 新增 `traceFix` extension，可配置 variant、class prefix、constructor、class initializer、synthetic method、bridge method。
- 不再因为 class 是 abstract 就跳过整个类，只跳过 abstract/native 这类没有可插桩方法体的方法。
- trace section 名称保留可读方法名并追加稳定 hash，同时遵守 Android 127 个 UTF-16 code unit 的 section 名称上限。
- 保留异常路径上的 `Trace.endSection()` 配对兜底。

### 验证
- 用 Android 17 trace regression 替换旧 2x2 AGP 矩阵，覆盖 Java/Kotlin fixture 的 debug 和开启 R8 的 release。
- 通过 `javap` 做方法级字节码断言，覆盖重载、synchronized、interface default、abstract class 中的 concrete method、长方法名、跳过 native/abstract 方法、跳过 Kotlin synthetic default-argument 方法等边界。

### 文档
- 明确 `0.2.x` 是 Android 17 / AGP 9.1.x 版本线，`0.1.x` 是旧 AGP 7.4/8.3 兼容线。
- 说明 TraceFix 只添加 App 侧 `android.os.Trace` section，不替代 Perfetto、`atrace` 或 `ProfilingManager`。

## 0.1.0 - 2026-02-09

### 新功能
- 将插件插桩从已移除的 Transform API 迁移到 AGP 官方 Instrumentation API。
- 增加异常路径兜底：在方法异常退出时也会注入 `Trace.endSection()`，保证 trace 配对。
- 新增 2x2 兼容性 Demo 矩阵（AGP 7.4.2 / 8.3.2 x 本地/远程插件）。
- 新增 Gradle 兼容性 CI 矩阵，自动验证编译期字节码插桩。

### 文档
- 重写 README/README_zh，突出“如何选版本、怎么接入”。
- 增加维护者 Maven Central 发版说明。

## 0.0.6 - 2021-12-23

### 新功能
- 基于旧 Transform API 的插桩版本。
