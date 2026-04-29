# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- **Core**: 新增 `CheckSpec` / `PathEntry` 底层模型，支持 `at(path)` 绑定失败路径与失败值快照。
- **Core**: 新增对象图递归遍历与递归选项（`ObjectGraphWalker` / `RecursiveOption`）。
- **Validation**: 新增事件驱动校验（`ValidationEventListener` / `ValidationEventManager`），支持进度监听（`ProgressListener`）与取消令牌（`CancelToken`）。
- **Security**: 新增校验器白名单注册表（`ValidatorWhitelistRegistry`）与默认实现（`DefaultValidatorWhitelistRegistry`）。
- **Observability**: 新增/升级 OpenTelemetry trace/span 提取（`OpenTelemetryBridge`），并增强 WebFlux Reactor Context 链路（`ReactiveTrace` / `TraceInfoExtractor`）。
- **Reactive**: 新增 `StrictProcessor`，支持将 strict 模式错误以 `Flux<Business>` 输出。

### Changed
- **HTTP Status**: 上下文缺失时不再无脑回退 `500`；`4xxxx` 错误码稳定映射为 `400`（见 `Business.resolveHttpStatusWithoutContext`）。
- **MVC**: 响应体构建职责拆分：`buildBody`（单/多）分别委托到 `buildMap/buildMultiMap`，便于覆写自定义 JSON 结构。
- **Logging**: Business 默认严重级别为 `ERROR`（并支持 `fail-fast.logging.severity-mapping` 自定义）。

## [1.0.0] - 2026-02-22

### Added
- **Core**: 实现了基于 Fluent API 的链式校验 (`Failure.begin()`, `Failure.strict()`)。
- **Core**: 支持 `Fail-Fast` (快速失败) 和 `Fail-Strict` (全量收集) 两种模式。
- **Annotation**: 引入 `@Validate` 注解和 `FastValidator` 接口，支持 AOP 切面校验。
- **Validation**: 内置丰富的校验断言库 (String, Number, Collection, Date, etc.)。
- **Exception**: 统一的异常处理机制 (`DefaultExceptionHandler`) 和错误码接口 (`ResponseCode`)。
- **Config**: 自动配置 (`FailFastAutoConfiguration`) 和可配置的 HTTP 状态码映射。
- **Docs**: 完善的 README 文档和使用示例。
- **Tests**: 全面的单元测试和集成测试覆盖 (220+ tests)。

### Improved
- 优化了异常堆栈追踪，支持 `shadow-trace` 配置以快速定位校验代码位置。
- 增强了与 Spring Validation (`@Valid`) 的兼容性。

### Initial
- 项目初始化提交。
