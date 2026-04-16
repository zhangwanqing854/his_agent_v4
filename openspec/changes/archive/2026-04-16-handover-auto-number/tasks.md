## 1. 数据库准备

- [x] 1.1 在 shift_handover 表添加 handover_no 列（VARCHAR(10) UNIQUE）
- [x] 1.2 编写 SQL 脚本并执行

## 2. 编号生成服务（后端）

- [x] 2.1 创建 HandoverNoGenerator 服务类
- [x] 2.2 实现日期格式化方法（YYMMDD）
- [x] 2.3 实现当日最大序号查询方法（带锁）
- [x] 2.4 实现编号生成方法（prefix + 序号）
- [x] 2.5 实现重试机制（最多3次）

## 3. 实体和 DTO 更新（后端）

- [x] 3.1 在 ShiftHandover 实体添加 handoverNo 字段
- [x] 3.2 在 HandoverDto 添加 handoverNo 字段
- [x] 3.3 更新 toHandoverDto 方法映射 handoverNo

## 4. 交班创建集成（后端）

- [x] 4.1 在 HandoverService.createHandover 调用编号生成
- [x] 4.2 设置 handoverNo 到新交班记录
- [x] 4.3 处理编号生成失败异常

## 5. 前端展示

- [x] 5.1 在交班列表显示编号列
- [x] 5.2 在交班详情页显示编号
- [x] 5.3 更新 API 类型定义（handoverNo 字段）

## 6. 测试验证

- [x] 6.1 单元测试：编号格式验证
- [x] 6.2 单元测试：序号递增逻辑
- [x] 6.3 单元测试：跨日序号重置
- [x] 6.4 集成测试：创建交班自动编号
- [x] 6.5 功能验证：前端显示编号