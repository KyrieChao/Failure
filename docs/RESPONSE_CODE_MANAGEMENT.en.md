# Response Code Management

## 1. Response Code Design

### 1.1 Response Code Structure

Response codes use a numeric structure, recommended to use 5-digit format: `[HTTP Status Code Prefix][Specific Error Code]`

- **HTTP Status Code Prefix** (first 3 digits): Determines the HTTP response status code
  - `400`: Client error → HTTP 400 Bad Request
  - `500`: Server error → HTTP 500 Internal Server Error
  - Other prefixes like `401`, `403`, `404` can also be used

- **Specific Error Code** (last 2 or more digits): Used to distinguish specific error scenarios
  - `40010`: User not found
  - `40011`: Password is blank
  - `40012`: Invalid password

### 1.2 Response Code Examples

| Response Code | HTTP Status Code | Meaning                |
|--------------|-----------------|------------------------|
| 40010        | 400             | User not found         |
| 40011        | 400             | Password is blank      |
| 40012        | 400             | Invalid password       |
| 50001        | 500             | Internal server error  |

## 2. Response Code Implementation

### 2.1 Implementing ResponseCode Interface with Enums

Using enums to implement the ResponseCode interface is a concise and type-safe approach:

```java
public enum RequestCode implements ResponseCode {
    // Request object cannot be null
    REQUEST_OBJECT_NOT_NULL(10001, "Request object cannot be null", "Request object cannot be null");

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
    USERNAME_EXIST(40010, "Username already exists", "This username has been registered"),
    EMAIL_EXIST(40011, "Email already exists", "This email has been registered"),
    PHONE_EXIST(40012, "Phone number already exists", "This phone number has been registered"),
    GENDER_UNKNOWN(40013, "Invalid gender value", "Gender can only be 0(unknown), 1(male), 2(female)"),
    USERNAME_BLANK(40014, "Username cannot be blank", "Username field is required and cannot be empty"),
    NICKNAME_BLANK(40015, "Nickname cannot be blank", "Nickname field is required and cannot be empty"),
    EMAIL_INVALID(40016, "Invalid email format", "Email does not meet standard format"),
    EMAIL_BLANK(40017, "Email cannot be blank", "Email field is required and cannot be empty"),
    PHONE_INVALID(40018, "Invalid phone number format", "Phone number does not meet rules (recommended 11 digits starting with 1)"),
    STATUS_DISABLED(40019, "Invalid user status value", "Status can only be 0(disabled), 1(active), 2(locked)"),
    BIRTHDAY_INVALID(40020, "Invalid birthday format or value", "Birthday date is invalid or in the future"),
    USER_NOT_FOUND(40021, "User not found or password error", "No matching user found for the account or password is incorrect"),
    USER_NOT_LOGIN(40022, "Please login first", "Current request requires login status"),
    PASSWORD_BLANK(40023, "Password cannot be blank", "Password field is required"),
    NO_AUTHORITY(40024, "No operation permission", "Current user has no permission to perform this operation");

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

### 2.2 Advantages

- **Type Safety**: Enums provide compile-time type checking
- **Easy Management**: Grouped by business modules, clear structure
- **Readability**: Enum constant names intuitively express error meanings
- **Easy Expansion**: New response codes can be added at any time

## 3. HTTP Status Code Mapping Rules

### 3.1 Mapping Principles

According to the implementation of `CodeMappingConfig`, the HTTP status code mapping rules are as follows:

1. **Direct Mapping**: If the response code is between 100-599, use it directly as the HTTP status code
2. **Exact Match**: Check if there is an exact mapping for this response code in the configuration
3. **Range Match**: If no exact match, take the first three digits of the response code (e.g., 400 for 40012) to find range mapping
4. **Default Mapping**: If none of the above matches, 40000-49999 returns HTTP 400 Bad Request, others return HTTP 500 Internal Server Error

### 3.2 Why internal code and HTTP Status are inconsistent?

HTTP status codes are used to indicate the overall status of the request, while internal codes are used to indicate specific business error scenarios. They have different responsibilities:

- **HTTP 400 + code 400**: Annotation validation (JSR-303) - Client error, general validation error
- **HTTP 500 + code 500**: Aggregated error collection - Server error, multiple errors aggregated
- **HTTP 400 + code 400XX**: Programmatic validation (specific business error)

### 3.3 Where does the 12 in 40012 come from?

40012 is a 5-digit response code where:
- `400`: Indicates client error, corresponding to HTTP 400 Bad Request
- `12`: Indicates the specific error scenario (determined by the user)

This is an encoding convention used to distinguish different business error scenarios while maintaining consistent HTTP status codes.

### 3.4 Configuration Support

In `application.yml`, you can customize the mapping from response codes to HTTP status codes using numeric HTTP status codes:

```yaml
fail-fast:
  code-mapping:
    http-status:
      40001: 400
      40101: 401
      40301: 403
      40401: 404
      42201: 422
      42901: 429
      50001: 500
