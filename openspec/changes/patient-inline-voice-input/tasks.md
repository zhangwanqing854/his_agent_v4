## 1. 患者列表UI修改

- [x] 1.1 在 CreateHandoverPage.vue 患者列表操作列添加语音录入按钮（麦克风图标）
- [x] 1.2 添加按钮样式，使用紧凑图标设计避免列表过宽
- [x] 1.3 实现点击按钮打开语音录入对话框的逻辑，传入当前患者信息

## 2. VoiceInputDialog组件优化

- [x] 2.1 添加 patient prop，支持外部传入患者对象
- [x] 2.2 当传入 patient 时，自动切换到单人模式并隐藏模式切换按钮
- [x] 2.3 单人模式下识别结果直接回写，不执行患者匹配逻辑
- [x] 2.4 优化单人模式的 apply 事件，直接返回识别内容而非匹配结果

## 3. 数据回写逻辑

- [x] 3.1 CreateHandoverPage.vue 添加 handleInlineVoiceApply 方法
- [x] 3.2 实现识别内容直接写入传入患者的 currentCondition 字段
- [x] 3.3 回写后显示成功提示，允许用户继续修改

## 4. 录音后编辑界面

- [x] 4.1 在 VoiceInputDialog.vue 添加新的 status 状态 'edit'
- [x] 4.2 创建编辑界面 UI：显示 textarea（可编辑识别内容）+ 确认/取消按钮
- [x] 4.3 修改 handleComplete 逻辑：inline 模式下停止录音后进入 'edit' 状态而非 'processing'
- [x] 4.4 添加 editableContent ref 存储可编辑的识别内容
- [x] 4.5 添加 handleConfirmEdit 方法：emit editableContent 并关闭对话框
- [x] 4.6 添加 handleCancelEdit 方法：关闭对话框不回写内容

## 5. 测试验证

- [x] 5.1 测试患者列表语音按钮显示
- [x] 5.2 测试点击按钮打开对话框并传入正确患者
- [x] 5.3 测试语音识别结果正确回写
- [x] 5.4 测试回写后可手动修改
- [ ] 5.5 测试录音停止后进入编辑界面
- [ ] 5.6 测试编辑界面可修改内容
- [ ] 5.7 测试确认后正确回写修改后的内容