# 生产检查清单（Production Checklist）

## 安全与信息泄露

- 确认 `fail-fast.debug-snapshot=false`（避免把失败参数值带到异常/日志里）。
- 确认脱敏策略符合业务要求（敏感字段建议使用注解或统一 masking 策略）。

## 响应与可观测性

- 统一错误响应：确认应用没有自定义的全局异常处理器覆盖 Failure 的默认行为。
- 多错误返回：如需在“全量收集”场景返回 `errors` 明细，开启 `fail-fast.verbose=true`；若不希望暴露明细，保持默认 `false`。
- TraceId：确认 traceId 方案（框架上下文 / OpenTelemetry / 业务自定义）在你的链路中一致。

## 性能与稳定性

- 评估是否需要开启 AOP 注解校验（`@Validate`），并避免在高频路径里做过多反射。
- 生产环境建议保持默认的堆栈裁剪策略，避免异常堆栈过大影响吞吐（详见配置项）。

