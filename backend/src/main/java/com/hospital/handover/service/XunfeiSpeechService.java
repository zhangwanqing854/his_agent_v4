package com.hospital.handover.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class XunfeiSpeechService {

    private static final Logger logger = LoggerFactory.getLogger(XunfeiSpeechService.class);
    private static final String WS_URL = "wss://rtasr.xfyun.cn/v1/ws";

    @Value("${xunfei.appid:}")
    private String appId;

    @Value("${xunfei.apikey:ef71686251f3f7b42bbb56c3d737f938}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String recognize(byte[] audioData) {
        if (appId == null || appId.isEmpty()) {
            logger.warn("讯飞appid未配置");
            return "请在application.yml配置xunfei.appid";
        }

        try {
            byte[] pcmData = convertToPcm(audioData);
            return doWebSocketRecognize(pcmData);
        } catch (Exception e) {
            logger.error("语音识别失败", e);
            return "";
        }
    }

    private byte[] convertToPcm(byte[] audioData) {
        if (audioData.length > 12) {
            String header = new String(audioData, 0, 4, StandardCharsets.US_ASCII);
            if ("RIFF".equals(header)) {
                int dataOffset = findDataChunk(audioData);
                byte[] pcm = new byte[audioData.length - dataOffset];
                System.arraycopy(audioData, dataOffset, pcm, 0, pcm.length);
                return pcm;
            }
        }
        return audioData;
    }

    private int findDataChunk(byte[] wav) {
        int offset = 12;
        while (offset < wav.length - 8) {
            String id = new String(wav, offset, 4, StandardCharsets.US_ASCII);
            int size = littleEndian(wav, offset + 4);
            offset += 8;
            if ("data".equals(id)) {
                return offset;
            }
            offset += size;
        }
        return 44;
    }

    private int littleEndian(byte[] b, int off) {
        return (b[off] & 0xFF) | ((b[off + 1] & 0xFF) << 8) |
                ((b[off + 2] & 0xFF) << 16) | ((b[off + 3] & 0xFF) << 24);
    }

    private String doWebSocketRecognize(byte[] pcm) throws Exception {
        String ts = String.valueOf(System.currentTimeMillis() / 1000);
        String signa = buildSignature(ts);
        String url = WS_URL + "?appid=" + URLEncoder.encode(appId, "UTF-8")
                + "&ts=" + ts + "&signa=" + URLEncoder.encode(signa, "UTF-8") + "&lang=cn";

        CompletableFuture<String> result = new CompletableFuture<>();
        StringBuilder transcript = new StringBuilder();

        WebSocketClient client = new WebSocketClient(URI.create(url)) {
            @Override
            public void onOpen(ServerHandshake handshake) {
                logger.info("讯飞WebSocket已连接");
                int offset = 0;
                int chunkSize = 1280;
                while (offset < pcm.length) {
                    int len = Math.min(chunkSize, pcm.length - offset);
                    byte[] chunk = new byte[len];
                    System.arraycopy(pcm, offset, chunk, 0, len);
                    this.send(ByteBuffer.wrap(chunk));
                    offset += len;
                    try {
                        Thread.sleep(40);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                this.send("{\"end\": true}");
            }

            @Override
            public void onMessage(String message) {
                try {
                    JsonNode json = objectMapper.readTree(message);
                    String action = json.path("action").asText();
                    if ("result".equals(action)) {
                        JsonNode dataNode = json.path("data");
                        if (dataNode.isTextual()) {
                            JsonNode inner = objectMapper.readTree(dataNode.asText());
                            JsonNode cn = inner.path("cn");
                            if (!cn.isMissingNode()) {
                                JsonNode st = cn.path("st");
                                JsonNode rt = st.path("rt");
                                if (rt.isArray()) {
                                    for (JsonNode rtItem : rt) {
                                        JsonNode ws = rtItem.path("ws");
                                        if (ws.isArray()) {
                                            for (JsonNode wsItem : ws) {
                                                JsonNode cw = wsItem.path("cw");
                                                if (cw.isArray()) {
                                                    for (JsonNode cwItem : cw) {
                                                        transcript.append(cwItem.path("w").asText());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if ("0".equals(st.path("type").asText())) {
                                    result.complete(transcript.toString());
                                    this.close();
                                }
                            }
                        }
                    } else if ("error".equals(action)) {
                        logger.error("讯飞错误: {}", json.path("desc").asText());
                        result.complete("");
                        this.close();
                    }
                } catch (Exception e) {
                    logger.error("解析消息失败", e);
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                logger.info("WebSocket关闭: {}", reason);
                if (!result.isDone()) {
                    result.complete(transcript.toString());
                }
            }

            @Override
            public void onError(Exception ex) {
                logger.error("WebSocket错误", ex);
                result.complete("");
            }
        };

        client.connect();
        return result.get(30, TimeUnit.SECONDS);
    }

    private String buildSignature(String ts) throws Exception {
        String base = appId + ts;
        MessageDigest md = MessageDigest.getInstance("MD5");
        String md5Hex = hex(md.digest(base.getBytes(StandardCharsets.UTF_8)));

        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(apiKey.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
        return Base64.getEncoder().encodeToString(mac.doFinal(md5Hex.getBytes(StandardCharsets.UTF_8)));
    }

    private String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}