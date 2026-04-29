# Migration Guide

This guide helps you upgrade between versions with a short checklist.

## Pre-upgrade Checklist

- Dependency versions: keep `failure-spring-boot-starter` and optional ecosystem starters on the same version.
- Result API: if you used `Result.success(...)`, switch to `Result.ok(...)`.
- Exception types: the primary public exceptions are `Business` / `MultiBusiness`.
- Package name: the main package is `com.chao.failure` (avoid referencing `com.chao.failfast.*`).
- Configuration prefix: properties use the `fail-fast` prefix (for example `fail-fast.verbose`).

## Common Checks

### 1.3.x: quick migration notes

- **HTTP status fallback**: When context/config is missing, `4xxxx` business codes now fall back to `400` (no longer defaulting to `500`). If you relied on the old behavior, either configure `fail-fast.code-mapping.http-status` or adjust your codes.
- **Customize error body**:
  - MVC: extend `FailFastExceptionHandler` and override `buildBody(Business)` / `buildBody(MultiBusiness)`.
  - WebFlux: replace `WebExceptionHandler`, or extend `FailFastWebExceptionHandler` and override `buildMap/buildMultiMap/buildMapDetail`.
- **Reactive strict stream**: use `StrictProcessor` as the strict-mode reactive output processor (legacy name `FailureFlux` is replaced by `StrictProcessor`).
- **Validator whitelist**: validator reflection instantiation is guarded by `ValidatorWhitelistRegistry`; whitelist your custom validators via `FailFastConfigurer` or a custom registry bean.

### Result factory methods

- `Result.ok(T)`: success
- `Result.fail(...)`: failure (accepts `ResponseCode` or `Business`)
