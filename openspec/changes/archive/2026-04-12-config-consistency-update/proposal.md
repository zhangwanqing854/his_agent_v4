## 为什么

项目配置存在不一致：`backend/pom.xml` 实际已配置为 JDK 17 (`<java.version>17</java.version>`)，但多处文档和配置文件仍声明 JDK 21。此外，测试框架配置使用多选值 `"postman|pytest|restassured"`，缺乏明确选择。需同步修正以避免开发人员困惑和潜在的环境配置问题。

## 变更内容

1. **JDK 版本统一为 17**
   - 修改 `openspec/config.yaml` 第10行
   - 修改 `AGENTS.md` 第7行
   - 修改 `scripts/generate_db_design_doc.js` 第418行
   - 修改 `openspec/changes/shift-handover-phase1/design.md` 第9行

2. **API 测试框架明确化**
   - `openspec/config.yaml` 第115行：`"postman|pytest|restassured"` → `"restassured"`
   - 理由：后端为 Java Spring Boot，RestAssured 与 JUnit5 配合最佳

3. **非目标**
   - 不修改 `backend/pom.xml`（已是正确的 JDK 17）
   - 不添加新的配置项（如 Docker/CI 配置）
   - 不修改 interfaceMapping（Vue 文件映射已正确）

## 功能 (Capabilities)

### 新增功能

无 - 此变更不引入新功能，仅修正配置一致性。

### 修改功能

无 - 此变更不涉及规范级行为变更，仅是配置层面的文本修正。

## 影响

| 影响范围 | 说明 |
|---------|------|
| 配置文件 | `openspec/config.yaml` - JDK 版本、测试框架 |
| 项目文档 | `AGENTS.md` - 技术栈描述 |
| 生成脚本 | `scripts/generate_db_design_doc.js` - 文档生成模板 |
| 变更设计 | `openspec/changes/shift-handover-phase1/design.md` - 已有变更的技术栈描述 |

**与 HIS/LIS/PACS 系统对接点**: 无影响 - 此变更仅涉及内部配置，不影响外部系统对接。