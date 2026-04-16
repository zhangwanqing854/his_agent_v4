import request from '@/api/request'
import type { InterfaceConfig } from '@/types/interface-config'
import {
  getInterfaceConfigList as mockGetList,
  getInterfaceConfigById as mockGetById,
  createInterfaceConfig as mockCreate,
  updateInterfaceConfig as mockUpdate,
  deleteInterfaceConfig as mockDelete
} from '@/mock/interface-config'

const USE_MOCK = false

export function getInterfaceConfigList(): Promise<{ code: number; data: InterfaceConfig[] }> {
  if (USE_MOCK) {
    return Promise.resolve({ code: 0, data: mockGetList() })
  }
  return request.get('/interface-configs')
}

export function getInterfaceConfigById(id: number): Promise<{ code: number; data: InterfaceConfig | null }> {
  if (USE_MOCK) {
    return Promise.resolve({ code: 0, data: mockGetById(id) })
  }
  return request.get(`/interface-configs/${id}`)
}

export function createInterfaceConfig(data: InterfaceConfig): Promise<{ code: number; data: InterfaceConfig }> {
  if (USE_MOCK) {
    return Promise.resolve({ code: 0, data: mockCreate(data) })
  }
  return request.post('/interface-configs', data)
}

export function updateInterfaceConfig(id: number, data: Partial<InterfaceConfig>): Promise<{ code: number; data: InterfaceConfig | null }> {
  if (USE_MOCK) {
    return Promise.resolve({ code: 0, data: mockUpdate(id, data) })
  }
  return request.put(`/interface-configs/${id}`, data)
}

export function deleteInterfaceConfig(id: number): Promise<{ code: number }> {
  if (USE_MOCK) {
    mockDelete(id)
    return Promise.resolve({ code: 0 })
  }
  return request.delete(`/interface-configs/${id}`)
}

export interface TestResult {
  success: boolean
  message: string
  responseTime?: number
  statusCode?: number
  errorDetail?: string
}

export function testInterfaceConnection(id: number): Promise<{ code: number; data: TestResult }> {
  if (USE_MOCK) {
    return Promise.resolve({ code: 0, data: { success: true, message: '连接成功', responseTime: 50 } })
  }
  return request.post(`/interface-configs/${id}/test`)
}