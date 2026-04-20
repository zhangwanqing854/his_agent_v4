# Vosk 离线语音识别部署说明

## 1. 下载 Vosk 中文模型

**手动下载方式（推荐）：**

1. 在浏览器中访问 https://alphacephei.com/vosk/models
2. 下载以下任一模型：
   - `vosk-model-cn-kaldi-multilingual-0.15.zip`（约 50MB，推荐）
   - `vosk-model-small-cn-0.22.zip`（约 42MB，轻量版）

3. 解压到项目目录：
```bash
# 解压到 backend/vosk-model 目录
unzip vosk-model-small-cn-0.22.zip
mv vosk-model-small-cn-0.22 /Users/zwq/workspace/his_agent_v4/backend/vosk-model
```

**命令行下载（需要稳定网络）：**
```bash
cd /Users/zwq/workspace/his_agent_v4/backend
mkdir -p vosk-model
curl -L -A "Mozilla/5.0" -o vosk-model.zip "https://alphacephei.com/vosk/models/vosk-model-small-cn-0.22.zip"
unzip vosk-model.zip
mv vosk-model-small-cn-0.22 vosk-model
rm vosk-model.zip
```

## 2. 配置模型路径

默认模型路径：`./vosk-model`（相对于 backend 目录）

可通过环境变量覆盖：
```bash
export VOSK_MODEL_PATH=/path/to/your/model
```

## 3. WebSocket 端点配置

WebSocket 端点：`ws://localhost:8080/ws/voice`