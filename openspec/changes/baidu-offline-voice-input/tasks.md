## 1. SDK 准备（功能模块：voice-input）

- [ ] 1.1 申请百度离线语音 SDK License 和 AppID
- [ ] 1.2 获取百度离线语音 Web SDK 包和模型文件
- [ ] 1.3 将模型文件部署到后端静态资源目录

## 2. 后端配置（功能模块：voice-input）

- [ ] 2.1 配置后端静态资源服务，提供模型文件下载接口 /api/voice/model
- [ ] 2.2 配置系统配置表存储 SDK License 和 AppID

## 3. 前端集成（功能模块：handover-creation）

- [ ] 3.1 安装百度离线语音 Web SDK 依赖到前端项目
- [ ] 3.2 创建 baiduVoiceService.ts 工具类，封装 SDK 初始化和识别调用
- [ ] 3.3 实现模型文件下载和 IndexedDB 缓存逻辑
- [ ] 3.4 修改 VoiceInputDialog.vue 的 startSpeechRecognition 方法，替换为百度 SDK 调用

## 4. 离线支持（功能模块：voice-input）

- [ ] 4.1 实现离线检测逻辑，无网络时自动使用本地模型
- [ ] 4.2 实现首次使用时的模型下载进度显示
- [ ] 4.3 实现识别失败时的错误处理和降级提示

## 5. 测试验证（功能模块：voice-input）

- [ ] 5.1 测试离线环境下语音识别功能正常工作
- [ ] 5.2 测试中文医疗术语识别准确率
- [ ] 5.3 测试单人语音录入和批量语音录入功能
- [ ] 5.4 测试首次使用模型下载流程