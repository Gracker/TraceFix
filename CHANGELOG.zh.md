# 更新日志

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
