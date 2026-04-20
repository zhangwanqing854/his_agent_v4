import request from './request'

export interface SystemConfig {
  EXAM_REPORT_URL: string
  TEST_REPORT_URL: string
}

export function fetchSystemConfig() {
  return request.get<SystemConfig>('/system-config')
}

export function updateSystemConfig(configs: Partial<SystemConfig>) {
  return request.put('/system-config', configs)
}