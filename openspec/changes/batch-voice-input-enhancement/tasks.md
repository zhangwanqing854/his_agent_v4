## 1. 组件重构 - VoiceInputDialog

- [x] 1.1 在VoiceInputDialog.vue添加模式切换按钮（单人/批量）
- [x] 1.2 添加批量模式状态管理（status: 'recording' | 'paused' | 'matching' | 'preview'）
- [x] 1.3 重构录音逻辑，支持暂停/继续/取消操作
- [ ] 1.4 优化实时转写显示，使用debounce限制更新频率

## 2. 语音解析功能

- [x] 2.1 创建voiceParser.ts工具，实现正则表达式解析床号/姓名/病情
- [x] 2.2 支持标准格式解析（如"2床张三，病情稳定"）
- [x] 2.3 支持非标准格式解析（仅床号、仅姓名等）
- [x] 2.4 添加解析结果类型定义（ParsedVoiceItem）

## 3. 患者匹配功能

- [x] 3.1 创建patientMatcher.ts工具，实现床号/姓名匹配逻辑
- [x] 3.2 处理精确匹配场景
- [x] 3.3 处理同名患者匹配冲突场景
- [x] 3.4 处理未匹配场景，返回候选患者列表

## 4. 批量匹配预览组件

- [x] 4.1 创建BatchMatchPreview.vue子组件（集成到VoiceInputDialog.vue）
- [x] 4.2 显示解析条目列表及其匹配状态
- [x] 4.3 实现未匹配条目的患者选择下拉框
- [x] 4.4 实现同名患者冲突的选择下拉框
- [x] 4.5 实现确认按钮，批量填充病情到患者

## 5. CreateHandoverPage集成

- [x] 5.1 在CreateHandoverPage.vue集成批量语音录入入口
- [x] 5.2 传递当前科室患者列表给VoiceInputDialog
- [x] 5.3 批量填充后更新患者表格数据
- [ ] 5.4 测试批量语音录入完整流程

## 6. 测试验证

- [ ] 6.1 单元测试voiceParser解析逻辑
- [ ] 6.2 单元测试patientMatcher匹配逻辑
- [ ] 6.3 手动测试批量语音录入UI交互
- [ ] 6.4 手动测试同名患者冲突处理
- [ ] 6.5 手动测试未匹配患者手动选择