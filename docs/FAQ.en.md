# FAQ

## Concepts & Selection

### Does Failure conflict with Spring @Valid / @Validated?

No. They complement each other:

- `@Valid` / `@Validated` handle DTO field-level constraints (`@NotNull`, `@Email`, etc.)
- Failure handles cross-field business validation ("discount cannot exceed order total")
- Failure converts Spring Validation exceptions (`MethodArgumentNotValidException`, `ConstraintViolationException`) into a unified error response format

### Where should I start?

| Your scenario | Recommended entry |
|---------------|-------------------|
| Replace if + throw, quick start | `Failure.begin()` chain API |
| Reusable validation, shared across endpoints | `@Validate` + `FastValidator` |
| Data processing pipeline, functional style | `Result<T>` / `Results` |
| DTO field-level + cross-field business rules | `@Valid` + `Failure.begin()` together |

### Can Chain API and @Validate be used together?

Yes. This is the most common pattern:

```java
@PostMapping("/order")
@Validate(value = OrderValidator.class, fast = false)  // Annotation: field-level checks
public Result<?> createOrder(@RequestBody @Valid CreateOrderReq req) {
    Failure.strict()                                     // Chain API: cross-field logic
        .at("total")
            .check(t -> t.compareTo(req.getDiscount()) > 0, OrderCode.DISCOUNT_EXCEED)
        .failAll();
    orderService.create(req);
    return Result.ok("Order created");
}
```

## Chain API

### What's the difference between fail() and failAll()?

| Method | Behavior on error | Best for |
|--------|-------------------|----------|
| `fail()` | Throws first `Business` exception | Fast-fail, performance-first |
| `failAll()` | Throws `MultiBusiness` (all errors) | Forms, show all problems at once |
| `verify()` | Writes to `ValidationContext`, no throw | Annotation-driven mode |

**Note**: `failAll()` only makes sense with `Failure.strict()`. With `Failure.begin()` it only collects one error (the chain stops at the first failure).

### How does or() work?

`or()` means "condition A **or** condition B, satisfy either one". Equivalent to logical OR:

```java
// "Is admin" OR "has read permission" — if either passes, no error
Failure.begin()
    .equals(role, Role.ADMIN)
    .or()
    .hasPermission(user, "READ")
    .failNow(UserCode.NO_PERMISSION);
```

**Key rules**:
- `or()` only applies to the immediately adjacent two conditions
- `A.or().B.C` equals `(A || B) && C`
- Avoid `or()` in `strict()` mode — semantics get confusing

### How many errors does strict mode collect before truncation?

Default: **50**. Adjustable:

```yaml
fail-fast:
  chain:
    max-errors: 100
```

When the limit is reached, subsequent checks are skipped and `errorsTruncated` is set to `true`. Use `chain.getCauses()` to retrieve collected errors.

### What's the difference between defer() and check()?

`defer(Supplier)` is **lazy** — the Supplier only executes if all prior checks passed. Best for expensive checks (e.g., DB queries):

```java
Failure.begin()
    .notNull(userId, UserCode.USER_REQUIRED)       // Null check first
    .defer(() -> dbService.isUserActive(userId),   // Only queries DB if userId is non-null
           UserCode.USER_INACTIVE)
    .fail();
```

A raw `check()` would execute the DB query even if `userId` is null (causing NPE).

### When should I use stopOnFail()?

When subsequent checks depend on a prior value being non-null, to prevent NPE:

```java
Failure.strict()
    .notNull(user, UserCode.REQUIRED)
    .stopOnFail()                                    // Stop if user is null
    .check(user.getAge() > 18, UserCode.TOO_YOUNG)   // Safe access to user.getAge()
    .failAll();
```

## @Validate & Validators

### FastValidator vs TypedValidator?

- **FastValidator**: directly implement `validate(dto, ctx)` — best for few DTO types, logic is explicit
- **TypedValidator**: register multiple DTO types via `registerValidators()` — best for many types or when you need dependency injection in a single class

### Why doesn't my @Validate throw errors?

Check two things:
1. Is `fast` set correctly? `fast=true` (default) stops at the first error silently if using `verify()`
2. Are you calling `verify()` (not `fail()`) in the validator? `verify()` doesn't throw

### How do I inject Spring Beans into a Validator?

Implement `FastValidator` or extend `TypedValidator`, annotate with `@Component`, inject as usual:

