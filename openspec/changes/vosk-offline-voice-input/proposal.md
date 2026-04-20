## 为什么

当前系统使用浏览器原生 Web Speech API 进行语音识别，依赖 Google/Apple 云端服务，**无法离线运行**。医院内网环境可能无法访问外部网络，导致语音录入功能失效。需要替换为支持离线运行的语音识别方案。

Vosk 是开源离线语音识别引擎，无需 License，适合医院内网部署。

## 变更内容

- 将语音识别从 Web Speech API 替换为 Vosk 离线语音识别
- 后端部署 Vosk 服务，前端通过 WebSocket 连接进行实时语音识别
- 支持离线环境下的语音录入功能
- 保持现有语音录入交互流程不变

## 功能 (Capabilities)

### 新增功能

- `vosk-voice-recognition`: Vosk 离线语音识别功能，后端 WebSocket 服务

### 修改功能

- `handover-creation`: 语音录入实现从 Web Speech API 改为 Vosk 离线语音

## 影响

- 前端：VoiceInputDialog.vue - 替换语音识别实现，使用 WebSocket 连接
- 后端：新增 Vosk 语音识别 WebSocket 服务端
- 部署：需要部署 Vosk 中文模型文件（约 50MB）
- 非目标：不修改语音解析逻辑、患者匹配逻辑、UI 交互流程