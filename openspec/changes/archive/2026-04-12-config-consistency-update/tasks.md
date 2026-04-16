## 1. JDK 版本统一

- [x] 1.1 修改 `openspec/config.yaml` 第10行：`JDK 21` → `JDK 17`
- [x] 1.2 修改 `AGENTS.md` 第7行：`JDK 21` → `JDK 17`
- [x] 1.3 修改 `scripts/generate_db_design_doc.js` 第418行：`JDK 21` → `JDK 17`
- [x] 1.4 修改 `openspec/changes/shift-handover-phase1/design.md` 第9行：`JDK 21` → `JDK 17`

## 2. API 测试框架明确化

- [x] 2.1 修改 `openspec/config.yaml` 第115行：`"postman|pytest|restassured"` → `"restassured"`

## 3. 验证

- [x] 3.1 验证 `backend/pom.xml` 保持 JDK 17 配置不变
- [x] 3.2 通过 grep 搜索确认无遗漏的 JDK 21 声明
- [x] 3.3 运行 `openspec-cn status` 验证变更状态

---

**备注：**
- 所有任务属于【配置管理】模块
- 无数据库变更
- 任务预计耗时：每个任务 < 30分钟，总计约 1 小时