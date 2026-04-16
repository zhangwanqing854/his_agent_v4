import request from './request'
import type { ApiResponse } from './request'

export interface VoiceSessionDto {
  sessionId: string
  transcript: string
  status: 'pending' | 'completed' | 'received'
  createdAt: string
  updatedAt: string
  expiresAt: string
}

export function createVoiceSession() {
  return request.post<ApiResponse<VoiceSessionDto>>('/voice-session/create')
}

export function submitVoiceSession(sessionId: string, transcript: string) {
  return request.post<ApiResponse<VoiceSessionDto>>(`/voice-session/${sessionId}`, {
    transcript,
    timestamp: new Date().toISOString()
  })
}

export function getVoiceSession(sessionId: string) {
  return request.get<ApiResponse<VoiceSessionDto>>(`/voice-session/${sessionId}`)
}

export function checkVoiceSessionStatus(sessionId: string) {
  return request.get<ApiResponse<{
    status: string
    transcript: string
    hasContent: boolean
  }>>(`/voice-session/${sessionId}/status`)
}