# 响应码管理方案

## 1. 响应码设计

### 1.1 响应码结构

响应码采用数字结构，推荐使用5位数字格式：`[HTTP状态码前三位][具体错误码]`

- **HTTP状态码前三位**：决定HTTP响应状态码
  - `400`：客户端错误 → HTTP 400 Bad Request
  - `500`：服务器错误 → HTTP 500 Internal Server Error
  - 其他如`401`、`403`、`404`等也可使用

- **具体错误码**：后两位或更多位，用于区分具体错误场景
  - `40010`：用户不存在
  - `40011`：密码为空
  - `40012`：密码无效

### 1.2 响应码示例

| 响应码   | HTTP状态码 | 含义                |
|-------|---------|-------------------|
| 40010 | 400     | 用户不存在            |
| 40011 | 400     | 密码为空             |
| 40012 | 400     | 密码无效             |
| 50001 | 500     | 系统内部错误           |

## 2. 响应码实现

### 2.1 通过枚举实现 ResponseCode 接口

使用枚举来实现 ResponseCode 接口，是一种简洁且类型安全的方式：

```java
public enum RequestCode implements ResponseCode {
    // 请求对象不能为空
    REQUEST_OBJECT_NOT_NULL(10001, "Request object cannot be null", "请求对象不能为空");

    RequestCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    private final int code;
    private final String message;
    private final String description;

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

public enum UserCode implements ResponseCode {
    USERNAME_EXIST(40010, "用户名已存在", "该用户名已被注册"),
    EMAIL_EXIST(40011, "邮箱已存在", "该邮箱已被注册"),
    PHONE_EXIST(40012, "手机号已存在", "该手机号已被注册"),
    GENDER_UNKNOWN(40013, "性别值不合法", "性别只能为 0(未知)、1(男)、2(女)"),
    USERNAME_BLANK(40014, "用户名不能为空", "用户名字段必填且不可为空字符串"),
    NICKNAME_BLANK(40015, "昵称不能为空", "昵称字段必填且不可为空字符串"),
    EMAIL_INVALID(40016, "邮箱格式不正确", "邮箱不符合标准格式"),
    EMAIL_BLANK(40017, "邮箱不能为空", "邮箱字段必填且不可为空字符串"),
    PHONE_INVALID(40018, "手机号格式不正确", "手机号不符合规则（建议11位数字，以1开头）"),
    STATUS_DISABLED(40019, "用户状态值不合法", "状态只能为 0(禁用)、1(正常)、2(锁定)"),
    BIRTHDAY_INVALID(40020, "生日格式或值不合法", "生日日期无效或为未来时间"),
    USER_NOT_FOUND(40021, "用户不存在或密码错误", "根据账号未找到匹配用户或密码不正确"),
    USER_NOT_LOGIN(40022, "请先登录", "当前请求需要登录状态"),
    PASSWORD_BLANK(40023, "密码不能为空", "密码字段必填"),
    NO_AUTHORITY(40024, "无操作权限", "当前用户无权执行该操作");

    UserCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    private final int code;
    private final String message;
    private final String description;

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

### 2.2 优点

- **类型安全**：枚举提供了编译时类型检查
- **易于管理**：按业务模块分组，结构清晰
- **可读性强**：枚举常量名称直观表达错误含义
- **易于扩展**：可以随时添加新的响应码

## 3. HTTP状态码映射规则

### 3.1 映射原则

根据 `CodeMappingConfig` 的实现，HTTP状态码映射规则如下：

1. **直接映射**：如果响应码在 100-599 之间，直接使用该码作为HTTP状态码
2. **精确匹配**：查找配置中是否有该响应码的精确映射
3. **范围匹配**：如果没有精确匹配，取响应码的前三位（如40012取400）查找范围映射
4. **默认映射**：如果以上都没有匹配，40000-49999返回 HTTP 400 Bad Request，其他返回 HTTP 500 Internal Server Error

### 3.2 为什么内部code和HTTP Status不一致？

HTTP状态码用于表示请求的整体状态，而内部code用于表示具体的业务错误场景，两者职责不同：

- **HTTP 400 + code 400**：注解校验（JSR-303）- 客户端错误，通用验证错误
- **HTTP 500 + code 500**：全收集聚合错误 - 服务器错误，多个错误聚合
- **HTTP 400 + code 400XX**：编程式校验（具体业务错误）

### 3.3 40012的12从哪来？


40012 是一个5位响应码，其中：
- `400`：表示客户端错误，对应HTTP 400 Bad Request
- `12`：表示具体的错误场景 由用户自己决定

这是一种编码约定，用于在保持HTTP状态码一致的同时，区分不同的业务错误场景。

### 3.4 配置支持

在 `application.yml` 中可以自定义响应码到HTTP状态码的映射，使用数字形式的HTTP状态码：

```yaml
failure:
  code-mapping:
    http-status:
      40001: 400  # BAD_REQUEST
      40101: 401  # UNAUTHORIZED
      40301: 403  # FORBIDDEN
      40401: 404  # NOT_FOUND
      42201: 422  # UNPROCESSABLE_ENTITY
      42901: 429  # TOO_MANY_REQUESTS
      50001: 500  # INTERNAL_SERVER_ERROR
```

### 3.5 映射示例

| 响应码   | 映射过程                | HTTP状态码 |
|-------|---------------------|---------|
| 400   | 直接映射                | 400     |
| 500   | 直接映射                | 500     |
| 40012 | 范围匹配（400）          | 400     |
| 50001 | 范围匹配（500）          | 500     |
| 60000 | 默认映射（非40000-49999）   | 500     |

## 4. 前端处理策略

### 4.1 处理流程

1. **首先判断HTTP状态码**：
   - `400`：客户端错误，需要解析 `code` 字段做精细化处理
   - `500`：服务器错误，可能是单个错误或聚合错误

2. **解析响应体中的 `code