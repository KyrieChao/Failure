# 迁移指南（Migration Guide）

这份文档用于帮助你从旧版本升级到新版本时快速完成自检与改造。

## 升级前自检清单

- 依赖版本号：确认 `failure-spring-boot-starter` 以及可选生态 starter 使用同一版本号。
- 示例 API：若你的代码中存在 `Result.success(...)`，请改为 `Result.ok(...)`。
- 异常类型：对外的核心异常类型是 `Business` / `MultiBusiness`，请避免使用不存在或已废弃的异常命名。
- 包名：项目主包名为 `com.chao.failure`，请避免引用 `com.chao.failfast.*`。
- 配置前缀：配置项前缀为 `fail-fast`（例如 `fail-fast.verbose`）。

## 常见变更（对照检查）

### 1.3.x：核心重构与行为变更速览

- **HTTP 状态码回退**：当缺少上下文配置时，框架会将 `4xxxx` 错误码稳定映射为 `400`（不再默认回退 `500`）。如果你之前依赖“无上下文返回 500”的行为，请调整错误码设计或补齐 `fail-fast.code-mapping.http-status`。
- **响应体自定义**：
  - MVC：推荐继承 `FailFastExceptionHandler` 并覆写 `buildBody(Business)` / `buildBody(MultiBusiness)`。
  - WebFlux：可自定义/替换 `WebExceptionHandler`，或继承 `FailFastWebExceptionHandler` 并覆写 `buildMap/buildMultiMap/buildMapDetail`。
- **严格流（Reactive strict）**：strict 模式的 reactive 输出以 `StrictProcessor` 为主；若你历史上使用过 `FailureFlux` 命名，请迁移到 `StrictProcessor`。
- **校验器白名单**：AOP 校验器反射创建前会校验 `ValidatorWhitelistRegistry` 白名单；如你使用了自定义 validator，需要通过 `FailFastConfigurer` 或自定义 `ValidatorWhitelistRegistry` 将其加入白名单。

### Result 工厂方法

- `Result.ok(T)`：成功
- `Result.fail(...)`：失败（可传 `ResponseCode` 或 `Business`）

### 文档与代码一致性

若你是从 README 复制示例接入，请优先以代码实现为准，并检查 README 是否已与当前版本对齐。
