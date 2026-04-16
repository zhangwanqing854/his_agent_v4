## 1. 修复查看按钮事件处理

- [x] 1.1 移除查看按钮的 `.stop.prevent` 修饰符，改为 `@click="handleView(row)"
- [x] 1.2 添加 viewLoading 状态变量，用于显示加载状态
- [x] 1.3 优化 handleView 函数，添加 try-catch-finally 和错误提示
- [x] 1.4 在弹窗患者列表区域添加 v-loading 指令

## 2. 优化弹窗显示逻辑

- [x] 2.1 确认 el-dialog 的 append-to-body 属性正确配置
- [x] 2.2 检查弹窗样式，确保无 z-index 或 display:none 问题
- [x] 2.3 添加调试日志确认事件触发

## 3. 验证和测试

- [x] 3.1 运行前端类型检查 (npm run type-check)
- [ ] 3.2 启动开发服务器，测试查看按钮点击响应
- [ ] 3.3 测试弹窗显示交班详情和患者列表
- [ ] 3.4 测试数据加载失败时的错误提示