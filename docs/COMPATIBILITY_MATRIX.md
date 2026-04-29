# 兼容性矩阵（Compatibility Matrix）

## 运行环境

| 维度 | 支持范围 |
|---|---|
| Java | 17+ |
| Spring Boot | 3.2.x+ |

## Web 栈

| 场景 | 支持 |
|---|---|
| Spring MVC（Servlet） | ✅ 内置统一异常响应（默认启用） |
| Spring WebFlux（Reactive） | ✅ 内置统一异常响应（默认启用） |

## 说明

- 本仓库 `pom.xml` 以 Spring Boot 3.2.0 作为 parent 进行编译与测试。
- Web / WebFlux / Validation / AOP 依赖均为可选项：只在你的应用引入对应 starter 时才会生效。

