import { ElMessage } from 'element-plus'

export interface VoiceWebSocketOptions {
  onConnected?: () => void
  onPartial?: (text: string) => void
  onResult?: (text: string) => void
  onFinal?: (text: string) => void
  onError?: (error: string) => void
  onDisconnected?: () => void
  maxRetries?: number
  retryDelay?: number
}

export class VoiceWebSocket {
  private ws: WebSocket | null = null
  private url: string
  private options: VoiceWebSocketOptions
  private retryCount = 0
  private maxRetries: number
  private retryDelay: number
  private isConnected = false
  private shouldReconnect = true
  private mediaRecorder: MediaRecorder | null = null
  private audioContext: AudioContext | null = null
  
  constructor(url: string, options: VoiceWebSocketOptions = {}) {
    this.url = url
    this.options = options
    this.maxRetries = options.maxRetries ?? 3
    this.retryDelay = options.retryDelay ?? 1000
  }
  
  connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      try {
        this.ws = new WebSocket(this.url)
        
        this.ws.onopen = () => {
          this.isConnected = true
          this.retryCount = 0
          this.shouldReconnect = true
          this.options.onConnected?.()
          resolve()
        }
        
        this.ws.onmessage = (event) => {
          try {
            const message = JSON.parse(event.data)
            this.handleMessage(message)
          } catch {
            this.options.onError?.('消息解析失败')
          }
        }
        
        this.ws.onerror = () => {
          this.isConnected = false
          this.options.onError?.('WebSocket连接错误')
          reject(new Error('WebSocket连接错误'))
        }
        
        this.ws.onclose = () => {
          this.isConnected = false
          this.options.onDisconnected?.()
          
          if (this.shouldReconnect && this.retryCount < this.maxRetries) {
            this.retryCount++
            ElMessage.warning(`连接断开，正在重连 (${this.retryCount}/${this.maxRetries})`)
            setTimeout(() => this.connect(), this.retryDelay)
          } else if (this.retryCount >= this.maxRetries) {
            ElMessage.error('连接重试失败，请刷新页面')
          }
        }
      } catch (error) {
        reject(error)
      }
    })
  }
  
  private handleMessage(message: { type: string; text?: string; partial?: boolean }) {
    switch (message.type) {
      case 'connected':
        break
      case 'partial':
        this.options.onPartial?.(message.text || '')
        break
      case 'result':
        this.options.onResult?.(message.text || '')
        break
      case 'final':
        this.options.onFinal?.(message.text || '')
        break
      case 'error':
        this.options.onError?.(message.text || '识别错误')
        break
    }
  }
  
  async startRecording(): Promise<void> {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        audio: {
          echoCancellation: true,
          noiseSuppression: true,
          sampleRate: 16000,
          channelCount: 1
        }
      })
      
      this.audioContext = new AudioContext({ sampleRate: 16000 })
      
      this.mediaRecorder = new MediaRecorder(stream, {
        mimeType: 'audio/webm;codecs=pcm'
      })
      
      this.mediaRecorder.ondataavailable = async (event) => {
        if (event.data.size > 0 && this.isConnected) {
          const audioBuffer = await event.data.arrayBuffer()
          const base64Audio = this.arrayBufferToBase64(audioBuffer)
          this.sendAudio(base64Audio)
        }
      }
      
      this.mediaRecorder.start(100)
    } catch {
      throw new Error('无法访问麦克风')
    }
  }
  
  private arrayBufferToBase64(buffer: ArrayBuffer): string {
    const bytes = new Uint8Array(buffer)
    let binary = ''
    for (let i = 0; i < bytes.byteLength; i++) {
      binary += String.fromCharCode(bytes[i])
    }
    return btoa(binary)
  }
  
  private sendAudio(base64Audio: string): void {
    if (this.ws && this.isConnected) {
      const message = {
        type: 'audio',
        data: base64Audio
      }
      this.ws.send(JSON.stringify(message))
    }
  }
  
  sendEnd(): void {
    if (this.ws && this.isConnected) {
      const message = { type: 'end' }
      this.ws.send(JSON.stringify(message))
    }
  }
  
  stopRecording(): void {
    if (this.mediaRecorder && this.mediaRecorder.state !== 'inactive') {
      this.mediaRecorder.stop()
      this.mediaRecorder.stream.getTracks().forEach(track => track.stop())
      this.mediaRecorder = null
    }
    
    if (this.audioContext) {
      this.audioContext.close()
      this.audioContext = null
    }
  }
  
  disconnect(): void {
    this.shouldReconnect = false
    this.stopRecording()
    
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
    
    this.isConnected = false
  }
  
  getConnectionStatus(): boolean {
    return this.isConnected
  }
}