```java
@Component
public class OrderValidator extends TypedValidator {
    @Resource
    private UserService userService;  // Injected directly

    @Override
    protected void registerValidators() {
        register(OrderDTO.class, (dto, ctx) -> {
            Failure.with(ctx)
                .notNull(dto.getUserId(), OrderCode.USER_REQUIRED)
                .defer(() -> userService.exists(dto.getUserId()),
                       OrderCode.USER_NOT_FOUND)
                .verify();
        });
    }
}
```

## Exceptions & Error Handling

### I enabled shadow-trace but don't see method names in responses

`method` / `location` are written to logs (SLF4J) but **not included in the JSON response body** by default. Check your log output, or customize `FailFastExceptionHandler`.

### How do I customize the error response format?

Extend `FailFastExceptionHandler` and override `handleBusinessException`:

```java
@RestControllerAdvice
public class CustomExceptionHandler extends FailFastExceptionHandler {

    @Override
    @ExceptionHandler(Business.class)
    public ResponseEntity<?> handleBusinessException(Business e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("code", e.getResponseCode().getCode());
        body.put("message", e.getResponseCode().getMessage());
        body.put("detail", e.getDetail());
        body.put("path", e.getPath());
        if (e.getTraceId() != null)
            body.put("traceId", e.getTraceId());
        return ResponseEntity.status(e.getHttpStatus()).body(body);
    }
}
```

### Why don't I see errors details in strict mode responses?

You need to enable verbose mode:

```yaml
fail-fast:
  verbose: true
```

### How do I tell the frontend which field failed?

Use `at(path)` to tag field paths, with `verbose: true`:

```java
Failure.strict()
    .at("username").notBlank(username, UserCode.USERNAME_REQUIRED)
    .at("email").email(email, UserCode.EMAIL_INVALID)
    .failAll();
```

## Async & Reactive

### How do I use failAsync?

For remote validation calls (e.g., checking if a username is already taken):

```java
Failure.begin()
    .notBlank(username, UserCode.USERNAME_REQUIRED)
    .checkAsync(
        userService.isUsernameTaken(username)        // Returns CompletionStage<Boolean>
            .thenApply(taken -> !taken),
        UserCode.USERNAME_EXISTS
    )
    .failAsync()
    .thenRun(() -> userService.register(dto))        // Run business logic on success
    .exceptionally(ex -> {
        log.error("Registration failed", ex);
        return null;
    });
```

### Does WebFlux lose ThreadLocal context?

Yes, WebFlux's threading model can cause ThreadLocal loss. Solution:

```yaml
fail-fast:
  reactive:
    context-first: true   # Read from Reactor Context first, fallback to ThreadLocal
```

## Multi-Scene & I18n

### How do I apply different validation rules per scenario?

Failure has 20 built-in `Scenario` enums (CREATE, UPDATE, SUBMIT, DRAFT, etc.). Use `whenScene` / `inScene`:

```java
// CREATE: username + password both required
// UPDATE: only username required
Failure.with(ctx)
    .inScene(Scenario.CREATE, s -> s
        .notBlank(dto.getUsername(), UserCode.USERNAME_REQUIRED)
        .notBlank(dto.getPassword(), UserCode.PASSWORD_REQUIRED))
    .inScene(Scenario.UPDATE, s -> s
        .notBlank(dto.getUsername(), UserCode.USERNAME_REQUIRED))
    .verify();
```

### How do I support multiple languages?

i18n is enabled by default (`zh_CN`). Add corresponding properties files:

```
src/main/resources/i18n/
├── messages_zh_CN.properties    # Chinese (default)
├── messages_en_US.properties    # English
└── messages_ja_JP.properties    # Japanese
```

The framework auto-selects locale based on the `Accept-Language` header or `default-locale` config.

## Security & Performance

### Is it safe to enable debug-snapshot in production?

**No**. `debug-snapshot: true` includes parameter values in exceptions, potentially exposing passwords, phone numbers, etc. Use it in dev/test only. Production should always have it disabled.

### Does Failure have performance overhead?

The chain API itself has negligible overhead — checks are only executed at the terminal operation (`fail()` etc.). The real cost comes from your check conditions (DB queries, remote calls). Use `defer()` to avoid unnecessary expensive checks.

### Will stack traces blow up my logs?

Default `trim-stack-trace: true` automatically strips Spring/framework stack frames, keeping only your business code. You can also add custom prefixes via `FailFastConfigurer.addExceptionSkipPrefixes()`.
