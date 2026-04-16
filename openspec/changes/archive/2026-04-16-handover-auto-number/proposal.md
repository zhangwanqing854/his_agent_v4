## 为什么

交班记录目前没有唯一编号，难以追溯和引用。在医疗场景中，交班编号是重要的标识，用于病历归档、审计追溯和跨系统对接。缺少编号会导致查询困难、无法快速定位特定交班记录。

## 变更内容

1. 在 `shift_handover` 表添加 `handover_no` 字段存储交班编号
2. 编码规则：年份后两位 + 月份 + 天 + 自增序号（例：`260415001` 表示 2026年4月15日第1号）
3. 创建交班时自动生成编号，确保全局唯一
4. 编号生成采用日期+序号机制，同一天内序号递增

## 功能 (Capabilities)

### 新增功能
- `handover-number-generation`: 交班编号自动生成服务，支持按日期生成唯一编号

### 修改功能
- `handover-creation`: 创建交班时需自动生成并设置编号

## 影响

### 后端
- `ShiftHandover.java` - 添加 `handoverNo` 字段
- `HandoverService.java` - 创建交班时生成编号
- 新增 `HandoverNoGenerator.java` - 编号生成服务
- 数据库 - `shift_handover` 表添加 `handover_no` 列

### 前端
- 交班列表显示编号
- 交班详情显示编号

### API
- `HandoverDto` - 添加 `handoverNo` 字段

## 非目标

- 不支持手动指定编号
- 不支持修改已有编号
- 不实现编号回收/重用机制