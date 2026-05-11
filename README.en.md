# Failure Spring Boot Starter

[![Maven Central](https://img.shields.io/maven-central/v/io.github.kyriechao/failure-spring-boot-starter.svg)](https://central.sonatype.com/artifact/io.github.kyriechao/failure-spring-boot-starter)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java 17+](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/technologies/downloads/)
[![Java CI with Maven](https://github.com/KyrieChao/Failure/actions/workflows/ci.yml/badge.svg)](https://github.com/KyrieChao/Failure/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/KyrieChao/Failure/branch/main/graph/badge.svg)](https://codecov.io/gh/KyrieChao/Failure)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=KyrieChao_Failure&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=KyrieChao_Failure)
[![OpenSSF Best Practices](https://www.bestpractices.dev/projects/12712/badge)](https://www.bestpractices.dev/projects/12712)
[![Release](https://jitpack.io/v/KyrieChao/Failure.svg)](https://jitpack.io/#KyrieChao/Failure)
[![爱发电](https://img.shields.io/badge/爱发电-支持作者-946ce6?style=flat-square)](https://ifdian.net/a/chao242702)
[![Stars](https://img.shields.io/github/stars/KyrieChao/Failure?style=social&logo=github)](https://github.com/KyrieChao/Failure/stargazers)


[中文版本](./README.md)

Failure is a lightweight, high-performance validation and business-exception framework designed for Spring Boot 3.x.
Following the "Fail Fast, Fail Strict" philosophy, it eliminates boilerplate code and provides a type-strict, fluent
validation experience.

🔗 Practical Example Project: [Failure-in-Action](https://github.com/KyrieChao/Failure-in-Action)

🌐 Failure Framework Guide: [KyrieChao Blogs](https://kyriechao.github.io)

📊 Full Performance Report & Test Code: [Failure-Benchmark](https://github.com/KyrieChao/Benchmark)

---

## ⚡ Understand in 30 Seconds

- You write: `Failure.begin().notBlank(...).email(...).fail();`
- You get: a unified error JSON response (code/message/description/errors/timestamp), and it also handles Spring `@Valid`/`@Validated`
- You avoid: repeating `if (...) throw ...` everywhere

## 🚀 60-Second Integration (Minimal MVC Example)

### 1) Add dependency

```xml
<dependency>
    <groupId>io.github.kyriechao</groupId>
    <artifactId>failure-spring-boot-starter</artifactId>
    <version>latest</version>
</dependency>
```

### 2) Minimal Controller

```java
import com.chao.failure.Failure;
import com.chao.failure.internal.core.ResponseCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    public interface UserCode {
        ResponseCode USERNAME_REQUIRED = ResponseCode.of(40001, "USERNAME_REQUIRED", "Username is required");
        ResponseCode EMAIL_INVALID = ResponseCode.of(40002, "EMAIL_INVALID", "Invalid email format");
    }

    public record CreateUserReq(
            @NotBlank(message = "username required")
            String username,
            String email
    ) {}

    @PostMapping
    public String create(@RequestBody @Valid CreateUserReq req) {
        Failure.begin()
                .notBlank(req.username(), UserCode.USERNAME_REQUIRED)
                .email(req.email(), UserCode.EMAIL_INVALID)
                .fail();
        return "ok";
    }
}
```

#### Configure application.yml

```yaml
fail-fast:
  shadow-trace: true
```

### 3) Example error response JSON

```json
{
  "code": 40001,
  "message": "USERNAME_REQUIRED",
  "description": "Username is required",
  "errors": [
    {
      "code": 40001,
      "message": "USERNAME_REQUIRED",
      "path": "UserController#create.username",
      "detail": "Username is required",
      "rejected": ""
    }
  ],
  "timestamp": "2026-04-28 12:34:56"
}
```

### Recommended Path (We choose defaults for you)

- Want to replace if + throw: start with `Failure.begin()` (primary path)
- Want to extract validation from business code: then use `@Validate` + `FastValidator`
- Prefer functional style: then use `Result<T>`

## 🚀 Core Features

- **Fluent Validation Chain**: Supports `Fail-Fast` (immediate fail) and `Fail-Strict` (collect all errors) modes.
- **Rich Assertions**: Built-in 50+ validation methods for Objects, Strings, Numbers, Collections, Date/Time, Enums, Optionals, etc.
- **Default Localization**: Provides out-of-the-box localized error messages (e.g., Chinese support) without manual configuration.
- **Annotation-Driven & Type Dispatch**: Provides `@Validate` annotation and `FastValidator` interface for AOP validation; supports `TypedValidator` pattern for automatic type dispatch, and `TemplateValidator` for template method pattern (execute common validation first, then specific validation) to reduce coupling and improve code reuse.
- **Functional Results**: Provides `Result<T>` monad with `map`, `flatMap`, `recover` operations.
- **Smart Debug Snapshot**: Optionally includes invalid values in exceptions (auto-masking & truncation) when `fail-fast.debug-snapshot=true` (default: false).
- **Smart Exception Handling**: Automatically maps business error codes to HTTP status codes; when context is missing, `4xxxx` codes fall back to `400` (avoids accidental `500`), with `shadow-trace` for quick debugging.
- **Optional Starters**: Provides optional starters (Micrometer Observability / OpenAPI springdoc) without polluting core dependencies.
- **Path & Recursive Validation**: Supports attaching `path` and invalid value snapshots via `at(path)`; includes object graph traversal with configurable recursion options.
- **Event-driven & Cancellable Validation**: Provides validation events (start/end/failure/violation), progress callbacks, and a `CancelToken` for cancellation.
- **Masking & Safety**: Enhanced structured masking with depth/collection/field limits; adds a validator whitelist registry to reduce reflection instantiation risks.
- **Observability & WebFlux Context**: OpenTelemetry trace/span extraction and WebFlux Reactor Context-first support (`fail-fast.reactive.context-first`).

---

## ⚡ 30-Second Comparison

**No More Boilerplate, Just Fluent Flow**

<table>
<tr>
<th width="50%">Traditional "if-throw" Hell</th>
<th width="50%">Failure "Fluent" Style</th>
</tr>
<tr>
<td>

```java
if(user ==null){
    throw Business.of(Code.USER_NULL);
}
if(StringUtils.isBlank(user.getName())){
    throw Business.of(Code.NAME_EMPTY);
}
if(user.getAge() < 18){
    throw Business.of(Code.TOO_YOUNG);
}
```

</td>
<td>

```java
Failure.begin()
    .notNull(user, Code.USER_NULL)
    .notBlank(user.getName(),Code.NAME_EMPTY)
    .min(user.getAge(), 18,Code.TOO_YOUNG)
    .fail();
```

</td>
</tr>
</table>

---

---
## ⚡ Performance

JMH microbenchmark results (vs Hibernate Validator):

![Performance Chart](docs/images/failure_benchmark_visualization.png)

*Test environment & reproducible code: [Benchmark repo](https://github.com/KyrieChao/Benchmark)*

---

## 📚 Documentation

| Document                                  | Content                                                 |
|:------------------------------------------|:--------------------------------------------------------|
| [Quick Start](#%EF%B8%8F-quick-start)     | Installation, basic usage, and three modes introduction |
| [API Reference](docs/API_REFERENCE.en.md)    | Complete API list, method details, and best practices   |
| [Configuration](#%EF%B8%8F-configuration) | application.yml configuration details                   |
| [I18n Guide](./docs/I18N_GUIDE.md)        | Internationalization configuration and key reference    |
| [Response Code Management](./docs/RESPONSE_CODE_MANAGEMENT.md) | Response code mapping and management scheme |
| [Compatibility Matrix](docs/COMPATIBILITY_MATRIX.en.md) | Supported Java / Spring Boot versions |
| [Migration Guide](docs/MIGRATION_GUIDE.en.md) | Upgrade notes & breaking changes |
| [Production Checklist](other/mnk/PRODUCTION_CHECKLIST.en.md) | Production readiness checklist |
| [FAQ](docs/FAQ.en.md) | Frequently asked questions |
| [Security Policy](./SECURITY.md) | Vulnerability reporting |

---

### ⚡ Real-World Example: Order Creation

A basic demo doesn't show the framework's true power. Here's a **complete order creation** validation covering null, collection, numeric, cross-field checks, and `at()` path markers:

```java
public interface OrderCode {
    ResponseCode USER_REQUIRED   = ResponseCode.of(40001, "USER_REQUIRED", "Order user is required");
    ResponseCode ITEMS_EMPTY     = ResponseCode.of(40002, "ITEMS_EMPTY", "Order items cannot be empty");
    ResponseCode TOTAL_INVALID   = ResponseCode.of(40003, "TOTAL_INVALID", "Order total must be positive");
    ResponseCode DISCOUNT_EXCEED = ResponseCode.of(40004, "DISCOUNT_EXCEED", "Discount cannot exceed order total");
}

@PostMapping("/order")
public Result<?> createOrder(@RequestBody @Valid CreateOrderReq req) {
    Failure.strict()                                    // Strict mode: collects all errors
        .at("userId")
            .notNull(req.getUserId(), OrderCode.USER_REQUIRED)
        .at("items")
            .notEmpty(req.getItems(), OrderCode.ITEMS_EMPTY)
        .at("total")
            .positive(req.getTotal(), OrderCode.TOTAL_INVALID)
        .at("total", req.getTotal())                    // 2nd arg = snapshot on failure
            .check(t -> t.compareTo(req.getDiscount()) > 0, OrderCode.DISCOUNT_EXCEED,
                   String.format("Discount %.2f exceeds total %.2f", req.getDiscount(), req.getTotal()))
        .at("items", "sku")
            .forEach(req.getItems(), item ->
                item.getSku() != null && !item.getSku().isBlank(),
                OrderCode.ITEMS_EMPTY)
        .failAll();                                     // Throws all errors at once
    orderService.create(req);
    return Result.ok("Order created");
}
```

**What this demonstrates**:
- `at(path)` — binds each check to a field path for frontend error positioning
- `at(path, value)` — captures value snapshot on failure (pair with `debug-snapshot: true`)
- `strict()` + `failAll()` — collects all errors, essential for forms
- `.check(boolean, code, detail)` — cross-field or custom condition checks
- `String.format` for dynamic detail — errors carry real-time data
- `@Valid` + `Failure.strict()` pairing — JSR-303 for fields, Failure for business rules

---

## 🛠️ Quick Start

### 1. Requirements

- JDK 17+
- Spring Boot 3.2.x+

### 2. Dependency

This project is published on Maven Central. Add the dependency to your `pom.xml`:

```xml
<!-- Maven Central (Recommended for Production) -->
<dependency>
    <groupId>io.github.kyriechao</groupId>
    <artifactId>failure-spring-boot-starter</artifactId>
    <version>latest</version>
</dependency>
```

### Optional Starters (Observability / OpenAPI)

Failure uses a "core starter + optional ecosystem starters" structure. The core starter does not hard-depend on Micrometer/springdoc; optional modules are enabled when present on the classpath.

Optional starters are released independently from the core starter. Keep versions consistent within each optional starter itself, and use the latest released version that matches your dependency strategy.

#### 1) Observability (Micrometer)

Enabled automatically when `MeterRegistry` is present. Metrics:
- `failure.validation.time` (Timer, tag: `source=chain|jsr|method`)
- `failure.validation.count` (Counter, tags: `source=chain|jsr|method`, `result=success|fail`)

```xml
<dependency>
    <groupId>io.github.kyriechao</groupId>
    <artifactId>failure-observability-spring-boot-starter</artifactId>
    <version>latest</version>
</dependency>
```

#### 2) OpenAPI (springdoc)

Enabled automatically when springdoc `OpenAPI` type is present:
- Adds unified error response schemas (`ErrorItem` / `ErrorResponse`)
- Adds `400` / `422` error responses for all operations if missing

```xml
<dependency>
    <groupId>io.github.kyriechao</groupId>
    <artifactId>failure-openapi-springdoc-starter</artifactId>
    <version>latest</version>
</dependency>
```

---

## 💡 Three Validation Modes

### Mode 1: Fail-Fast (Immediate Failure)

**Scenario**: Defensive programming for parameters. Stops subsequent logic immediately upon finding an invalid
parameter.

```java
// Throws exception immediately if notBlank fails, subsequent checks will not be executed
Failure.begin()
    .notBlank(username, UserCode.USERNAME_REQUIRED)
    .email(email, UserCode.EMAIL_INVALID)
    .fail();
```

**Terminal Methods Comparison**:

| Method | Mode | On error | On success | Typical scenario |
|--------|------|----------|------------|------------------|
| `.fail()` | begin() | Throws first `Business` | Continues | Fast-fail, input defense |
| `.failAll()` | strict() | Throws `MultiBusiness` (single if only 1) | Continues | Forms, batch import, collect all |
| `.verify()` | with(ctx) | Writes to ctx silently | No-op | Annotation-driven, decoupled |
| `.failAsync()` | begin() | Async throw first error | Async continue | Remote check, async fast-fail |
| `.failAllAsync()` | strict() | Async throw aggregated | Async continue | Async batch validation |
| `.verifyAsync()` | any | Async return boolean | Async return true | Async pass/fail check |

```java
// Force fail example: Permission check
Failure.begin()
    .notNull(user, UserCode.USER_NOT_FOUND)
    .failNow(UserCode.PERMISSION_DENIED, "Access Denied")  // Throws immediately
    .state(user.getRole() == Role.ADMIN, UserCode.PERMISSION_DENIED)  // Will not execute
    .fail();
```

---

### Mode 2: Fail-Strict (Collect All)

**Scenario**: Form submission, batch import, etc., where all errors need to be returned at once.

```java
// All validations are executed, errors are collected and thrown together
Failure.strict()
    .notBlank(username, UserCode.USERNAME_REQUIRED, "Username cannot be empty")
    .email(email, UserCode.EMAIL_INVALID, "Invalid email format")
    .min(age, 18,UserCode.AGE_TOO_YOUNG, "Must be at least 18 years old")
    .failAll();  // Must use failAll()
```

**Manual Error Retrieval (No Exception)**:

```java
var chain = Failure.strict()
        .notBlank(username, UserCode.USERNAME_REQUIRED)
        .email(email, UserCode.EMAIL_INVALID);

if(!chain.isValid()){
var causes = chain.getCauses();  // Get all errors
    return Result.fail("Validation failed",causes);
}
```

---

### Mode 3: Contextual (Context Integration)

**Scenario**: Used with `@Validate` annotation to decouple validation logic from business code.

```java
// Controller
@PostMapping("/register")
@Validate(value = UserRegisterValidator.class, fast = false)  // fast=false collects all errors
public Result<?> register(@RequestBody UserRegisterDTO dto) {
    userService.register(dto);
    return Result.ok("Registration successful");
}

// Validator
@Component
public class UserRegisterValidator implements FastValidator<UserRegisterDTO> {
    @Override
    public void validate(UserRegisterDTO dto, ValidationContext ctx) {
        Failure.with(ctx)
                .notBlank(dto.getUsername(), UserCode.USERNAME_REQUIRED)
                .email(dto.getEmail(), UserCode.EMAIL_INVALID)
                .verify();  // Contextual mode uses verify()
    }

    @Override
    public Class<?> getSupportedType() {
        return UserRegisterDTO.class;
    }
}
```

**@Validate fast parameter**:

| fast Value       | Behavior                            | Scenario             |
|:-----------------|:------------------------------------|:---------------------|
| `true` (Default) | Stops immediately after first error | Performance priority |
| `false`          | Executes all validation rules       | Show all errors      |

---

### 🔀 Selection Guide: Which approach should I use?

Failure provides two core approaches — **Chain API** and **@Validate annotation**. Here's how to choose.

**Decision tree**:

```
What are you validating?
├─ Simple null/format checks inside Controller/Service
│   → Chain API: Failure.begin() / Failure.strict()
│     Why: intuitive, no extra setup, works inline
│
├─ Reusable validation logic shared across multiple endpoints
│   → @Validate + FastValidator or TypedValidator
│     Why: decouples validation from business code, write once reuse everywhere
│
├─ Both — DTO field constraints + cross-field business rules
│   → @Valid (JSR-303) + Chain API together
│     Why: @Valid for field-level (notNull/email), Chain for relational checks
│
└─ Functional pipeline / data processing
    → Result<T> / Results
      Why: chainable map/flatMap/recover for data pipelines
```

**Quick comparison**:

| Dimension | Chain API | @Validate + FastValidator |
|-----------|-----------|--------------------------|
| Coding style | Fluent calls inline | Separate Validator class, annotation |
| Reusability | Copy-paste for same logic | Shared across controllers |
| Learning curve | Low (IDE autocomplete) | Medium (understand ValidationContext) |
| Best for | Rapid prototyping, one-off checks | Complex logic, team collaboration |
| JSR-303 mix | Pairs with @Valid on DTOs | Pairs with @Valid, errors unified |

---

### Exception Handling & JSR-303 Compatibility

The framework provides built-in `FailFastExceptionHandler`, which not only handles its own business exceptions but also
perfectly integrates with Spring's native JSR-303 (`@Valid` / `@Validated`) validation.

**Features**:

- **Unified Format**: Whether it is an exception thrown by `Failure` or triggered by `@NotNull`, the final response
  format is completely consistent.
- **Mode Adaptation**: The `fast` attribute of the `@Validate` annotation also applies to JSR-303 exceptions.
    - `fast=true` (Default): Even if Hibernate Validator throws multiple errors, only the first one is returned in the
      response.
    - `fast=false`: Returns all errors collected by JSR-303.

To customize, inherit `FailFastExceptionHandler`:

```java

@RestControllerAdvice
public class CustomExceptionHandler extends FailFastExceptionHandler {

    @Override
    @ExceptionHandler(Business.class)
    public ResponseEntity<?> handleBusinessException(Business e) {
        // Custom response format
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("errorCode", e.getResponseCode().getCode());
        body.put("errorMessage", e.getResponseCode().getMessage());
        body.put("detail", e.getDetail());
        return ResponseEntity.badRequest().body(body);
    }
}
```

---

## 🎛️ Flow Control & Lazy Evaluation

### Dynamic Skip (when)

Control whether to execute subsequent validations dynamically.

```java
Failure.begin()
    .when(isVip)                // If not VIP
    .check(vipRule)             // This line will be skipped
    .when(true)                 // Resume execution
    .check(commonRule);         // Continue execution
```

### Lazy Evaluation (defer)

Execute expensive validation logic (via Supplier) only when strictly necessary. If previous validations failed (
Fail-Fast) or were skipped, the supplier will not be executed.

```java
Failure.begin()
    .notNull(userId)// Query DB only if userId is not null
    .defer(() ->dbService
    .isUserActive(userId),UserCode.USER_INACTIVE);
```

### Lazy invalidValue snapshot (Supplier)

When the invalid value snapshot is expensive (e.g., serialization / masking), use the Supplier overload so it is computed only on failure and only when debug snapshot is enabled.

```java
Failure.begin()
    .check(user != null, UserCode.USER_NULL, "User required", () -> user)
    .fail();
```

### Stop on Failure (stopOnFail)

Stops subsequent checks if there are any errors (even in strict mode), until `resume()` is called. Essential for
preventing NPE.

```java
Failure.strict()
    .notNull(user, UserCode.REQUIRED)
    .stopOnFail()      // Stop if user is null
    .defer(() ->user.isAdmin(),UserCode.NO_PERMISSION); // Safe access
```

---

## 🔀 Logical Operations (OR)

The `or()` operator is supported for "Condition A OR Condition B" scenarios.

```java
// Example: User is either ADMIN OR has READ permission
Failure.begin()
    .equals(role, Role.ADMIN)       // Condition A: Is Admin
    .or()                           // OR
    .hasPermission(user, "READ")    // Condition B: Has Read Permission
    .failNow(UserCode.NO_PERMISSION); // Throws if neither A nor B is satisfied
```

Note: `or()` only applies to the immediately adjacent conditions. The default logic for chain calls is `AND`.
`A.or().B.C` is equivalent to `(A || B) && C`.

---

## ⚙️ Configuration

Configure framework behavior in `application.yml` (full property list: [CONFIGURATION.en.md](docs/CONFIGURATION.en.md)):

```yaml
fail-fast:
  shadow-trace: true
  trim-stack-trace: true
  verbose: true
  debug-snapshot: true
  method-validation-enabled: true

  code-mapping:
    constraint-mapping:
      NotBlank: 40010
      Email: 40020
      Positive: 40030
    constraint-path-mapping:
      - constraint: NotBlank
        path: user.username
        code: 40040
      - constraint: NotNull
        path: user.username
        code: 40045
      - constraint: Email
        path: user.email
        code: 40050
    constraint-bean-mapping:
      - constraint: NotBlank
        bean: com.chao.failuretest.model.dto.UserJSRDTO
        code: 40060
      - constraint: Email
        bean: com.chao.failuretest.model.dto.UserDTO
        code: 40070
    http-status:
      40010: 400
    groups:
      auth: ["40100..40199"]
      business: ["40000..40099"]

  trace-id:
    enabled: true
    header-name: X-Trace-Id
    response-header-name: X-Trace-Id
    response-header: true
    generate-if-missing: true
    mdc-key: traceId
    mdc-enabled: true

  reactive:
    context-first: true

  i18n:
    default-locale: zh_CN

  logging:
    banner: false

  masking:
    structured-enabled: true
    max-depth: 4
    max-collection-size: 20
    max-fields: 30
```

### WebFlux Context-First (Recommended)

In WebFlux (reactive) applications, execution may hop across threads and ThreadLocal may lose request context. Enabling `fail-fast.reactive.context-first=true` makes the framework read `traceId/scene/shadow-trace` from Reactor Context first, then fall back to ThreadLocal for compatibility.

### Custom default rules (ErrorPolicy)

For advanced customization of default response code, default detail generation, and whether to capture invalid values, provide an `ErrorPolicy` Spring bean.

---

## 📖 More Documentation

- **[API_REFERENCE.en.md](docs/API_REFERENCE.en.md)** - Complete API Reference, Design Patterns
- **[CONFIGURATION.en.md](docs/CONFIGURATION.en.md)** - Configuration Reference
- **[Failure-in-Action](https://github.com/KyrieChao/Failure-in-Action)** - Live Demo Project

---
## ☕ Support the Author

If you find this project helpful, consider [supporting me on Aifadian](https://ifdian.net/a/chao242702) to help maintain Failure.

Or simply give it a ⭐ Star to help more people discover this project!

---
## 🤝 Contributing

Issues and Pull Requests are welcome! Please:
- Ensure `mvn test` passes.
- Keep coverage above the JaCoCo thresholds (default: 80%+).
- Follow the existing code style.

## 📄 License

Apache License 2.0 - See [LICENSE](LICENSE) for details.

---
**Author**: [KyrieChao](https://github.com/KyrieChao)
