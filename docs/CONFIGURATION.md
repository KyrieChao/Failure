## ⚙️ 配置说明

Failure 的全部配置项都在 `fail-fast` 前缀下。

在 `application.yml` 中配置（示例：全量示例，非默认值）：

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

## 配置项一览

以下默认值以代码为准（见 [FailureProperties.java](file:///d:/Work/WorkIDEA/SpringBoot/mvn/fail-fast-improved/src/main/java/com/chao/failure/config/properties/FailureProperties.java)）。

### fail-fast（顶层）

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `fail-fast.shadow-trace` | `false` | 是否打印方法名等调试信息（定位链路/调用点用）。 |
| `fail-fast.trim-stack-trace` | `true` | 是否裁剪堆栈（减少日志体积）。 |
| `fail-fast.verbose` | `false` | 是否启用详细错误响应模式；开启后错误响应会返回更多明细（如 `errors`）。 |
| `fail-fast.debug-snapshot` | `false` | 是否启用调试快照（DX-2）。 |
| `fail-fast.method-validation-enabled` | `false` | 是否启用 Spring Method Validation（开启后才会注册 `MethodValidationPostProcessor`）。 |

### fail-fast.i18n（国际化）

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `fail-fast.i18n.enabled` | `true` | 是否启用国际化。 |
| `fail-fast.i18n.default-locale` | `zh_CN` | 默认 Locale。 |
| `fail-fast.i18n.basename` | `classpath:i18n/messages` | 资源文件基路径。 |
| `fail-fast.i18n.encoding` | `UTF-8` | 资源文件编码。 |
| `fail-fast.i18n.cache-seconds` | `3600` | 资源缓存秒数。 |

### fail-fast.trace-id（TraceId）

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `fail-fast.trace-id.enabled` | `false` | 是否启用 TraceId（启用后会注册 MVC/WebFlux 的 TraceId 过滤器）。 |
| `fail-fast.trace-id.header-name` | `X-Trace-Id` | 从哪个请求头读取 TraceId。 |
| `fail-fast.trace-id.generate-if-missing` | `false` | 当请求头缺失时是否自动生成 TraceId。 |
| `fail-fast.trace-id.response-header` | `false` | 是否回写 TraceId 到响应头。 |
| `fail-fast.trace-id.response-header-name` | `X-Trace-Id` | 回写到响应头的 header 名。 |
| `fail-fast.trace-id.mdc-enabled` | `false` | 是否写入 MDC。 |
| `fail-fast.trace-id.mdc-key` | `traceId` | 写入 MDC 的 key。 |

### fail-fast.reactive（WebFlux）

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `fail-fast.reactive.context-first` | `false` | WebFlux 下是否优先从 Reactor Context 读取关键决策信息。 |

### fail-fast.logging（日志）

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `fail-fast.logging.default-severity` | `ERROR` | Business 异常默认严重级别。 |
| `fail-fast.logging.banner` | `true` | 是否打印紧凑 banner（日志展示相关）。 |
| `fail-fast.logging.severity-mapping` | `{}` | 按业务 code 映射严重级别（如 `WARN/INFO/ERROR`）。 |

### fail-fast.masking（脱敏）

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `fail-fast.masking.structured-enabled` | `false` | 是否对对象快照启用结构化脱敏。 |
| `fail-fast.masking.max-depth` | `3` | 最大递归深度。 |
| `fail-fast.masking.max-collection-size` | `20` | 最大集合元素数。 |
| `fail-fast.masking.max-fields` | `30` | 最大字段数。 |

### fail-fast.chain（链式行为）

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `fail-fast.chain.max-errors` | `50` | strict 模式最大收集错误数；非正数表示使用框架默认。 |

### fail-fast.code-mapping（错误码映射）

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `fail-fast.code-mapping.http-status` | `{}` | 错误码 -> HTTP status 映射（`Map<String, Integer>`）。 |
| `fail-fast.code-mapping.groups` | `{}` | 错误码分组（`Map<String, List<Object>>`），支持 range 字符串如 `"40100..40199"`。 |
| `fail-fast.code-mapping.constraint-mapping` | `{}` | 约束名 -> 响应码（如 `NotBlank: 40010`）。 |
| `fail-fast.code-mapping.constraint-path-mapping` | `[]` | 约束名 + path -> 响应码（列表项：`constraint/path/code`）。 |
| `fail-fast.code-mapping.constraint-bean-mapping` | `[]` | 约束名 + beanClass -> 响应码（列表项：`constraint/bean/code`）。 |

## 常见用法

### 1) 开启方法参数校验（Method Validation）

```yaml
fail-fast:
  method-validation-enabled: true
```

通常还需要引入 `spring-boot-starter-validation`，并在需要的方法/类上使用 `@Validated` 才会触发方法级校验。

### 2) TraceId 贯穿请求与日志

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

### 3) 错误码映射（HTTP 状态码、约束映射）

```yaml
fail-fast:
  code-mapping:
    http-status:
      "40010": 400
    constraint-mapping:
      NotBlank: 40010
      Email: 40020
```

建议将 `http-status` / `severity-mapping` 的 code key 写成字符串（加引号）以避免 YAML 把其当作数字导致绑定差异。
