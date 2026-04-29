## ⚙️ Configuration

All Failure properties are under the `fail-fast` prefix.

Configure in `application.yml` (example: full example, not defaults):

```yaml
fail-fast:
  shadow-trace: true
  trim-stack-trace: true
  verbose: true
  debug-snapshot: true
  method-validation-enabled: true
  code-mapping:
    http-status:
      40010: 400
    groups:
      auth: [ "40100..40199" ]
      business: [ "40000..40099" ]
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
        code: 40040
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
  reactive:
    context-first: true
  trace-id:
    enabled: true
    header-name: X-Trace-Id
    response-header-name: X-Trace-Id
    response-header: true
    generate-if-missing: true
    mdc-key: traceId
    mdc-enabled: true
  i18n:
    enabled: true
    default-locale: zh_CN
    basename: classpath:i18n/messages
    encoding: UTF-8
    cache-seconds: 3600
  logging:
    default-severity: ERROR
    banner: false
    severity-mapping:
      40010: WARN
      40020: INFO
  masking:
    structured-enabled: true
    max-depth: 4
    max-collection-size: 20
    max-fields: 30
  chain:
    max-errors: 50
```

## Property Reference

Defaults are defined in code (see [FailureProperties.java](file:///d:/Work/WorkIDEA/SpringBoot/mvn/fail-fast-improved/src/main/java/com/chao/failure/config/properties/FailureProperties.java)).

### fail-fast (Top-level)

| Property | Default | Description |
| --- | --- | --- |
| `fail-fast.shadow-trace` | `false` | Whether to include method/location info for debugging. |
| `fail-fast.trim-stack-trace` | `true` | Whether to trim stack traces (reduce log size). |
| `fail-fast.verbose` | `false` | Verbose error response mode; when enabled, error responses include more details (such as `errors`). |
| `fail-fast.debug-snapshot` | `false` | Enable debug snapshot (DX-2). |
| `fail-fast.method-validation-enabled` | `false` | Enable Spring Method Validation (registers `MethodValidationPostProcessor`). |

### fail-fast.i18n (Internationalization)

| Property | Default | Description |
| --- | --- | --- |
| `fail-fast.i18n.enabled` | `true` | Enable i18n support. |
| `fail-fast.i18n.default-locale` | `zh_CN` | Default locale. |
| `fail-fast.i18n.basename` | `classpath:i18n/messages` | Message bundle basename. |
| `fail-fast.i18n.encoding` | `UTF-8` | Message source encoding. |
| `fail-fast.i18n.cache-seconds` | `3600` | Message source cache seconds. |

### fail-fast.trace-id (TraceId)

| Property | Default | Description |
| --- | --- | --- |
| `fail-fast.trace-id.enabled` | `false` | Enable TraceId support (registers MVC/WebFlux TraceId filter). |
| `fail-fast.trace-id.header-name` | `X-Trace-Id` | Request header name for TraceId. |
| `fail-fast.trace-id.generate-if-missing` | `false` | Generate TraceId when missing from request header. |
| `fail-fast.trace-id.response-header` | `false` | Write TraceId into response header. |
| `fail-fast.trace-id.response-header-name` | `X-Trace-Id` | Response header name for TraceId. |
| `fail-fast.trace-id.mdc-enabled` | `false` | Put TraceId into MDC. |
| `fail-fast.trace-id.mdc-key` | `traceId` | MDC key name for TraceId. |

### fail-fast.reactive (WebFlux)

| Property | Default | Description |
| --- | --- | --- |
| `fail-fast.reactive.context-first` | `false` | In WebFlux, prefer reading key decisions from Reactor Context before ThreadLocal. |

### fail-fast.logging (Logging)

| Property | Default | Description |
| --- | --- | --- |
| `fail-fast.logging.default-severity` | `ERROR` | Default severity for `Business` exceptions. |
| `fail-fast.logging.banner` | `true` | Whether to print compact banner (log presentation). |
| `fail-fast.logging.severity-mapping` | `{}` | Map business code to severity (e.g. `WARN/INFO/ERROR`). |

### fail-fast.masking (Masking)

| Property | Default | Description |
| --- | --- | --- |
| `fail-fast.masking.structured-enabled` | `false` | Enable structured masking for object snapshots. |
| `fail-fast.masking.max-depth` | `3` | Max recursion depth. |
| `fail-fast.masking.max-collection-size` | `20` | Max collection entries. |
| `fail-fast.masking.max-fields` | `30` | Max object fields. |

### fail-fast.chain (Chain Behavior)

| Property | Default | Description |
| --- | --- | --- |
| `fail-fast.chain.max-errors` | `50` | Max errors collected in strict mode; non-positive means framework default. |

### fail-fast.code-mapping (Code Mapping)

| Property | Default | Description |
| --- | --- | --- |
| `fail-fast.code-mapping.http-status` | `{}` | Error code -> HTTP status mapping (`Map<String, Integer>`). |
| `fail-fast.code-mapping.groups` | `{}` | Group name -> list of codes/ranges (`Map<String, List<Object>>`), supports range strings like `"40100..40199"`. |
| `fail-fast.code-mapping.constraint-mapping` | `{}` | Constraint name -> response code (e.g. `NotBlank: 40010`). |
| `fail-fast.code-mapping.constraint-path-mapping` | `[]` | Constraint + path -> response code (items: `constraint/path/code`). |
| `fail-fast.code-mapping.constraint-bean-mapping` | `[]` | Constraint + bean class -> response code (items: `constraint/bean/code`). |

## Common Recipes

### 1) Enable Method Validation

```yaml
fail-fast:
  method-validation-enabled: true
```

Typically you also need `spring-boot-starter-validation`, and add `@Validated` on the target class/method.

### 2) TraceId across request and logs

```yaml
fail-fast:
  trace-id:
    enabled: true
    header-name: X-Trace-Id
    generate-if-missing: true
    response-header: true
    response-header-name: X-Trace-Id
    mdc-enabled: true
    mdc-key: traceId
```

### 3) Map codes to HTTP status / constraints

```yaml
fail-fast:
  code-mapping:
    http-status:
      "40010": 400
    constraint-mapping:
      NotBlank: 40010
      Email: 40020
```

It is recommended to quote the code keys in `http-status` / `severity-mapping` to avoid YAML numeric parsing differences during binding.
