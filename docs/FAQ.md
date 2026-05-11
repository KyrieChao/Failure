# FAQ

## 概念与选型

### Failure 和 Spring 的 @Valid/@Validated 冲突吗？

不冲突。两者互补：

- `@Valid` / `@Validated` 做 DTO 字段级约束（`@NotNull`、`@Email` 等）
- Failure 做跨字段业务校验（"优惠不能超过订单总额"这类逻辑）
- Failure 会把 Spring Validation 抛出的异常（`MethodArgumentNotValidException`、`ConstraintViolationException`）转换为统一的错误响应格式

### 我该从哪种用法开始？

| 你的场景 | 推荐入口 |
|----------|---------|
| 替代 if + throw，快速上手 | `Failure.begin()` 链式调用 |
| 校验逻辑要复用，多个接口共享 | `@Validate` + `FastValidator` |
| 数据处理流水线，函数式风格 | `Result<T>` / `Results` |
| DTO 基础校验 + 跨字段业务校验 | `@Valid` + `Failure.begin()` 搭配 |

### Chain API 和 @Validate 能混用吗？

能。最常见的模式：

```java
@PostMapping("/order")
@Validate(value = OrderValidator.class, fast = false)  // 注解驱动：基础字段校验
public Result<?> createOrder(@RequestBody @Valid CreateOrderReq req) {
    Failure.strict()                                     // Chain API：跨字段逻辑
        .at("total")
            .check(t -> t.compareTo(req.getDiscount()) > 0, OrderCode.DISCOUNT_EXCEED)
        .failAll();
    orderService.create(req);
    return Result.ok("下单成功");
}
```

## 链式 API

### fail() 和 failAll() 有什么区别？

| 方法 | 有错误时的行为 | 适用场景 |
|------|--------------|----------|
| `fail()` | 抛出第一个 `Business` 异常 | 快速失败，性能优先 |
| `failAll()` | 抛出 `MultiBusiness`（含所有错误） | 表单校验，一次返回所有问题 |
| `verify()` | 不抛异常，错误写入 `ValidationContext` | 注解驱动模式 |

**注意**：`failAll()` 只适用于 `Failure.strict()`。在 `Failure.begin()` 下调用 `failAll()` 只会得到一个错误（因为第一个错误后就停止了）。

### or() 是什么意思，怎么理解？

`or()` 表示"前一个条件**或**后一个条件，满足其一即可"。等价于逻辑 OR：

```java
// "角色是管理员" 或 "拥有读权限"，满足一个就不报错
Failure.begin()
    .equals(role, Role.ADMIN)
    .or()
    .hasPermission(user, "READ")
    .failNow(UserCode.NO_PERMISSION);
```

**关键规则**：
- `or()` 只作用于紧邻的前后两个条件
- `A.or().B.C` 等价于 `(A || B) && C`
- 不要在 `strict()` 模式下用 `or()`，语义容易混淆

### strict 模式下最多收集多少错误，会被截断吗？

默认最多 **50 个**。可通过配置调整：

```yaml
fail-fast:
  chain:
    max-errors: 100   # 改为 100
```

当错误数量达到上限后，后续校验会被跳过，且 `errorsTruncated` 标记为 `true`。可以通过 `chain.getCauses()` 获取已收集的错误列表。

### defer() 和普通 check() 有什么区别？

`defer(Supplier)` 是**懒加载校验**——只有前面所有校验都通过时，才会执行 Supplier。适合开销大的校验（如数据库查询）：

```java
Failure.begin()
    .notNull(userId, UserCode.USER_REQUIRED)       // 先判空
    .defer(() -> dbService.isUserActive(userId),   // userId 非空才会查数据库
           UserCode.USER_INACTIVE)
    .fail();
```

如果用普通 `check()`，即使 `userId` 为 null，数据库查询也会执行（可能导致 NPE）。

### stopOnFail() 什么时候用？

当后续校验依赖前面的结果不为 null 时，防止 NPE：

```java
Failure.strict()
    .notNull(user, UserCode.REQUIRED)
    .stopOnFail()                                    // user 为 null 则停止后续
    .check(user.getAge() > 18, UserCode.TOO_YOUNG)   // 安全访问 user.getAge()
    .failAll();
```

## @Validate 与 Validator

### FastValidator 和 TypedValidator 选哪个？

- **FastValidator**：直接实现 `validate(dto, ctx)` 方法，适合少量 DTO 类型，逻辑直观
- **TypedValidator**：通过 `registerValidators()` 注册多个 DTO 类型的校验逻辑，一个类管理所有校验，适合类型多、需要依赖注入的场景

### 为什么 @Validate 执行了但没报错？

