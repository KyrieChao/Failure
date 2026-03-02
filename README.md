# Fail-Fast Spring Boot Starter

[![Java CI with Maven](https://github.com/KyrieChao/Failure/actions/workflows/ci.yml/badge.svg)](https://github.com/KyrieChao/Failure/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/KyrieChao/Failure/branch/main/graph/badge.svg)](https://codecov.io/gh/KyrieChao/Failure)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Java 17+](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/technologies/downloads/)
[![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Release](https://jitpack.io/v/KyrieChao/Failure.svg)](https://jitpack.io/#KyrieChao/Failure)

[English Version](./README.en.md)

Fail-Fast 是一个专为 Spring Boot 3.x 设计的轻量级、高性能参数校验与业务异常处理框架。它遵循 "Fail Fast, Fail Strict" 设计哲学，旨在消除样板代码，提供类型安全、流式调用的校验体验。

🔗 **实战示例项目**: [Failure-in-Action](https://github.com/KyrieChao/Failure-in-Action)

---

## 🚀 核心特性

- **流式校验链**: 支持 `Fail-Fast` (快速失败) 与 `Fail-Strict` (全量收集) 双模式
- **丰富的断言库**: 内置对象、字符串、数值、集合、日期时间、枚举、Optional 等 50+ 种校验方法
- **上下文集成**: 支持 `TypedValidator` 模式，将校验逻辑与业务逻辑解耦
- **注解驱动**: 提供 `@Validate` 注解与 `FastValidator` 接口，支持 AOP 切面校验
- **函数式结果**: 提供 `Result<T>` 单子类型，支持 `map`, `flatMap`, `recover` 等函数式操作
- **智能异常处理**: 自动映射业务错误码到 HTTP 状态码，支持影子追踪 (`shadow-trace`) 快速定位问题

---

## 📚 文档导航

| 文档                            | 内容                                |
| ------------------------------- | ----------------------------------- |
| [快速开始](#-快速开始)          | 安装、基础用法、三种模式入门        |
| [API 参考](./API_REFERENCE.md)  | 完整的 API 列表、方法详解、最佳实践 |
| [配置说明](#%EF%B8%8F-配置说明) | application.yml 配置项详解          |

---

## 🛠️ 快速开始

### 环境要求

- JDK 17+
- Spring Boot 3.2.x+

### 引入依赖

本项目发布在 JitPack，请在 `pom.xml` 中添加：

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.KyrieChao</groupId>
    <artifactId>Failure</artifactId>
    <version>1.3.1</version>
</dependency>
```

---

## 💡 三种校验模式

### 模式一：Fail-Fast（快速失败）

**适用场景**: 参数防御性编程，一旦发现非法参数立即停止后续逻辑。

```java
// 一旦 notBlank 失败，立即抛出异常，不会执行后续校验
Failure.begin()
    .notBlank(username, UserCode.USERNAME_REQUIRED)
    .email(email, UserCode.EMAIL_INVALID)
    .fail();
```
```java
Failure.begin()
    .notBlank(username)
    .notNull(email)
    .failNow(UserCode.REQUIRED)
    .phone(phone)
    .email(email)
    .failNow(UserCode.INVALID);
```


**终结方法对比**:

| 方法                      | 说明                 |
| ------------------------- |--------------------|
| `.fail()`                 | 标准终结方法，有错误时抛出第一个异常 |
| `.failNow(code, message)` | 多数时候用于**分组校验**不用重写错误码 |

```java
// 强制失败示例：权限检查
Failure.begin()
    .notNull(user, UserCode.USER_NOT_FOUND)
    .failNow(UserCode.PERMISSION_DENIED, "当前角色无权访问")  // 直接抛出，后续不执行
    .state(user.getRole() == Role.ADMIN, UserCode.PERMISSION_DENIED)  // 不会执行
    .fail();
```

---

## 🎛️ 流程控制与延迟校验

### 动态跳过 (when)

根据条件动态决定是否执行后续的校验逻辑。

```java
Failure.begin()
    .when(isVip)                // 如果不是 VIP
    .check(vipRule)             // 这一行会被跳过
    .when(true)                 // 恢复执行
    .check(commonRule);         // 继续执行
```

### 延迟校验 (defer)

仅在真正需要时才执行开销较大的校验逻辑（支持 Supplier）。如果前面的校验已经失败（Fail-Fast）或被跳过，则不会执行。

```java
Failure.begin()
    .notNull(userId)
    // 只有 userId 不为 null 时，才会执行数据库查询
    .defer(() -> dbService.isUserActive(userId), UserCode.USER_INACTIVE);
```

---

### 模式二：Fail-Strict（全量收集）

**适用场景**: 表单提交、批量导入等需要一次性返回所有错误的场景。

```java
// 所有校验都会执行，最终收集所有错误统一抛出
Failure.strict()
    .notBlank(username, UserCode.USERNAME_REQUIRED, "用户名不能为空")
    .email(email, UserCode.EMAIL_INVALID, "邮箱格式不正确")
    .min(age, 18, UserCode.AGE_TOO_YOUNG, "年龄必须 ≥ 18 岁")
    .failAll();  // 必须配合 failAll() 使用
```

**手动获取错误（不抛异常）**:

```java
var chain = Failure.strict()
    .notBlank(username, UserCode.USERNAME_REQUIRED)
    .email(email, UserCode.EMAIL_INVALID);

if (!chain.isValid()) {
    var causes = chain.getCauses();  // 获取所有错误
    return Result.fail("参数校验失败", causes);
}
```

---

### 模式三：Contextual（上下文集成）

**适用场景**: 结合 `@Validate` 注解，将校验逻辑从业务代码中解耦。

```java
// Controller
@PostMapping("/register")
@Validate(value = UserRegisterValidator.class, fast = false)  // fast=false 全量收集
public Result<?> register(@RequestBody UserRegisterDTO dto) {
    userService.register(dto);
    return Result.success("注册成功");
}

// Validator
@Component
public class UserRegisterValidator implements FastValidator<UserRegisterDTO> {
    @Override
    public void validate(UserRegisterDTO dto, ValidationContext ctx) {
        Failure.with(ctx)
            .notBlank(dto.getUsername(), UserCode.USERNAME_REQUIRED)
            .email(dto.getEmail(), UserCode.EMAIL_INVALID)
            .verify();  // Contextual 模式使用 verify() 终结
    }

    @Override
    public Class<?> getSupportedType() {
        return UserRegisterDTO.class;
    }
}
```

**@Validate 的 fast 参数**:

| fast 值       | 行为                 | 适用场景         |
| ------------- | -------------------- | ---------------- |
| `true` (默认) | 第一个错误后立即停止 | 性能优先         |
| `false`       | 执行所有校验规则     | 需要展示所有错误 |

---

## 🔀 逻辑运算 (OR)

支持 `or()` 逻辑操作符，用于表达 "满足条件A 或 满足条件B" 的场景。

```java
// 示例：用户或者是管理员，或者是拥有特定权限的普通用户
Failure.begin()
    .equals(role, Role.ADMIN)       // 条件A：是管理员
    .or()                           // 或
    .hasPermission(user, "READ")    // 条件B：拥有读权限
    .failNow(UserCode.NO_PERMISSION); // 如果A和B都不满足，则抛出异常
```

注意：`or()` 仅作用于其紧邻的两个条件。链式调用的默认逻辑为 `AND`。
`A.or().B.C` 等价于 `(A || B) && C`。

---

## ⚙️ 配置说明

在 `application.yml` 中配置：

```yaml
fail-fast:
  shadow-trace: true   # 异常中包含校验点的类名与行号（调试推荐开启）
  verbose: true        # 多错误响应是否包含详细的 errors 列表
  code-mapping:
    http-status:
      40001: 400       # 错误码 40001 -> HTTP 400
      40100: 401
    groups:
      auth: ["40100..40199"]      # 范围映射
      business: ["40000..40099"]
```

---

## 📖 更多文档

- **[API_REFERENCE.md](./API_REFERENCE.md)** - 完整的 API 参考、所有校验方法列表、设计模式详解
- **[Failure-in-Action](https://github.com/KyrieChao/Failure-in-Action)** - 实战示例项目

---

## 🤝 贡献指南

欢迎提交 Issue 或 Pull Request！请确保：

- 运行 `mvn test` 通过所有测试
- 代码覆盖率保持在 99%+
- 遵循现有代码风格

## 📄 许可证

Apache License 2.0 - 详见 [LICENSE](LICENSE) 文件。

---

**Author**: [KyrieChao](https://github.com/KyrieChao)