import request from './request'
import type { ApiResponse } from './request'

export interface SmsConfigDto {
  key: string
  value: string
  maskedValue: string
  description: string
  isSensitive: boolean
}

export interface SmsConfigResponse {
  enabled: boolean
  provider: string
  configs: SmsConfigDto[]
}

export interface SmsConfigUpdateRequest {
  enabled: boolean
  provider: string
  aliyunAccessKeyId?: string
  aliyunAccessKeySecret?: string
  aliyunSignName?: string
  aliyunTemplateCode?: string
}

export function getSmsConfig(): Promise<ApiResponse<SmsConfigResponse>> {
  return request.get('/sms-config') as Promise<ApiResponse<SmsConfigResponse>>
}

export function updateSmsConfig(data: SmsConfigUpdateRequest): Promise<ApiResponse<void>> {
  return request.put('/sms-config', data) as Promise<ApiResponse<void>>
}

export function testSmsSend(phone: string): Promise<ApiResponse<void>> {
  return request.post('/sms-config/test', null, { params: { phone } }) as Promise<ApiResponse<void>>
}