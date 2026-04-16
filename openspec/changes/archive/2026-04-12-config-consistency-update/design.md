## 上下文

### 当前状态

项目存在配置不一致问题：
- `backend/pom.xml` 已配置 `<java.version>17</java.version>`
- 但文档和配置文件声明 JDK 21，造成混淆

### 约束

- Spring Boot 3.x 完全支持 JDK 17（最低要求 JDK 17）
- 不涉及代码变更，仅是文本修正
- 需保持其他配置不变（interfaceMapping、路径配置等）

### 利益相关者

- 开发人员：依赖准确的配置文档设置开发环境
- CI/CD 系统：读取配置确定构建参数

## 目标 / 非目标

**目标：**
- 统一所有文档/配置中 JDK 版本声明为 17
- 明确 API 测试框架为 RestAssured
- 保持现有功能配置不变

**非目标：**
- 不修改 `backend/pom.xml`（已是正确的 JDK 17）
- 不添加 Dockerfile 或 CI 配置
- 不修改前端配置
- 不引入新的测试框架依赖（RestAssured 已在 pom.xml 中通过 `spring-boot-starter-test` 包含）

## 决策

### 1. JDK 版本选择：17 而非 21

**决策：** 将所有声明统一为 JDK 17

**理由：**
- pom.xml 已是 JDK 17，修改文档比修改代码风险更低
- Spring Boot 3.x LTS 对 JDK 17 支持稳定
- JDK 21 的虚拟线程等特性项目未使用，无依赖

**替代方案考虑：**
- 改 pom.xml 为 JDK 21：风险更高，需验证兼容性
- 保持不一致：不可接受，会导致开发环境混乱

### 2. API 测试框架选择：RestAssured

**决策：** 明确为 `restassured`

**理由：**
- 后端是 Java Spring Boot 项目
- RestAssured 与 JUnit5 无缝集成
- 无需额外环境（Postman 需 Newman CLI，pytest 需 Python）

**替代方案考虑：**
- Postman：需要 Node.js + Newman，环境复杂度高
- pytest：跨语言框架，与 Java 项目风格不一致

### 3. 修改范围

**决策：** 仅修改不一致之处，不扩展范围

**理由：**
- 变更风险最小化原则
- interfaceMapping 已验证正确（Vue 文件存在）
- 扩展配置（Docker/CI）属于未来迭代

## 风险 / 权衡

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 修改遗漏 | 部分文件仍声明 JDK 21 | 通过 grep 全量搜索验证 |
| 验证不充分 | 修改后配置不生效 | 运行 `openspec-cn status` 验证 |

**权衡：**
- 简单文本修改 → 低风险，无需测试
- 不添加 Docker/CI 配置 → 可能需要后续补充，但当前变更保持聚焦