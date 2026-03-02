# API Reference

[English Version](./API_REFERENCE.en.md)

本文档详尽列出了 `Failure` 校验框架的核心 API、设计模式及最佳实践。基于 fluent-interface 风格设计，旨在提供高可读性、强类型安全且易于扩展的校验体验。

---

## 目录

1. [设计模式与核心入口](#1-设计模式与核心入口)
2. [校验方法详解](#2-校验方法详解)
3. [终结操作](#3-终结操作)
4. [最佳实践](#4-最佳实践)

---

## 1. 设计模式与核心入口

框架支持三种核心校验模式，分别适用于不同的业务场景：

### 1.1 Fail-Fast (快速失败模式)

**适用场景**: 参数防御性编程，一旦发现非法参数立即停止后续逻辑，避免资源浪费。

```java
// 一旦 notBlank 失败，立即抛出异常，不会执行后续校验
Failure.begin()
    .notBlank(username, UserCode.USERNAME_REQUIRED)
    .email(email, UserCode.EMAIL_INVALID)
    .fail();
```

**特点**:

- 遇到第一个错误立即停止
- 抛出 `BusinessException`
- 性能最优

---

### 1.2 Fail-Strict (全量收集模式)

**适用场景**: 批量导入、表单提交等需要一次性返回所有错误的场景。

```java
// 即使 username 为空，也会继续检查 email，最终收集所有错误
Failure.strict()
    .notBlank(username, UserCode.USERNAME_REQUIRED)
    .email(email, UserCode.EMAIL_INVALID)
    .failAll();
```

**特点**:

- 执行所有校验规则
- 抛出 `MultiBusinessException`（包含所有错误）
- 适合前端表单一次性展示所有错误

---

### 1.3 Contextual (上下文集成模式)

**适用场景**: 结合 `TypedValidator` 或 `FastValidator` 使用，将校验逻辑与业务逻辑解耦。

```java
// 错误实时报告给 ctx，不立即抛出
Failure.with(ctx)
    .notBlank(dto.getUsername(),UserCode.USERNAME_BLANK)
    .email(dto.getEmail(),UserCode.EMAIL_INVALID)
    .verify();
```

**特点**:

- 校验全程不抛异常
- 错误写入 `ValidationContext`
- 由调用方决定后续处理

---

## 2. 校验方法详解

所有校验方法均支持以下四种重载形式（以 `notNull` 为例）：

1. `notNull(obj)` - 由 `failNow` 决定错误处理方式
2. `notNull(obj, code)` - 指定 `ResponseCode`
3. `notNull(obj, code, detail)` - 指定 `ResponseCode` 和详细描述
4. `notNull(obj, Consumer<Business.Fabricator>)` - 使用 Lambda 构建复杂错误

```java
// 示例：四种重载方式
Failure.begin()
    .notNull(obj)                                    // 方式1
    .notNull(obj, UserCode.REQUIRED)                 // 方式2
    .notNull(obj, UserCode.REQUIRED, "不能为空")      // 方式3
    .notNull(obj, f ->f.responseCode(UserCode.REQUIRED).detail("自定义详情"))  // 方式4
    .fail();
```

---

### 2.1 通用对象校验 (Object)

| 方法                             | 描述                       |
|--------------------------------|--------------------------|
| `notNull(obj)` / `exists(obj)` | 验证对象不为 null              |
| `isNull(obj)`                  | 验证对象必须为 null             |
| `instanceOf(obj, type)`        | 验证对象是指定类型的实例             |
| `notInstanceOf(obj, type)`     | 验证对象不是指定类型的实例            |
| `allNotNull(objs...)`          | 验证一组对象均不为 null           |
| `equals(obj1, obj2)`           | 验证对象相等 (`Object.equals`) |
| `notEquals(obj1, obj2)`        | 验证对象不等                   |
| `same(obj1, obj2)`             | 验证引用相同 (`==`)            |
| `notSame(obj1, obj2)`          | 验证引用不同 (`!=`)            |

---

### 2.2 字符串校验 (String)

| 方法                                | 描述                    |
|-----------------------------------|-----------------------|
| `notBlank(str)` / `notEmpty(str)` | 非 null 且去除首尾空格后长度 > 0 |
| `blank(str)`                      | 为 null、空串或仅包含空白字符     |
| `lengthBetween(str, min, max)`    | 字符长度在 [min, max] 之间   |
| `lengthMin(str, min)`             | 最小长度限制                |
| `lengthMax(str, max)`             | 最大长度限制                |
| `match(str, regex)`               | 符合正则表达式               |
| `email(str)`                      | 邮箱格式校验                |
| `mobile(str)`                     | 中国大陆手机号格式校验           |
| `url(str)`                        | URL 格式校验              |
| `ipAddress(str)`                  | IPv4 地址格式校验           |
| `uuid(str)`                       | UUID 格式校验             |
| `isNumeric(str)`                  | 仅包含数字                 |
| `isAlpha(str)`                    | 仅包含字母                 |
| `isAlphanumeric(str)`             | 仅包含字母或数字              |
| `isLowerCase(str)`                | 全部小写                  |
| `isUpperCase(str)`                | 全部大写                  |
| `contains(str, sub)`              | 包含子串                  |
| `notContains(str, sub)`           | 不包含子串                 |
| `startsWith(str, prefix)`         | 以指定前缀开头               |
| `endsWith(str, suffix)`           | 以指定后缀结尾               |
| `equalsIgnoreCase(str1, str2)`    | 忽略大小写相等               |

---

### 2.3 数值校验 (Number)

支持 `Integer`, `Long`, `Double`, `BigDecimal` 等所有 `Number` 子类。

| 方法                               | 描述                 |
|----------------------------------|--------------------|
| `positive(num)`                  | 正数 (> 0)           |
| `nonNegative(num)`               | 非负数 (>= 0)         |
| `negative(num)`                  | 负数 (< 0)           |
| `notZero(num)`                   | 非零 (!= 0)          |
| `isZero(num)`                    | 等于零 (== 0)         |
| `greaterThan(val, threshold)`    | 大于 (>)             |
| `greaterOrEqual(val, threshold)` | 大于等于 (>=)          |
| `lessThan(val, threshold)`       | 小于 (<)             |
| `lessOrEqual(val, threshold)`    | 小于等于 (<=)          |
| `inRange(val, min, max)`         | 在区间 [min, max] 内   |
| `multipleOf(val, divisor)`       | 是 divisor 的倍数      |
| `decimalScale(decimal, scale)`   | BigDecimal 小数位精度检查 |

---

### 2.4 集合与容器校验

#### Collection / List / Set

| 方法                                  | 描述             |
|-------------------------------------|----------------|
| `notEmpty(collection)`              | 容器非 null 且包含元素 |
| `isEmpty(collection)`               | 容器为 null 或无元素  |
| `sizeBetween(collection, min, max)` | 元素数量在范围内       |
| `sizeEquals(collection, size)`      | 元素数量严格相等       |
| `sizeMin(collection, min)`          | 元素数量 >= min    |
| `sizeMax(collection, max)`          | 元素数量 <= max    |
| `contains(collection, element)`     | 包含指定元素         |
| `notContains(collection, element)`  | 不包含指定元素        |
| `hasNoNullElements(collection)`     | 容器内不包含 null 元素 |
| `allMatch(collection, predicate)`   | 所有元素均满足条件      |
| `anyMatch(collection, predicate)`   | 任一元素满足条件       |
| `noneMatch(collection, predicate)`  | 没有元素满足条件       |

#### Map

| 方法                           | 描述                |
|------------------------------|-------------------|
| `notEmpty(map)`              | Map 非 null 且包含键值对 |
| `isEmpty(map)`               | Map 为 null 或无键值对  |
| `containsKey(map, key)`      | Map 包含指定 Key      |
| `containsValue(map, value)`  | Map 包含指定 Value    |
| `sizeBetween(map, min, max)` | 键值对数量在范围内         |

#### Array

| 方法                               | 描述               |
|----------------------------------|------------------|
| `notEmpty(array)`                | 数组非 null 且长度 > 0 |
| `isEmpty(array)`                 | 数组为 null 或长度为 0  |
| `lengthBetween(array, min, max)` | 数组长度在范围内         |
| `contains(array, element)`       | 数组包含指定元素         |

---

### 2.5 日期时间校验 (Date/Time)

支持 `Date`, `LocalDate`, `LocalDateTime`, `Instant`, `ZonedDateTime`。

| 方法                          | 描述                   |
|-----------------------------|----------------------|
| `isPast(date)`              | 时间在当前时刻之前            |
| `isFuture(date)`            | 时间在当前时刻之后            |
| `isToday(date)`             | 日期是今天                |
| `after(d1, d2)`             | d1 晚于 d2             |
| `before(d1, d2)`            | d1 早于 d2             |
| `between(date, start, end)` | 时间在区间 [start, end] 内 |
| `notBefore(date, boundary)` | 时间不早于 boundary       |
| `notAfter(date, boundary)`  | 时间不晚于 boundary       |

---

### 2.6 Optional 校验

| 方法                         | 描述           |
|----------------------------|--------------|
| `isPresent(opt)`           | Optional 包含值 |
| `isEmpty(opt)`             | Optional 为空  |
| `ifPresent(opt, consumer)` | 如果有值，执行消费操作  |

---

### 2.7 枚举校验

| 方法                               | 描述          |
|----------------------------------|-------------|
| `enumValue(enumClass, value)`    | 字符串是有效的枚举名称 |
| `enumConstant(value, enumClass)` | 枚举值属于指定类型   |
| `enumIn(value, constants...)`    | 枚举值在指定常量列表中 |

---

### 2.8 布尔与状态校验

| 方法                                   | 描述                |
|--------------------------------------|-------------------|
| `isTrue(bool)` / `state(bool, code)` | 布尔值为 true         |
| `isFalse(bool)`                      | 布尔值为 false        |
| `satisfies(obj, predicate)`          | 对象满足自定义 Lambda 条件 |
| `compare(f1, f2, comparator)`        | 使用比较器比较结果为 0      |

---

### 2.9 身份标识校验

| 方法              | 描述              |
|-----------------|-----------------|
| `idCard(str)`   | 中国大陆身份证号校验      |
| `bankCard(str)` | 银行卡号校验（Luhn 算法） |

---

### 2.10 逻辑运算 (Logical)

| 方法    | 描述                                     |
|-------|----------------------------------------|
| `or()` | 逻辑或，连接前后两个校验条件。若前一个条件满足，则跳过后一个；若前一个不满足，则尝试后一个。 |

**示例**：

```java
// 场景：手机号或邮箱，满足其一即可
Failure.begin()
    .mobile(input)                  // 尝试校验手机号
    .or()                           // 或
    .email(input)                   // 尝试校验邮箱
    .failNow(UserCode.INVALID_INPUT); // 若都不满足，则抛出异常
```

**注意事项**：
1. `or()` 仅影响其**紧邻的右侧**一个条件。
2. 结合性：`A.or().B.C` 解析为 `(A || B) && C`。
3. 如果 `A` 满足，`B` 的校验逻辑会被跳过（短路）。
4. 如果 `A` 不满足，`B` 将被执行。如果 `B` 满足，则整体视为通过，之前的错误会被清除。

---

## 3. 终结操作 (Terminal Operations)

| 方法                       | 适用模式        | 描述                  |
|--------------------------|-------------|---------------------|
| `fail()`                 | `begin()`   | 执行校验，若有错误则抛出第一个异常   |
| `failAll()`              | `strict()`  | 执行校验，若有错误则抛出聚合异常    |
| `failNow(code, message)` | `begin()`   | **强制立即失败**，无条件抛出异常  |
| `verify()`               | `with(ctx)` | Contextual 模式的语义终结符 |
| `getCauses()`            | 所有          | 获取当前链中已收集的所有错误对象    |
| `isValid()`              | 所有          | 返回当前链是否通过校验         |
| `onFail(runnable)`       | `begin()`   | 校验失败时执行的回调函数        |

### 终结方法对比

```java
// Fail-Fast 模式
Failure.begin()
    .notBlank(username, UserCode.REQUIRED)
    .fail();                    // 有错误时抛出第一个异常

Failure.begin()
    .notNull(user)
    .failNow(UserCode.FORBIDDEN, "无权访问");


// Fail-Strict 模式  
Failure.strict()
    .notBlank(username, UserCode.REQUIRED)
    .email(email, UserCode.INVALID)
    .failAll();                 // 收集所有错误后统一抛出

// Contextual 模式
Failure.with(ctx)
    .notBlank(username, UserCode.REQUIRED)
    .verify();                  // 不抛异常，错误写入 ctx

```

---

## 4. 最佳实践

### 4.1 错误码管理

建议使用枚举管理错误码，实现 `ResponseCode` 接口：

```java
public enum UserCode implements ResponseCode {
    USERNAME_REQUIRED(40001, "用户名不能为空", "请输入用户名"),
    EMAIL_INVALID(40002, "邮箱格式不正确", "请输入正确的邮箱地址"),
    AGE_TOO_YOUNG(40003, "年龄必须 ≥ 18 岁", "年龄必须大于等于 18 岁");

    private final int code;
    private final String message;
    private final String description;

    UserCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
```

---

### 4.2 集中式校验 (TypedValidator)

将校验逻辑从 Controller/Service 中抽离到独立的 Validator 类：

```java

@Component
public class CustomValidator extends TypedValidator {
    private static final Set<Integer> GENDER_STATUS = Set.of(0, 1, 2);
    @Resource
    private UserService userService;

    @Override
    protected void registerValidators() {
        // 注册校验
        register(UserRegisterDTO.class, (dto, ctx) -> {
            Failure.with(ctx)
                    .satisfies(dto.getGender(), GENDER_STATUS::contains, UserCode.GENDER_UNKNOWN, "性别")
                    .notBlank(dto.getUsername(), UserCode.USERNAME_BLANK)
                    .notBlank(dto.getNickname(), UserCode.NICKNAME_BLANK)
                    .email(dto.getEmail(), UserCode.EMAIL_INVALID)
                    .mobile(dto.getPhone(), UserCode.PHONE_INVALID)
                    .verify();
            if (ctx.isFailed()) {
                return;
            }
            checkDuplicate(dto, ctx);
        });
    }

    private void checkDuplicate(UserRegisterDTO dto, ValidationContext context) {
        if (exists(User::getUsername, dto.getUsername())) {
            context.reportError(UserCode.USERNAME_EXIST);
        }
        if (exists(User::getEmail, dto.getEmail())) {
            context.reportError(UserCode.EMAIL_EXIST);
        }
        if (exists(User::getPhone, dto.getPhone())) {
            context.reportError(UserCode.PHONE_EXIST);
        }
    }

    private boolean exists(SFunction<User, ?> column, Object value) {
        if (value == null) return false;
        return userService.lambdaQuery()
                .eq(column, value)
                .eq(User::getIsDeleted, 0)
                .exists();
    }
}
```

Controller/ServiceImpl 中使用：

```java

@PostMapping("/register")
@Validate(value = CustomValidator.class, fast = false) // fast = false 表示使用集中式校验
public Result<?> register(@RequestBody UserRegisterDTO dto) {
    // AOP 自动执行校验
    userService.register(dto);
    return Result.success("注册成功");
}
```

---

### 4.3 复杂校验逻辑

对于需要依赖注入的复杂校验，使用 `FastValidator`：

```java

@Component
public class UserLoginValidator implements FastValidator<UserLoginDTO> {

    @Resource
    private UserService userService;

    @Override
    public void validate(UserLoginDTO dto, ValidationContext ctx) {
        // 基础格式校验
        Failure.with(ctx)
                .notBlank(dto.getEmail(), UserCode.EMAIL_BLANK)
                .notBlank(dto.getPassword(), UserCode.PASSWORD_BLANK)
                .email(dto.getEmail(), UserCode.EMAIL_INVALID)
                .verify();

        // 如果基础校验失败，提前返回
        if (ctx.isFailed()) return;

        // 业务逻辑校验
        User user = userService.findByEmail(dto.getEmail());
        Failure.with(ctx)
                .state(user != null, UserCode.USER_NOT_FOUND)
                .verify();
    }

    @Override
    public Class<?> getSupportedType() {
        return UserLoginDTO.class;
    }
}
```

---

### 4.4 异常处理

框架提供 `DefaultExceptionHandler`，如需自定义可继承 `FailFastExceptionHandler`：

```java

@RestControllerAdvice
public class CustomExceptionHandler extends FailFastExceptionHandler {

    @Override
    @ExceptionHandler(Business.class)
    public ResponseEntity<?> handleBusinessException(Business e) {
        // 自定义响应格式
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

### 4.5 函数式结果处理

使用 `Result<T>`：

```java
// 创建
Result<String> ok = Result.ok("success");
Result<String> fail = Result.fail(UserCode.NOT_FOUND, "用户不存在");

// 链式操作
Result<Integer> result = Result.ok("42")
    .map(Integer::parseInt)
    .filter(n -> n > 0, UserCode.INVALID)
    .recover(e -> 0);

// 获取值 失败时返回默认值 
Integer value = result.getOrElse(-1);
// 失败时会报错
Integer value = result.get();
```

---

使用 `Results<T>` ：

```java
// 异常捕获
Result<Integer> r1 = Results.tryOf(() -> Integer.parseInt(str), UserCode.INVALID);

// Optional 转换
Result<User> r2 = Results.fromOptional(userRepo.findById(id), UserCode.NOT_FOUND);

// 条件执行
Result<String> r3 = Results.whenOrFail(age >= 18, "成年", UserCode.TOO_YOUNG);

// 批量收集
Result<List<User>> r4 = Results.sequence(List.of(r1, r2, r3));

// 分区处理
Results.Partition<User> partition = Results.partition(results);
List<User> successes = partition.getSuccesses();

// 组合
Result<DTO> dto = Results.zip(userResult, orderResult, (u, o) -> new DTO(u, o));

// 遍历
Result<List<Integer>> nums = Results.traverse(strList, s -> 
    Results.tryOf(() -> Integer.parseInt(s), UserCode.INVALID)
);

// 重试
Result<String> api = Results.retry(3, Duration.ofMillis(100), () -> callApi());

// 管道
Result<Integer> piped = Results.pipe(
    Result.ok(10),
    n -> Result.ok(n * 2),
    n -> Result.ok(n + 5)
);

```
---
```java

public Result<OrderDTO> getOrder(Long userId, Long orderId) {
    // 并行查询
    Result<User> user = Results.tryOf(
            () -> userRepo.findById(userId), UserCode.NOT_FOUND
    );
    Result<Order> order = Results.fromOptional(
            orderRepo.findById(orderId), UserCode.ORDER_NOT_FOUND
    );
    
    // 组合验证
    return Results.zip(user, order, (u, o) -> {
        Results.ensure(Result.ok(o), 
                x -> x.getUserId().equals(u.getId()), UserCode.ORDER_NOT_BELONG
        );
        return new OrderDTO(u, o);
    });
}
```
---
**Result API**:

| 类别      | 方法                                                     | 说明                  |
|---------|--------------------------------------------------------|---------------------|
| **创建**  | `ok(value)`                                            | 创建成功结果              |
|         | `fail(code)` / `fail(code, detail)` / `fail(Business)` | 创建失败结果              |
|         | `ofNullable(value, code)`                              | null 转失败            |
|         | `fromOptional(opt, code)`                              | Optional 转 Result   |
|         | `supply(supplier, code)`                               | 捕获 Supplier 异常      |
|         | `run(runnable, code)`                                  | 捕获 Runnable 异常      |
|         | `when(condition, value, code)`                         | 条件创建                |
| **检查**  | `isSuccess()` / `isFail()`                             | 状态检查                |
|         | `exists()` / `contains(value)`                         | 存在性检查               |
| **获取**  | `get()`                                                | 获取值或抛异常             |
|         | `orNull()` / `getOrNull()`                             | 获取值或 null           |
|         | `orElse(default)` / `orElseGet(supplier)`              | 获取值或默认              |
|         | `orElseThrow()` / `orElseThrow(fn)`                    | 获取值或抛自定义异常          |
|         | `toOptional()`                                         | 转 Optional          |
| **转换**  | `map(fn)`                                              | 映射成功值               |
|         | `flatMap(fn)`                                          | 扁平映射                |
|         | `mapError(fn)` / `flatMapError(fn)`                    | 映射错误                |
|         | `filter(predicate, code)`                              | 过滤                  |
|         | `recover(fn)` / `recoverWith(fn)`                      | 错误恢复                |
|         | `fold(successFn, failFn)`                              | 双路映射                |
|         | `swap(code)`                                           | 交换成功失败              |
| **副作用** | `peek(consumer)` / `peekError(consumer)`               | 副作用消费               |
|         | `peekBoth(onSuccess, onError)`                         | 双路副作用               |
|         | `match(onSuccess, onError)`                            | 模式匹配消费              |
|         | `ifSuccess(action)` / `ifFailure(action)`              | 条件消费                |
| **组合**  | `combine(other, combiner)`                             | 组合两个 Result         |
|         | `allOf(results...)`                                    | 收集所有成功              |
| **其他**  | `stream()`                                             | 转 Stream            |
|         | `toFuture()`                                           | 转 CompletableFuture |

---
**Results API**

| 类别              | 方法                                                                                        | 说明                              |
|:----------------|:------------------------------------------------------------------------------------------|:--------------------------------|
| **异常捕获**        | `tryOf(supplier, code)` / `tryOf(supplier, code, detail)`                                 | 捕获 Supplier 异常，转为 Result        |
|                 | `tryRun(runnable, code)` / `tryRun(runnable, code, detail)`                               | 捕获 Runnable 异常，转为 Result        |
| **Optional 转换** | `fromOptional(opt, code)` / `fromOptional(opt, code, detail)`                             | Optional 转 Result，empty 时失败     |
|                 | `fromOptionalOrElse(opt, default)`                                                        | Optional 转 Result，empty 时用默认值   |
| **条件执行**        | `when(condition, supplier)`                                                               | 条件 true 时执行，false 返回 ok(null)   |
|                 | `whenOrFail(condition, value, code)` / `whenOrFail(condition, value, code, detail)`       | 条件 true 返回值，false 返回失败          |
|                 | `whenOrFail(condition, supplier, code)` / `whenOrFail(condition, supplier, code, detail)` | 条件 true 时执行 supplier，false 返回失败 |
| **批量收集**        | `sequence(results...)` / `sequence(list)`                                                 | 快速失败收集，遇错即返                     |
|                 | `sequenceAll(results...)` / `sequenceAll(list)`                                           | 全量收集，返回所有错误或所有成功                |
|                 | `partition(list)`                                                                         | 分区收集，同时返回成功和失败                  |
|                 | `successes(list)`                                                                         | 提取所有成功值                         |
|                 | `failures(list)`                                                                          | 提取所有失败                          |
| **折叠归约**        | `fold(list, identity, combiner)`                                                          | 折叠所有 Result，失败时停止               |
|                 | `reduce(list, combiner)`                                                                  | 归约所有成功值，失败时停止                   |
| **遍历映射**        | `traverse(list, mapper)`                                                                  | 遍历映射，快速失败                       |
|                 | `traverseAll(list, mapper)`                                                               | 遍历映射，全量收集                       |
|                 | `traverseIndexed(list, mapper)`                                                           | 带索引遍历，快速失败                      |
|                 | `traverseAllIndexed(list, mapper)`                                                        | 带索引遍历，全量收集                      |
| **组合操作**        | `zip(r1, r2, combiner)`                                                                   | 组合两个 Result                     |
|                 | `zip(r1, r2, r3, combiner)`                                                               | 组合三个 Result                     |
|                 | `zip(r1, r2, r3, r4, combiner)`                                                           | 组合四个 Result                     |
| **副作用**         | `tap(result, action)`                                                                     | 对 Result 执行副作用                  |
|                 | `tapSuccess(result, action)`                                                              | 成功时执行副作用                        |
|                 | `tapFailure(result, action)`                                                              | 失败时执行副作用                        |
|                 | `tapAsync(result, action)`                                                                | 异步执行副作用                         |
| **验证**          | `ensure(result, predicate, code)` / `ensure(result, predicate, code, detail)`             | 验证值，不满足则转为失败                    |
| **取值**          | `getOrNull(result)`                                                                       | 安全取值，失败返回 null                  |
| **竞争执行**        | `race(suppliers...)`                                                                      | 竞争执行，返回第一个成功或最后一个失败             |
| **重试**          | `retry(times, supplier)`                                                                  | 重试指定次数                          |
|                 | `retry(times, delay, supplier)`                                                           | 带延迟重试                           |
| **管道**          | `pipe(initial, functions...)`                                                             | 管道操作，顺序执行                       |
| **延迟执行**        | `defer(supplier)`                                                                         | 延迟执行（线程安全，懒加载）                  |
|                 | `lazy(supplier)`                                                                          | 同 defer                         |
|                 | `memoize(supplier)`                                                                       | 记忆化（非线程安全）                      |

---

## 5. 配置参考

### application.yml 完整配置

```yaml
fail-fast:
  # 调试配置
  shadow-trace: true        # 异常中包含校验点的类名与行号
  verbose: true             # 多错误响应包含详细 errors 列表

  # 错误码映射
  code-mapping:
    http-status:
      # 精确映射
      40001: 400
      40100: 401
      40300: 403
      40400: 404
      50000: 500
    groups:
      # 范围映射（支持 40100..40199 语法）
      auth: [ "40100..40199", "40300..40399" ]
      business: [ "40000..40099" ]
      system: [ "50000..59999" ]
```

---

**更多示例**: [Failure-in-Action](https://github.com/KyrieChao/Failure-in-Action)