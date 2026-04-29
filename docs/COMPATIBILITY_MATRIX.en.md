# Compatibility Matrix

## Runtime

| Dimension | Supported |
|---|---|
| Java | 17+ |
| Spring Boot | 3.2.x+ |

## Web Stack

| Scenario | Supported |
|---|---|
| Spring MVC (Servlet) | ✅ Built-in unified error response (enabled by default) |
| Spring WebFlux (Reactive) | ✅ Built-in unified error response (enabled by default) |

## Notes

- This repository compiles/tests against Spring Boot 3.2.0 (as the parent in `pom.xml`).
- Web / WebFlux / Validation / AOP dependencies are optional and only take effect when your application includes the corresponding starter.