检查两点：
1. `@Validate` 的 `fast` 参数是否为 `false`（严格模式）—— 如果在 `fast=true` 下用 `verify()`，可能错误被收集但未抛出
2. 是否在 `ValidationContext` 中正确调用了 `verify()` 而非 `fail()`

### 自定义 Validator 如何注入 Spring Bean？

实现 `FastValidator` 或继承 `TypedValidator`，加 `@Component`，注入方式和普通 Spring Bean 一样：

```java
@Component
public class OrderValidator extends TypedValidator {
    @Resource
    private UserService userService;  // 直接注入

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

## 异常与错误处理

### 为什么开了 shadow-trace 还是看不到方法名？

检查异常是否被全局异常处理器吞了。默认 `DefaultExceptionHandler` 会把 `method` / `location` 写入日志（SLF4J），但**响应的 JSON body 里不包含这两个字段**。如果需要看到，看日志输出或自定义异常处理器。

### 如何自定义错误响应格式？

继承 `FailFastExceptionHandler`，覆盖 `handleBusinessException`：

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
        body.put("path", e.getPath());               // 加了 path
        if (e.getTraceId() != null)
            body.put("traceId", e.getTraceId());     // 加了 traceId
        return ResponseEntity
            .status(e.getHttpStatus())
            .body(body);
    }
}
```

### 为什么 strict 模式下响应里没有 errors 明细？

需要开启 `verbose`：

```yaml
fail-fast:
  verbose: true
```

### 如何给前端返回"哪个字段错了"的信息？

用 `at(path)` 标记字段路径，同时开启 `verbose: true`：

```java
Failure.strict()
    .at("username").notBlank(username, UserCode.USERNAME_REQUIRED)
    .at("email").email(email, UserCode.EMAIL_INVALID)
    .failAll();
```

## 异步与响应式

### 异步校验（failAsync）怎么用？

适合远程调用校验（如检查用户名是否已注册）：

```java
Failure.begin()
    .notBlank(username, UserCode.USERNAME_REQUIRED)
    .checkAsync(
        userService.isUsernameTaken(username)        // 返回 CompletionStage<Boolean>
            .thenApply(taken -> !taken),
        UserCode.USERNAME_EXISTS
    )
    .failAsync()
    .thenRun(() -> userService.register(dto))        // 校验通过后执行业务
    .exceptionally(ex -> {
        log.error("注册失败", ex);
        return null;
    });
```

### WebFlux 下用 ThreadLocal 会丢上下文吗？

会。WebFlux 的线程模型会导致 ThreadLocal 上下文丢失。解决方案：

```yaml
fail-fast:
  reactive:
    context-first: true   # 优先从 Reactor Context 读取，再 fallback ThreadLocal
```

## 多场景与多语言

### 同一个 DTO 在不同场景下校验规则不一样，怎么办？

Failure 内置了 20 个 `Scenario`（如 CREATE、UPDATE、SUBMIT、DRAFT 等）。用 `whenScene` / `inScene` 区分：

```java
// CREATE 场景：用户名+密码必填
// UPDATE 场景：只校验用户名非空
Failure.with(ctx)
    .inScene(Scenario.CREATE, s -> s
        .notBlank(dto.getUsername(), UserCode.USERNAME_REQUIRED)
        .notBlank(dto.getPassword(), UserCode.PASSWORD_REQUIRED))
    .inScene(Scenario.UPDATE, s -> s
        .notBlank(dto.getUsername(), UserCode.USERNAME_REQUIRED))
    .verify();
```

Controller 上用 `@Validate(scenes = Scenario.CREATE)` 或 `@Validate(scenes = Scenario.UPDATE)` 区分。

### 如何支持英文/日文等多语言？

开启了 i18n（默认开启 `zh_CN`）。添加对应的 properties 文件即可：

```
src/main/resources/i18n/
├── messages_zh_CN.properties    # 中文（默认）
├── messages_en_US.properties    # 英文
└── messages_ja_JP.properties    # 日文
```

框架会根据请求的 `Accept-Language` 头或配置的 `default-locale` 自动选择。

## 安全与性能

### debug-snapshot 在生产环境能开吗？

**不建议**。`debug-snapshot: true` 会把失败时的参数值直接放进异常信息里，可能包含密码、手机号等敏感数据。测试环境验证脱敏逻辑可以开，生产应关闭。

### Failure 框架本身有性能开销吗？

链式调用本身几乎没有——校验方法只是构建一个检查列表，在终结操作（`fail()` 等）才真正执行。核心开销取决于你写的 check 条件本身（如数据库查询、远程调用）。

### 每条 check 都生成堆栈，日志会不会爆炸？

默认 `trim-stack-trace: true`，框架会自动裁剪掉 Spring/框架层的堆栈帧，只保留业务代码的调用链。你也可以通过 `FailFastConfigurer.addExceptionSkipPrefixes()` 添加更多需要裁剪的前缀。
