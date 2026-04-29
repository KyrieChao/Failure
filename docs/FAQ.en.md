# FAQ

## Does Failure conflict with Spring `@Valid` / `@Validated`?

No.

- You can keep using `@Valid` / `@Validated` for DTO/method parameter validation.
- Failure converts Spring Validation exceptions (for example MVC `MethodArgumentNotValidException`, `ConstraintViolationException`) into a unified error response structure.

## Where should I start?

- Want to replace if + throw: start with `Failure.begin()`.
- Want to extract validation logic: use `@Validate` + `FastValidator`.
- Prefer functional style: use `Result<T>` (optional).

## Why don't I see `errors` details in "collect all" mode?

By default, `errors` details are omitted (for both single-error and multi-error responses). Enable verbose mode:

```yaml
fail-fast:
  verbose: true
```

## Is WebFlux supported?

Yes. WebFlux has the same unified error handling support.
