# Changelog

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
