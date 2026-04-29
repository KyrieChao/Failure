# Failure - Fail-Fast Validation Framework

> **版本：Maven Central(1.3.1) JitPack(latest)** | 作者：Kyrie Chao | 目标：Java 17+, Spring Boot 3.2+

本指南为“项目全景说明”，部分示例与演进记录可能与当前版本存在偏差；请优先以 README 与 docs/ 下的专项文档为准（配置以 `docs/CONFIGURATION.md` 为准）。

---

## 📋 目录

1. [快速开始](#一快速开始)
2. [核心概念](#二核心概念)
3. [API 参考](#三api-参考)
4. [场景化验证](#四场景化验证)
5. [自定义验证器](#五自定义验证器)
6. [函数式结果](#六函数式结果)
7. [Reactive 支持](#七reactive-支持)
8. [配置指南](#八配置指南)
9. [扩展机制](#九扩展机制)
10. [最佳实践](#十最佳实践)
11. [性能基准](#十一性能基准)
12. [迁移指南](#十二迁移指南)
13. [已知问题与限制](#十三已知问题与限制)
14. [常见问题 FAQ](#十四常见问题-faq)
15. [版本变更记录](#十五版本变更记录)
16. [贡献指南](#十六贡献指南)
17. [Roadmap](#十七roadmap)

---

## 一、快速开始

### 1.1 安装依赖

```xml
<dependency>
    <groupId>io.github.kyriechao</groupId>
    <artifactId>failure-spring-boot-starter</artifactId>
    <version>1.3.1</version> <!-- 请使用最新版本 -->
</dependency>
```

### 1.2 最简示例

```java
import com.chao.failure.Failure;
import com.chao.failure.internal.core.ResponseCode;

@Service
public class UserService {

    public void createUser(String username, String email) {
        Failure.begin()
                .notBlank(username, ResponseCode.of(400, "用户名不能为空"))
                .email(email, ResponseCode.of(400, "邮箱格式错误"))
                .fail();

        // 验证通过，继续业务逻辑
    }
}
```

### 1.3 Controller 示例

```java
import com.chao.failure.annotation.Validate;
import com.chao.failure.constant.Scenario;

@RestController
public class UserController {

    @PostMapping("/users")
    @Validate(scene = Scenario.CREATE)
    public Result<User> create(@RequestBody User user) {
        return Result.ok(userService.save(user));
    }
}
```

### 1.4 实体类示例

```java
import com.chao.failure.annotation.Scene;
import com.chao.failure.constant.Scenario;
import jakarta.validation.constraints.*;

public class User {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @Scene(Scenario.CREATE)
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码至少6位")
    private String password;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式错误")
    private String email;

    // getters and setters
}
```

---

## 二、核心概念

### 2.1 设计理念

- **Fail-Fast 原则**：尽早发现并报告错误
- **链式 API**：流畅的验证语法，提高代码可读性
- **场景化验证**：不同业务场景应用不同验证规则
- **类型安全**：运行时类型检查，提供清晰的错误信息
- **函数式编程**：Result 封装，避免异常作为控制流

### 2.2 架构概览

```
┌─────────────────────────────────────────────────────────┐
│  入口层 (Entry)                                         │
│  ├─ Failure.begin()    → 快速失败模式（默认）             │
│  ├─ Failure.strict()   → 严格模式（收集所有错误）         │
│  └─ Failure.with(ctx)  → 上下文模式（自定义验证器）        │
└─────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  验证链 (Chain)                                              │
│  ├─ 状态管理：failFast, alive, errors                        │
│  ├─ 条件控制：when, or, stopOnFail                           │
│  ├─ 场景过滤：whenScene, inScene                             │
│  └─ 异步支持：checkAsync, failAsync                          │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  验证项 (Term)                                               │
│  ├─ StringTerm  → notBlank, email, mobile, lengthBetween    │
│  ├─ NumberTerm  → positive, inRange, greaterThan            │
│  ├─ CollectionTerm → notEmpty, sizeBetween, forEach         │
│  ├─ DateTerm    → before, after, isToday                    │
│  └─ ObjectTerm  → notNull, instanceOf, state                │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  异常处理                                                    │
│  ├─ Business        → 单条错误                               │
│  ├─ MultiBusiness   → 多条错误（严格模式）                    │
│  └─ 自动转换为统一 JSON 响应                                  │
└─────────────────────────────────────────────────────────────┘
```

### 2.3 验证模式对比

| 模式 | 方法 | 错误处理 | 适用场景 |
|------|------|----------|----------|
| **快速失败** | `Failure.begin()` | 第一个错误立即停止 | API 接口、性能敏感 |
| **严格模式** | `Failure.strict()` | 收集所有错误后抛出 | 表单验证、需要完整错误列表 |
| **上下文模式** | `Failure.with(ctx)` | 报告给 ValidationContext | 自定义验证器、配合 @Validate |

---

## 三、API 参考

### 3.1 启动验证链

```java
// 快速失败模式（默认）
Failure.begin()
    .notBlank(username)
    .email(email)
    .fail();

// 严格模式
Failure.strict()
    .notBlank(username)
    .email(email)
    .failAll();

// 上下文模式（自定义验证器中使用）
Failure.with(context)
    .notBlank(user.getUsername())
    .verify();
```

### 3.2 字符串验证 (StringTerm)

```java
Failure.begin()
    .notBlank(str, code)           // 不能为空（去除空白后）
    .notEmpty(str, code)           // 不能为空字符串
    .blank(str, code)              // 必须为空（或只有空白）
    .lengthBetween(str, 6, 20, code)  // 长度在范围内
    .lengthMin(str, 6, code)          // 最小长度
    .lengthMax(str, 20, code)         // 最大长度
    .lengthEquals(str, 11, code)      // 精确长度
    .email(str, code)              // 邮箱格式
    .mobile(str, code)             // 手机号格式（中国）
    .tel(str, code)                // 固定电话
    .url(str, code)                // URL 格式
    .ipAddress(str, code)          // IP 地址
    .uuid(str, code)               // UUID 格式
    .idCard(str, code)             // 身份证号
    .creditCard(str, code)         // 信用卡号
    .match(str, "^[a-z]+$", code)  // 匹配正则
    .startsWith(str, "prefix", code)
    .endsWith(str, "suffix", code)
    .contains(str, "substring", code)
    .notContains(str, "bad", code)
    .isLowerCase(str, code)
    .isUpperCase(str, code)
    .isNumeric(str, code)          // 纯数字
    .isAlpha(str, code)            // 纯字母
    .isAlphanumeric(str, code)     // 字母或数字
    .isJson(str, code)             // JSON 格式
    .isBase64(str, code)           // Base64 格式
    .fail();
```

### 3.3 数值验证 (NumberTerm)

```java
Failure.begin()
    .positive(num, code)           // 必须 > 0
    .nonNegative(num, code)        // 必须 >= 0
    .negative(num, code)           // 必须 < 0
    .nonPositive(num, code)        // 必须 <= 0
    .notZero(num, code)            // 不能为 0
    .isZero(num, code)             // 必须为 0
    .inRange(num, 1, 100, code)    // 在范围内 [min, max]
    .greaterThan(num, 0, code)     // 必须 >
    .greaterOrEqual(num, 0, code)  // 必须 >=
    .lessThan(num, 100, code)      // 必须 <
    .lessOrEqual(num, 100, code)   // 必须 <=
    .multipleOf(num, 10, code)     // 必须是倍数
    .decimalScale(num, 2, code)    // 小数位数
    .fail();
```

### 3.4 集合验证 (CollectionTerm)

```java
Failure.begin()
    .notEmpty(collection, code)    // 不能为空集合
    .isEmpty(collection, code)     // 必须为空集合
    .sizeEquals(collection, 5, code)      // 精确大小
    .sizeBetween(collection, 1, 100, code) // 大小范围
    .hasNoNullElements(collection, code)   // 不能包含 null
    .uniqueElements(collection, code)      // 元素必须唯一
    .allMatch(collection, item -> item > 0, code)   // 全部满足
    .anyMatch(collection, item -> item > 0, code)   // 至少一个满足
    .noneMatch(collection, item -> item < 0, code)  // 全部不满足
    .fail();
```

### 3.5 循环验证 (forEach + Scope)

```java
// 基础用法
Failure.begin()
    .forEach(orders, (order, scope) -> {
        scope
            .notBlank(scope.fieldEntry(Order::getOrderNo), code)
            .positive(scope.fieldEntry(Order::getAmount), code);
    })
    .fail();

// 嵌套对象验证
Failure.begin()
    .forEach(orders, (order, scope) -> {
        scope
            .notBlank(scope.fieldEntry(Order::getOrderNo), code)
            .nested(Order::getAddress, addressScope -> {
                addressScope
                    .notBlank(addressScope.fieldEntry(Address::getCity), code)
                    .notBlank(addressScope.fieldEntry(Address::getStreet), code);
            })
            .forEach(Order::getItems, (item, itemScope) -> {
                itemScope
                    .notBlank(itemScope.fieldEntry(Item::getName), code)
                    .positive(itemScope.fieldEntry(Item::getPrice), code);
            });
    })
    .fail();

// Scope API
.forEach(orders, (order, scope) -> {
    // 获取当前元素
    Order current = scope.it();
    
    // 获取字段（返回 PathEntry）
    scope.fieldEntry(Order::getOrderNo);
    
    // 条件验证
    scope.when(condition);
    scope.whenNot(condition);
    scope.unless(condition);
    
    // 验证断言
    scope.notBlank(scope.fieldEntry(Order::getName), code);
    scope.positive(scope.fieldEntry(Order::getAmount), code);
    
    // 嵌套验证
    scope.nested(Order::getAddress, nestedScope -> { ... });
    
    // 嵌套集合验证
    scope.forEach(Order::getItems, (item, itemScope) -> { ... });
    scope.forEachEntry(Order::getProperties, (key, value, entryScope) -> { ... });
});
```

### 3.6 日期验证 (DateTerm)

```java
Failure.begin()
    .before(date, LocalDate.now(), code)      // 必须在之前
    .after(date, LocalDate.now(), code)       // 必须在之后
    .beforeOrEqual(date, LocalDate.now(), code)
    .afterOrEqual(date, LocalDate.now(), code)
    .between(date, start, end, code)          // 在范围内
    .isToday(date, code)                      // 必须是今天
    .isPast(date, code)                       // 必须是过去
    .isFuture(date, code)                     // 必须是未来
    .isWeekend(date, code)                    // 必须是周末
    .isWeekday(date, code)                    // 必须是工作日
    .fail();
```

### 3.7 对象验证 (ObjectTerm)

```java
Failure.begin()
    .notNull(obj, code)
    .isNull(obj, code)
    .instanceOf(obj, String.class, code)
    .notInstanceOf(obj, Integer.class, code)
    .same(obj1, obj2, code)        // 必须是同一对象（==）
    .notSame(obj1, obj2, code)
    .equals(obj1, obj2, code)      // 必须相等（equals）
    .notEquals(obj1, obj2, code)
    .state(condition, code)        // 自定义条件
    .fail();
```

### 3.8 条件控制

```java
Failure.begin()
    // 基本条件
    .when(user != null)
    .notBlank(user.getName())
    .whenNot(isDeleted)
    .notNull(deletedAt)
    
    // 场景条件
    .whenScene(Scenario.CREATE)
    .notBlank(password)
    
    // 分组条件
    .whenGroup(CreateGroup.class)
    .notBlank(confirmPassword)
    
    // 或逻辑
    .notBlank(email)
    .or()
    .notBlank(phone)
    
    // 代码块
    .ifTrue(user != null, chain -> {
        chain.notBlank(user.getName());
    })
    
    // 场景代码块
    .inScene(Scenario.CREATE, chain -> {
        chain.notBlank(password);
        chain.lengthMin(password, 6);
    })
    
    // 分组代码块
    .inGroup(CreateGroup.class, chain -> {
        chain.notBlank(confirmPassword);
    })
    
    .fail();
```

### 3.9 异步验证

```java
CompletableFuture<Void> future = Failure.begin()
    .notBlank(username)
    .checkAsync(checkUsernameExists(username), code)
    .checkAsync(checkEmailExists(email), code)
    .failAsync();

future.thenRun(() -> {
    // 验证通过，执行业务逻辑
});
```

---

## 四、场景化验证

### 4.1 Scenario 枚举

```java
public enum Scenario {
    // 基础操作
    DEFAULT,      // 默认场景
    CREATE,       // 创建
    UPDATE,       // 更新
    DELETE,       // 删除
    
    // 工作流
    SUBMIT,       // 提交
    APPROVE,      // 审批通过
    REJECT,       // 审批驳回
    DRAFT,        // 保存草稿
    PUBLISH,      // 发布
    
    // 数据导入导出
    IMPORT,       // 导入
    EXPORT,       // 导出
    SYNC,         // 同步
    MIGRATE,      // 迁移
    
    // 批量操作
    BATCH_CREATE, // 批量创建
    BATCH_UPDATE, // 批量更新
    BATCH_DELETE, // 批量删除
    
    // 数据衍生
    COPY,         // 复制
    MERGE,        // 合并
    SPLIT,        // 拆分
    
    // 维护恢复
    RESTORE       // 恢复
}
```

### 4.2 @Scene 注解

```java
public class User {
    @NotBlank
    private String username;
    
    @Scene(Scenario.CREATE)  // 只在创建时验证
    @NotBlank
    @Size(min = 6)
    private String password;
    
    @Scene({Scenario.CREATE, Scenario.UPDATE})  // 多个场景
    @NotBlank
    @Email
    private String email;
}
```

### 4.3 @Validate 注解

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Validate {
    Class<? extends FastValidator>[] value() default {};  // 自定义验证器
    boolean fast() default true;                          // 是否快速失败
    Scenario[] scene() default {Scenario.DEFAULT};        // 业务场景
    Class<?>[] groups() default {};                       // JSR-303 分组
}

// 使用示例
@Validate(scene = Scenario.CREATE)  // 单个场景
public void create(User user) {}

@Validate(scene = {Scenario.CREATE, Scenario.UPDATE})  // 多个场景
public void save(User user) {}
```

---

## 五、自定义验证器

### 5.1 FastValidator 接口

```java
@Component
public class UserValidator implements FastValidator<User> {
    
    @Override
    public void validate(User user, ValidationContext context) {
        Failure.with(context)
            .notBlank(user.getUsername(), code)
            .email(user.getEmail(), code)
            .whenScene(Scenario.CREATE)
            .notBlank(user.getPassword(), code)
            .lengthMin(user.getPassword(), 6, code)
            .verify();
    }
}
```

### 5.2 TypedValidator （推荐）

```java
import com.chao.failure.Failure;

@Component
public class OrderValidator extends TypedValidator {
    // 推荐使用 with
    @Override
    protected void registerValidators() {
        // 注册 Order 验证
        register(Order.class, (order, ctx) -> {
            if (order.getAmount() == null || order.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                ctx.reportError(ResponseCode.of(400, "订单金额必须大于0"));
            }
        });

        // 注册 Payment 验证 
        register(Payment.class, (payment, ctx) -> {
            Failure.with(ctx)
                    .notNull(payment.getPayMethod, ResponseCode.of(400, "支付方式不能为空"))
                    .verify();
        });

        // 注册 User 验证
        register(User.class, (user, ctx) -> {
            // 验证逻辑
        });
    }
}
```

### 5.3 全局注册

```java
@Component
public class MyConfigurer implements FailFastConfigurer {
    @Override
    public void addCustomValidators(ValidatorRegistry registry) {
        registry.register(User.class, new UserValidator());
        registry.register(Order.class, new OrderValidator());
    }
}
```

---

## 六、函数式结果

### 6.1 Result 基础

```java
// 创建结果
Result<User> success = Result.ok(user);
Result<User> fail = Result.fail(ResponseCode.of(404, "用户不存在"));
Result<User> result = Result.ofNullable(user, ResponseCode.of(404, "用户不存在"));

// 状态检查
if (result.isSuccess()) {
    User user = result.get();
}
if (result.isFail()) {
    Business error = result.getError();
}

// 获取值
User user = result.getOrElse(new User());
User user = result.getOrNull();
User user = result.failNow();
```

### 6.2 函数式操作

```java
Result<Order> result = Result.ok(order)
    .map(order -> calculateTotal(order))              // 转换
    .flatMap(order -> saveOrder(order))               // 扁平化
    .filter(order -> order.getAmount() > 0, code)    // 过滤
    .peek(order -> log.info("订单: {}", order))        // 副作用
    .recover(error -> createDefaultOrder());          // 错误恢复
```

### 6.3 批量操作

```java
List<Result<Order>> results = List.of(r1, r2, r3);

// 快速失败
Result<List<Order>> all = Results.sequence(results);

// 收集所有
Result<List<Order>> all = Results.sequenceAll(results);

// 分区
Results.Partition<Order> partition = Results.partition(results);
List<Order> successes = partition.successes();
List<Business> failures = partition.failures();
```

---

## 七、Reactive 支持

### 7.1 WebFlux 集成

```java
@RestController
public class ReactiveUserController {

    @PostMapping("/api/users")
    @Validate(scene = Scenario.CREATE)
    public Mono<Result<User>> create(@RequestBody Mono<User> userMono) {
        return userMono
            .flatMap(user -> userService.create(user))
            .map(Results::success);
    }
}
```

### 7.2 异步验证

```java
Mono<User> userMono = ...;

Mono<Void> validationMono = userMono.flatMap(user -> {
    return Failure.begin()
        .notBlank(user.getUsername())
        .email(user.getEmail())
        .failMono();
});

userMono.zipWith(validationMono, (user, ignore) -> user)
    .flatMap(userService::save)
    .subscribe();
```

### 7.3 Reactive 上下文

```java
Mono<User>
    .just(user)
    .contextWrite(ctx -> ctx.put(ReactiveTrace.TRACE_ID_KEY, traceId))
    .flatMap(user -> {
        // 验证时会自动读取上下文中的 traceId
        return Failure.begin()
            .notBlank(user.getUsername())
            .failMono();
    });
```

---

## 八、配置指南

### 8.1 application.yml

```yaml
fail-fast:
  # 基础配置
  shadow-trace: false             # 调试模式
  trim-stack-trace: true          # 修剪堆栈跟踪
  verbose: false                  # 详细模式
  debug-snapshot: false           # 调试快照
  method-validation-enabled: true # 启用方法验证
  
  # 国际化
  i18n:
    enabled: true
    default-locale: zh_CN
    basename: classpath:i18n/messages
    encoding: UTF-8
    cache-seconds: 3600
  
  # 错误码映射
  code-mapping:
    http-status:
      "auth": [400, 401, 403]
      "system": 500
    
  # 链路追踪
  trace-id:
    enabled: true
    header-name: X-Trace-Id
    generate-if-missing: true
    response-header: true
    mdc-enabled: true
    mdc-key: traceId
  
  # Reactive 配置
  reactive:
    context-first: false
  
  # 验证配置
  validation:
    skip-types:
      - "java.io.InputStream"
```

### 8.2 国际化资源

```properties
# i18n/messages_zh_CN.properties
response.code.validation.error=参数验证失败
validation.username.notBlank=用户名不能为空
validation.email.invalid=邮箱格式错误

# i18n/messages_en_US.properties
response.code.validation.error=Validation failed
validation.username.notBlank=Username is required
validation.email.invalid=Invalid email format
```

---

## 九、扩展机制

### 9.1 FailFastConfigurer

```java
@Component
public class MyConfigurer implements FailFastConfigurer {
    
    @Override
    public void addValidationSkipTypes(SkipTypeRegistry registry) {
        registry.add(InputStream.class, OutputStream.class);
    }
    
    @Override
    public void addExceptionSkipPrefixes(SkipPrefixRegistry registry) {
        registry.add("com.mycompany.internal");
    }
    
    @Override
    public void addCustomValidators(ValidatorRegistry registry) {
        registry.register(User.class, new UserValidator());
    }
}
```

### 9.2 ErrorPolicy

```java
@Component
public class MyErrorPolicy implements ErrorPolicy {
    
    @Override
    public ResponseCode defaultCode() {
        return ResponseCode.of(500, "系统错误");
    }
    
    @Override
    public String defaultDetail(ResponseCode code) {
        return "操作失败，请稍后重试";
    }
    
    @Override
    public boolean captureInvalidValue(FailureContext context) {
        return !"prod".equals(System.getProperty("env"));
    }
}
```

### 9.3 ValidationObserver

```java
@Component
public class MetricsValidationObserver implements ValidationObserver {
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    @Override
    public void onValidationStart(String source, String scene) {
        // 记录验证开始
    }
    
    @Override
    public void onValidationEnd(String source, long durationNanos, boolean success) {
        meterRegistry.timer("validation.duration", 
            "source", source, "scene", scene)
            .record(durationNanos, TimeUnit.NANOSECONDS);
    }
    
    @Override
    public void onValidationFailure(String source, String errorCode) {
        meterRegistry.counter("validation.failure",
            "source", source, "code", errorCode)
            .increment();
    }
    
    @Override
    public void onViolation(String source, String constraint) {
        // 记录约束违反
    }
}

// 注册
ValidationObservers.setObserver(new MetricsValidationObserver());
```

---

## 十、最佳实践

### 10.1 响应码规范

```java
public interface ResponseCodes {
    // 通用错误 400xx
    ResponseCode PARAM_ERROR = ResponseCode.of(40000, "参数错误");
    ResponseCode PARAM_EMPTY = ResponseCode.of(40001, "参数不能为空");
    
    // 用户模块 401xx
    ResponseCode USER_NOT_FOUND = ResponseCode.of(40401, "用户不存在");
    ResponseCode USERNAME_EXISTS = ResponseCode.of(40101, "用户名已存在");
    
    // 订单模块 402xx
    ResponseCode ORDER_NOT_FOUND = ResponseCode.of(40402, "订单不存在");
    ResponseCode INVALID_AMOUNT = ResponseCode.of(40201, "金额无效");
}
```

### 10.2 分层验证

```java
// Controller 层：格式验证
@PostMapping
@Validate(scene = Scenario.CREATE)
public Result create(@RequestBody User user) {
    return Result.ok(userService.create(user));
}

// Service 层：业务规则验证
@Service
public class UserService {
    public User create(User user) {
        Failure.begin()
            .checkAsync(checkUsernameUnique(user.getUsername()), ResponseCodes.USERNAME_EXISTS)
            .checkAsync(checkEmailUnique(user.getEmail()), ResponseCodes.EMAIL_EXISTS)
            .fail();
        
        return userRepository.save(user);
    }
}
```

### 10.3 性能优化

```java
// 1. 使用快速失败模式（默认）
Failure.begin()
    .notBlank(username)
    .email(email)
    .fail();

// 2. 批量验证使用 forEach
Failure.strict()
    .forEach(users, (user, scope) -> {
        scope.notBlank(scope.fieldEntry(User::getName));
    })
    .failAll();

// 3. 缓存 TypedValidator
@Component
public class OrderValidator extends TypedValidator {
    // 验证器只注册一次
}
```

### 10.4 与 JSR-303 共存

```java
// 使用 JSR-303 注解 + @Scene
public class User {
    @NotBlank
    private String username;
    
    @Scene(Scenario.CREATE)
    @NotBlank
    @Size(min = 6)
    private String password;
}

// 使用 Failure 进行复杂验证
Failure.begin()
    .jsr(user).validate()  // JSR-303 验证
    .notBlank(user.getUsername())  // Failure 验证
    .fail();
```

---

## 十一、性能基准

### 11.1 基准测试结果

| 操作 | 耗时 | 说明 |
|------|------|------|
| 基础验证 (5个字段) | ~0.1ms | 快速失败模式 |
| 批量验证 (100个对象) | ~5ms | 严格模式 |
| 异步验证 (2个异步检查) | ~10ms | 并行执行 |
| JSR-303 对比 | 1.5x faster | 相比纯 JSR-303 |

### 11.2 性能优化建议

1. **使用快速失败模式**（默认）
2. **避免在循环中创建验证链**
3. **合理使用场景化验证**
4. **缓存常用验证器**
5. **对于复杂验证，使用 TypedValidator**

---

## 十二、迁移指南

### 12.1 从 JSR-303 迁移

```java
// 旧：纯 JSR-303
public class User {
    @NotBlank
    private String username;
    @Email
    private String email;
}

// 新：JSR-303 + @Scene
public class User {
    @NotBlank
    private String username;
    @Email
    private String email;
    
    @Scene(Scenario.CREATE)
    @NotBlank
    @Size(min = 6)
    private String password;
}

// 旧：手动验证
if (user.getUsername() == null) {
    throw new BadRequestException("用户名不能为空");
}

// 新：链式验证
Failure.begin()
    .notBlank(user.getUsername(), ResponseCode.of(400, "用户名不能为空"))
    .email(user.getEmail(), ResponseCode.of(400, "邮箱格式错误"))
    .fail();
```

### 12.2 从旧版本迁移

| 旧版本 | 新版本 | 说明 |
|--------|--------|------|
| `@Scene(value = "create")` | `@Scene(Scenario.CREATE)` | 使用枚举 |
| `forEach(items, item -> {...})` | `forEach(items, (item, scope) -> {...})` | 支持 Scope |
| 手动验证器 | `TypedValidator` | 更简洁的 API |

---

## 十三、已知问题与限制

### 13.1 已知问题

1. **循环依赖**：验证器之间存在循环依赖时可能导致初始化失败
2. **性能开销**：复杂对象的递归验证可能带来性能开销
3. **类型擦除**：泛型类型的验证可能受到类型擦除的限制

### 13.2 限制

1. **Java 版本**：要求 Java 17+
2. **Spring Boot**：要求 Spring Boot 3.2+
3. **内存消耗**：大量验证错误的收集可能增加内存使用
4. **异步验证**：依赖 CompletableFuture，需要注意线程池配置

---

## 十四、常见问题 FAQ

### Q1: @Validate 的 scene 参数是单数还是复数？

**A:** `scene` 是单数形式，但接受数组。可以写：
- `@Validate(scene = Scenario.CREATE)` - 单个场景
- `@Validate(scene = {Scenario.CREATE, Scenario.UPDATE})` - 多个场景

### Q2: 快速失败模式和严格模式有什么区别？

**A:** 
- **快速失败** (`Failure.begin()`): 遇到第一个验证错误就停止，抛出 `Business` 异常
- **严格模式** (`Failure.strict()`): 收集所有验证错误，最后抛出 `MultiBusiness` 异常

### Q3: 如何在自定义验证器中使用场景？

**A:** 使用 `Failure.with(context)` 和 `whenScene`：

```java
@Override
public void validate(User user, ValidationContext context) {
    Failure.with(context)
        .notBlank(user.getUsername())
        .whenScene(Scenario.CREATE)
        .notBlank(user.getPassword())
        .verify();
}
```

### Q4: forEach 中的 scope.fieldEntry 是什么？

**A:** `scope.fieldEntry(getter)` 返回 `PathEntry`，包含字段值和路径信息，用于：
1. 在错误报告中显示字段路径
2. 直接传递给验证方法

### Q5: 如何处理异步验证？

**A:** 使用 `checkAsync` 和 `failAsync`：

```java
CompletableFuture<Void> future = Failure.begin()
    .notBlank(username)
    .checkAsync(checkExists(username), code)
    .failAsync();

future.thenRun(() -> {
    // 验证通过
});
```

### Q6: 如何自定义错误响应格式？

**A:** 继承 `FailFastExceptionHandler`：

```java
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MyExceptionHandler extends FailFastExceptionHandler {
    @Override
    public ResponseEntity<?> handleBusinessException(Business e) {
        Map<String, Object> body = new HashMap<>();
        body.put("errorCode", e.getResponseCode().getCode());
        body.put("errorMsg", e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).body(body);
    }
}
```

### Q7: 如何集成 Micrometer 指标？

**A:** 使用 `ValidationObserver`：

```java
@Component
public class MetricsValidationObserver implements ValidationObserver {
    @Autowired
    private MeterRegistry meterRegistry;
    
    @Override
    public void onValidationEnd(String source, long durationNanos, boolean success) {
        meterRegistry.timer("validation.duration", 
            "source", source, "scene", scene)
            .record(durationNanos, TimeUnit.NANOSECONDS);
    }
}
```

### Q8: 如何处理 Reactive 环境？

**A:** 使用 `failMono` 和 `ReactiveContext`：

```java
Mono<User> userMono = ...;

userMono.flatMap(user -> {
    return Failure.begin()
        .notBlank(user.getUsername())
        .email(user.getEmail())
        .failMono();
});
```

---

## 十五、版本变更记录

### 1.8.1 (2026-03-26)
- 修复已知问题
- 性能优化

### 1.8.0 (2026-03-25)
- 新增 Scenario 枚举支持
- 新增 inScene(Consumer) 方法
- 新增 forEach + Scope 循环验证
- 新增 TypedValidator 多类型验证器
- 新增 Reactive 支持（WebFlux）
- 新增 ValidationObserver 扩展点
- 新增 AOT Native Image 支持
- 新增 ReflectionCache 性能优化

### 1.7.2 (2026-03-16)
- 修复已知问题

### 1.7.1 (2026-03-15)
- 修复已知问题

### 1.7.0 (2026-03-13)
- 增强验证方法
- 优化错误消息

### 1.6.0 (2026-03-07)
- 新增批量操作支持
- 增强函数式结果

### 1.5.1 (2026-03-05)
- 修复已知问题

### 1.5.0 (2026-03-02)
- 新增 Result 函数式结果封装
- 新增异步验证支持

### 1.4.0 (2026-03-01)
- 新增 JSR-303 集成
- 增强异常处理

### 1.3.1 (2026-02-28)
- 修复已知问题

### 1.3.0 (2026-02-28)
- 增强场景化验证
- 优化性能

### 1.2.2 (2026-02-27)
- 修复已知问题

### 1.2.1 (2026-02-26)
- 修复已知问题
- 优化性能
- 增强错误消息国际化

### 1.2.0 (2026-02-26)
- 新增场景化验证
- 增强验证链 API

### 1.1.0 (2026-02-22)
- 新增更多验证方法
- 增强错误处理

### 1.0.0 (2026-02-22)
- 初始版本
- 基础验证链 API
- 快速失败模式
- Spring Boot 自动配置
- 异常处理机制

---

## 十六、贡献指南

### 16.1 环境要求

- Java 17+
- Spring Boot 3.2+
- Maven 3.8+

### 16.2 开发流程

1. **Fork 仓库**
2. **创建分支**：`git checkout -b feature/your-feature`
3. **提交代码**：`git commit -m "Add your feature"`
4. **推送到远程**：`git push origin feature/your-feature`
5. **创建 Pull Request**

### 16.3 代码规范

- 遵循 Google Java Style Guide
- 所有代码必须有单元测试
- 提交信息使用语义化提交格式
- 新功能需要添加文档

### 16.4 测试

```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn integration-test

# 运行性能测试
mvn test -Dtest=PerformanceTest
```

---

## 十七、Roadmap

### 短期计划 (1.9.0)

- 增强 Kotlin DSL 支持
- 提供更多内置验证器
- 优化 Reactive 性能
- 增强 AI 辅助验证

### 中期计划 (2.0.0)

- 完全支持 Java 21 Virtual Threads
- 提供独立的验证引擎
- 支持更多 Web 框架
- 增强 IDE 插件

### 长期计划 (3.0.0)

- 分布式验证支持
- 更强大的规则引擎
- 与 AI 深度集成
- 跨语言验证标准

---

## 总结

Failure 框架提供了：

1. **简洁的链式 API** - 流畅的验证语法
2. **强大的场景化验证** - 不同场景不同规则
3. **灵活的错误处理** - 快速失败或收集所有错误
4. **函数式编程支持** - Result 封装避免异常控制流
5. **完整的扩展机制** - Configurer、Policy、Observer
6. **Spring Boot 原生集成** - 自动配置，开箱即用
7. **Reactive 支持** - WebFlux 无缝集成
8. **高性能设计** - 缓存和优化

---

**更多资源：**
- GitHub: https://github.com/KyrieChao/Failure
- 版本：Maven Central(1.3.1) JitPack(latest)
- 许可证：Apache-2.0
