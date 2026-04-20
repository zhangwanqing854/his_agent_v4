## 上下文

当前系统使用浏览器原生 Web Speech API 进行语音识别，依赖云端服务，医院内网环境下无法使用。需要替换为离线语音识别方案，支持在内网环境下正常使用语音录入功能。

Vosk 是开源离线语音识别引擎，特点：
- 开源免费（Apache 2.0 License）
- 支持中文模型（vosk-model-cn-kaldi-multilingual）
- 可通过 WebSocket 提供实时流式识别服务
- 模型文件约 50MB，部署轻量

## 目标 / 非目标

**目标：**
- 后端部署 Vosk WebSocket 语音识别服务
- 前端通过 WebSocket 连接发送音频流，接收实时识别结果
- 支持离线环境下的语音录入功能
- 保持现有 UI 交互流程不变

**非目标：**
- 不修改语音文本解析逻辑（parseVoiceText）
- 不修改患者匹配逻辑
- 不添加语音合成（TTS）功能

## 决策

### 册策 1：Vosk 服务部署方式

**选择：** 后端 Spring Boot 内嵌 WebSocket 服务

**理由：**
- 与现有架构一致，无需额外服务进程
- 使用 vosk-java 库集成 Vosk
- WebSocket 实时通信，支持流式识别

**替代方案：**
- 独立 Vosk-Server 进程：需要额外部署运维
- Python Vosk 服务：需要 Python 环境，架构不一致

### 册策 2：WebSocket 消息格式

**选择：** JSON 格式消息

**消息协议：**
```json
// 前端发送音频数据
{ "type": "audio", "data": "<base64-encoded-audio>" }

// 后端返回识别结果
{ "type": "result", "text": "识别文本", "partial": false }
{ "type": "partial", "text": "临时结果", "partial": true }
```

**理由：**
- JSON 格式易于解析
- 支持临时结果（partial）实时显示
- 与 Vosk API 输出格式一致

### 册策 3：音频格式

**选择：** PCM 16kHz 16bit 单声道

**理由：**
- Vosk 中文模型要求 16kHz
- PCM 格式无需编码，直接发送
- 浏览器 MediaRecorder API 支持获取 PCM

## 风险 / 权衡

- **风险：Vosk 中文模型识别准确率可能低于云端服务** → 医疗场景专业词汇较多，后续可考虑训练定制模型
- **风险：WebSocket 连接稳定性** → 前端添加重连机制，后端保持连接活跃
- **权衡：PCM 音频数据量较大** → 使用 base64 编码传输，带宽占用可接受

## 部署步骤

1. 后端添加 vosk-java 依赖
2. 下载 Vosk 中文模型文件，部署到服务器
3. 创建 WebSocket 语音识别端点 `/ws/voice`
4. 前端修改 VoiceInputDialog.vue，使用 WebSocket 替换 Web Speech API
5. 测试验证离线环境语音识别功能