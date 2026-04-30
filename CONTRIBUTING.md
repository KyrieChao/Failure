# Contributing to Failure

感谢您对 Failure 项目感兴趣！我们需要您的帮助来使这个项目变得更好。

## 行为准则 (Code of Conduct)

参与本项目即表示您同意遵守我们的行为准则。请保持友善、尊重和协作。

## 如何参与 (How to Contribute)

### 1. 提交 Issue (Reporting Issues)

如果您发现了 Bug 或有新功能建议，请先搜索现有的 Issues，看看是否已经有人提出。如果没有，请创建一个新的 Issue，并包含以下信息：

- **Bug**: 复现步骤、预期行为、实际行为、环境信息 (OS, Java 版本等)。
- **Feature**: 功能描述、使用场景、预期收益。

### 2. 提交代码 (Pull Requests)

我们欢迎任何形式的代码贡献！请遵循以下步骤：

1.  **Fork** 本仓库到您的 GitHub 账户。
2.  **Clone** 代码到本地：
    ```bash
    git clone https://github.com/KyrieChao/Failure.git
    ```
3.  **创建分支**：
    ```bash
    git checkout -b feature/my-new-feature
    # 或者
    git checkout -b fix/bug-fix-description
    ```
4.  **开发与测试**：
    - 请确保您的代码符合现有的代码风格。
    - 添加或更新相应的单元测试。
    - 运行所有测试以确保没有破坏现有功能：
      ```bash
      mvn clean verify
      ```
5.  **提交更改**：
    - 使用清晰、描述性的 Commit Message。
    - 示例：`feat: 添加自定义验证器支持` 或 `fix: 修复空指针异常`。
6.  **Push** 到远程仓库：
    ```bash
    git push origin feature/my-new-feature
    ```
7.  **提交 Pull Request**：
    - 在 GitHub 上发起 PR，描述您的更改内容和目的。
    - 关联相关的 Issue (例如 `Closes #123`)。

## 开发指南 (Development Guide)

### 环境要求
- JDK 17+
- Maven 3.9+

### 代码规范
- 我们遵循标准的 Java 代码规范。
- 类和方法应有清晰的 Javadoc。
- 保持代码简洁，避免过度设计。

### 测试规范
- 单元测试覆盖率应尽量保持在 90% 以上。
- 使用 JUnit 5 和 AssertJ 进行测试编写。
- 集成测试应涵盖主要的业务场景。

## 许可证 (License)

通过提交 Pull Request，您同意您的代码将根据本项目的 [Apache License 2.0](LICENSE) 进行授权。