```

### 3.5 Mapping Examples

| Response Code | Mapping Process                | HTTP Status Code |
|--------------|--------------------------------|-----------------|
| 400          | Direct mapping                 | 400             |
| 500          | Direct mapping                 | 500             |
| 40012        | Range matching (400)           | 400             |
| 50001        | Range matching (500)           | 500             |
| 60000        | Default mapping (non 40000-49999) | 500           |

## 4. Frontend Processing Strategy

### 4.1 Processing Flow

1. **First determine the HTTP status code**:
   - `400`: Client error, need to parse the `code` field for detailed processing
   - `500`: Server error, may be a single error or aggregated error

2. **Parse the `code` field in the response body**:
   - Display different error prompts based on the specific value of `code`
   - For aggregated errors, check the `errors` field for detailed information

### 4.2 Frontend Example Code

```javascript
fetch('/api/user/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ username: '', password: '' })
})
.then(response => {
  if (response.status === 400) {
    return response.json().then(data => {
      switch(data.code) {
        case 40012:
          showError('Password cannot be blank');
          break;
        case 40013:
          showError('User not found or password error');
          break;
        default:
          showError('Parameter error');
      }
    });
  } else if (response.status === 500) {
    return response.json().then(data => {
      showError('System error, please try again later');
      console.error(data);
    });
  }
})
.catch(error => {
  showError('Network error');
});
```

## 5. Usage Examples

### 5.1 Programmatic Validation

```java
// Password blank validation
if (password == null || password.isEmpty()) {
  throw Business.of(
    UserCode.PASSWORD_BLANK,
    "Password cannot be blank"
  );
}

// Username existence validation
if (userService.existsByUsername(username)) {
  throw Business.of(
    UserCode.USERNAME_EXIST,
    "Username already exists"
  );
}
```

### 5.2 Annotation Validation (JSR-303)

```java
public class UserDTO {
  @NotNull(message = "40014:Username cannot be blank")
  private String username;
  
  @NotNull(message = "40023:Password cannot be blank")
  @Size(min = 6, message = "Password length cannot be less than 6 digits")
  private String password;
  
  @NotNull(message = "40017:Email cannot be blank")
  @Email(message = "40016:Invalid email format")
  private String email;
}
```

### 5.3 Aggregated Error Handling

```java
List<Business> errors = new ArrayList<>();
if (username == null || username.isEmpty()) {
  errors.add(Business.of(UserCode.USERNAME_BLANK, "Username cannot be blank"));
}
if (password == null || password.isEmpty()) {
  errors.add(Business.of(UserCode.PASSWORD_BLANK, "Password cannot be blank"));
}
if (email == null || email.isEmpty()) {
  errors.add(Business.of(UserCode.EMAIL_BLANK, "Email cannot be blank"));
}
if (!errors.isEmpty()) {
  throw new MultiBusiness(errors); // Returns HTTP 500
}
```

## 6. Best Practices

1. **Group by Module**: Use different enum classes to group response codes by business modules
2. **Naming Conventions**: Enum constant names should intuitively express error meanings
3. **Unified Management**: Centralize all response codes to avoid scattered definitions
4. **Clear Semantics**: Response code meanings should be clear and unambiguous
5. **Frontend-Backend Consistency**: Frontend and backend use the same response code definitions
6. **Flexible Configuration**: Customize response code mapping through configuration files
7. **Detailed Logging**: Record response codes and error information for easy troubleshooting

### TraceId Logging (Recommended)

Enable `fail-fast.trace-id.mdc-enabled=true` and print TraceId via MDC (example):

```yaml
logging:
  pattern:
    console: "%clr(%d{${LOG_DATEFORMAT_PATTERN:yyyy-MM-dd'T'HH:mm:ss.SSSXXX}}){faint} %clr(${LOG_LEVEL_PATTERN:%5p}) %clr(${PID:-}){magenta} %clr([${spring.application.name:-}]){faint} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} %clr(:){faint} %m%replace([%X{traceId:-}]){'^\\\\[\\\\]$',''}%n${LOG_EXCEPTION_CONVERSION_WORD:%wEx}"
```

## 7. Summary

This response code management scheme provides a concise, type-safe response code management mechanism by implementing the ResponseCode interface with enums. Using enums allows response codes to be grouped by business modules, improving code readability and maintainability. At the same time, it supports custom mapping from response codes to HTTP status codes through configuration files, providing a unified error handling standard for frontend and backend.

This approach avoids the burden of memorizing complex coding rules, and developers can directly use enum constants to work with response codes, improving development efficiency and code quality.
