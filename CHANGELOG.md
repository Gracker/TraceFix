# Changelog

## 0.2.0 - 2026-06-05

### Android 17
- Move the default build and verification baseline to Android 17 / API 37 with AGP 9.1.1, Gradle 9.3.1, Kotlin 2.3.21, and Java 17.
- Keep demo modules opt-in so normal plugin builds do not configure old sample apps unless `TRACEFIX_ENABLE_DEMOS` or `TRACEFIX_ENABLE_REMOTE_DEMOS` is set.

### Instrumentation
- Add the `traceFix` extension for variant, class-prefix, constructor, class-initializer, synthetic-method, and bridge-method controls.
- Skip only non-instrumentable methods such as abstract/native methods instead of skipping whole abstract classes.
- Use readable trace section names with stable hash suffixes and Android's 127 UTF-16 code-unit limit.
- Keep exception-safe `Trace.endSection()` pairing for thrown paths.

### Verification
- Replace the legacy 2x2 AGP matrix with an Android 17 trace regression that builds Java and Kotlin fixtures in debug and minified release variants.
- Verify transformed bytecode with method-level `javap` assertions for overloads, synchronized methods, interface default methods, concrete methods in abstract classes, long names, skipped native/abstract methods, and skipped Kotlin synthetic default-argument methods.

### Documentation
- Document `0.2.x` as the Android 17 / AGP 9.1.x line and `0.1.x` as the legacy AGP 7.4/8.3 line.
- Clarify that TraceFix adds app-side `android.os.Trace` sections and does not replace Perfetto, `atrace`, or `ProfilingManager`.

## 0.1.0 - 2026-02-09

### Features
- Migrate plugin instrumentation from removed Transform API to AGP official Instrumentation API.
- Add exception-safe trace pairing by injecting `Trace.endSection()` on exceptional exits.
- Add 2x2 compatibility demo matrix (AGP 7.4.2 / 8.3.2 x local / remote plugin).
- Add Gradle compatibility CI matrix to verify compile-time bytecode injection.

### Documentation
- Rewrite README/README_zh to focus on version selection and usage path.
- Add maintainer release instructions for Maven Central publishing.

## 0.0.6 - 2021-12-23

### Features
- Legacy Transform API based instrumentation release.
