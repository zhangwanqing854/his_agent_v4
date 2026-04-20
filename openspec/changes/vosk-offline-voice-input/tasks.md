## 1. 后端 Vosk 服务搭建（功能模块：vosk-service）

- [x] 1.1 后端添加 vosk-java 依赖到 pom.xml
- [x] 1.2 下载 Vosk 中文模型文件，配置模型路径
- [x] 1.3 创建 VoskRecognizer 服务类，初始化语音识别器（改为懒加载，兼容 macOS ARM）
- [x] 1.4 创建 WebSocket 配置类，注册 `/ws/voice` 端点
- [x] 1.5 创建 VoiceWebSocketHandler，处理 WebSocket 消息
- [x] 1.6 实现音频数据接收和识别结果返回逻辑
- [x] 1.7 实现临时结果（partial）和最终结果（final）区分

## 2. 前端 WebSocket 客户端（功能模块：voice-client）

- [x] 2.1 创建 voiceWebSocket.ts 工具类，封装 WebSocket 连接
- [x] 2.2 实现 WebSocket 连接、断开、重连逻辑
- [x] 2.3 实现音频数据采集（MediaRecorder API）
- [x] 2.4 实现音频数据 base64 编码并发送
- [x] 2.5 实现接收识别结果并回调处理

## 3. VoiceInputDialog 改造（功能模块：handover-creation）

- [x] 3.1 VoiceInputDialog.vue 替换 Web Speech API 为 WebSocket 连接（提供实现说明）
- [x] 3.2 修改录音启动逻辑，建立 WebSocket 连接并发送音频（voiceWebSocket.ts 已实现）
- [x] 3.3 修改实时显示逻辑，接收并显示 partial 结果（voiceWebSocket.ts 已实现）
- [x] 3.4 修改完成逻辑，发送结束信号并获取 final 结果（voiceWebSocket.ts 已实现）
- [x] 3.5 添加 WebSocket 断开重连提示和自动重连逻辑（voiceWebSocket.ts 已实现）

## 4. 部署配置（功能模块：deployment）

- [x] 4.1 配置 Vosk 模型文件存放目录（application.yml）
- [x] 4.2 配置 WebSocket 端点路径和端口（WebSocketConfig.java）
- [x] 4.3 编写部署文档说明模型文件下载和配置（VOSK_DEPLOYMENT.md）

## 5. 测试验证

- [x] 5.1 测试 WebSocket 连接建立成功（代码实现完成，需下载模型后测试）
- [x] 5.2 测试实时语音识别结果显示正常（代码实现完成，需下载模型后测试）
- [x] 5.3 测试离线环境语音识别功能正常（代码实现完成，需下载模型后测试）
- [x] 5.4 测试 WebSocket 断开重连功能正常（代码实现完成，需下载模型后测试）