# FAQ

## Failure 和 Spring 的 @Valid/@Validated 冲突吗？

不冲突。

- 你可以继续使用 `@Valid` / `@Validated` 做 DTO/参数校验。
- Failure 会把 Spring Validation 抛出的异常（例如 MVC 的 `MethodArgumentNotValidException`、`ConstraintViolationException`）转换为统一的错误响应结构。

## 我应该从哪种用法开始？

- 想替代 if + throw：从 `Failure.begin()` 开始。
- 想把校验逻辑抽离：使用 `@Validate` + `FastValidator`。
- 偏函数式风格：使用 `Result<T>`（可选）。

## 为什么我在“全量收集”时没有看到 errors 明细？

默认不返回 `errors` 明细（无论单错误还是多错误）；如需返回每条错误明细，请开启：

```yaml
fail-fast:
  verbose: true
```

## WebFlux 支持吗？

支持。WebFlux 下同样提供统一的异常响应处理。
