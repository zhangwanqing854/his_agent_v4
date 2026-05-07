import type { InterfaceConfig, SyncResult } from '@/types/interface-config'
import request from './request'

const SOAP_PROXY_PATH = '/api/soap-proxy'

export interface SyncDeptPatientOverviewResult {
  success: boolean
  message: string
  count?: number
}

export async function syncDeptPatientOverview(deptCode: string): Promise<SyncDeptPatientOverviewResult> {
  try {
    const response = await request.post(`/sync/dept-patient-info?deptCode=${encodeURIComponent(deptCode)}`)
    if (response.code === 0 && response.data) {
      return {
        success: true,
        message: response.message || '同步成功',
        count: response.data.count || 0
      }
    }
    return {
      success: false,
      message: response.message || '同步失败'
    }
  } catch (error: any) {
    return {
      success: false,
      message: error.message || '同步失败'
    }
  }
}

export async function executeSync(config: InterfaceConfig): Promise<SyncResult> {
  const startTime = Date.now()
  
  try {
    const syncTimeRange = calculateSyncTimeRange(config)
    
    if (config.apiProtocol === 'SOAP') {
      return await executeSoapSync(config, syncTimeRange)
    } else {
      return await executeRestSync(config, syncTimeRange)
    }
  } catch (error: any) {
    return {
      success: false,
      message: error.message || '同步失败',
      time: `${Date.now() - startTime}ms`
    }
  }
}

export function calculateSyncTimeRange(config: InterfaceConfig): { startTime: Date; endTime: Date; syncType: string } {
  const now = new Date()
  const isFirstSync = config.isFirstSync === true || !config.lastSyncTime
  
  if (isFirstSync) {
    const days = config.firstSyncDays || 30
    const startTime = new Date(now.getTime() - days * 24 * 60 * 60 * 1000)
    return { startTime, endTime: now, syncType: '首次同步' }
  } else {
    const hours = config.incrementalSyncHours || 24
    const lastSync = config.lastSyncTime ? new Date(config.lastSyncTime) : new Date(now.getTime() - hours * 60 * 60 * 1000)
    const startTime = new Date(lastSync.getTime() - 1 * 60 * 60 * 1000)
    return { startTime, endTime: now, syncType: '增量同步' }
  }
}

export function formatSyncTime(date: Date, format?: string): string {
  const fmt = format || 'yyyy-MM-dd HH:mm:ss'
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  
  return fmt
    .replace('yyyy', String(year))
    .replace('MM', month)
    .replace('dd', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

async function executeSoapSync(config: InterfaceConfig, syncTimeRange: { startTime: Date; endTime: Date; syncType: string }): Promise<SyncResult> {
  const startTime = Date.now()
  
  const startParamName = config.syncTimeParamStart || 'startTime'
  const endParamName = config.syncTimeParamEnd || 'endTime'
  const timeFormat = config.syncTimeFormat || 'yyyy-MM-dd HH:mm:ss'
  
  const soapParams = [
    ...(config.soapParams || []),
    { name: startParamName, value: formatSyncTime(syncTimeRange.startTime, timeFormat) },
    { name: endParamName, value: formatSyncTime(syncTimeRange.endTime, timeFormat) }
  ]
  
  const soapEnvelope = buildSoapEnvelope(
    config.soapAction || '',
    config.soapNamespace || 'http://i.sync.common.pkuih.iih/',
    soapParams
  )
  
  const response = await fetch(SOAP_PROXY_PATH, {
    method: 'POST',
    headers: {
      'Content-Type': 'text/xml;charset=UTF-8',
      'SOAPAction': config.soapAction || '',
      'X-SOAP-URL': config.url
    },
    body: soapEnvelope
  })
  
  if (!response.ok) {
    throw new Error(`HTTP ${response.status}: ${response.statusText}`)
  }
  
  const xmlText = await response.text()
  const data = parseSoapResponse(xmlText)
  
  return {
    success: true,
    message: `${syncTimeRange.syncType}成功，获取 ${data.length} 条数据`,
    data,
    count: data.length,
    time: `${Date.now() - startTime}ms`
  }
}

async function executeRestSync(config: InterfaceConfig, syncTimeRange: { startTime: Date; endTime: Date; syncType: string }): Promise<SyncResult> {
  const startTime = Date.now()
  
  const headers: Record<string, string> = {
    'Content-Type': 'application/json'
  }
  
  if (config.authType === 'BEARER' && config.authConfig?.token) {
    headers['Authorization'] = `Bearer ${config.authConfig.token}`
  } else if (config.authType === 'BASIC' && config.authConfig?.username && config.authConfig?.password) {
    const credentials = btoa(`${config.authConfig.username}:${config.authConfig.password}`)
    headers['Authorization'] = `Basic ${credentials}`
  } else if (config.authType === 'API_KEY' && config.authConfig?.apiKey) {
    headers['X-API-Key'] = config.authConfig.apiKey
  }
  
  const startParamName = config.syncTimeParamStart || 'startTime'
  const endParamName = config.syncTimeParamEnd || 'endTime'
  const timeFormat = config.syncTimeFormat || 'yyyy-MM-dd HH:mm:ss'
  
  let requestBody: any = {}
  if (config.requestTemplate) {
    try {
      requestBody = JSON.parse(config.requestTemplate)
    } catch {
      requestBody = {}
    }
  }
  
  requestBody[startParamName] = formatSyncTime(syncTimeRange.startTime, timeFormat)
  requestBody[endParamName] = formatSyncTime(syncTimeRange.endTime, timeFormat)
  
  const response = await fetch(config.url, {
    method: config.method || 'POST',
    headers,
    body: JSON.stringify(requestBody)
  })
  
  if (!response.ok) {
    throw new Error(`HTTP ${response.status}: ${response.statusText}`)
  }
  
  const result = await response.json()
  const dataPath = config.mappingTables[0]?.dataPath || 'data'
  const data = getDataByPath(result, dataPath)
  
  return {
    success: true,
    message: `${syncTimeRange.syncType}成功，获取 ${Array.isArray(data) ? data.length : 1} 条数据`,
    data: Array.isArray(data) ? data : [data],
    count: Array.isArray(data) ? data.length : 1,
    time: `${Date.now() - startTime}ms`
  }
}

function buildSoapEnvelope(action: string, namespace: string, params: { name: string; value: string }[]): string {
  const paramsXml = params.map(p => `         <i:${p.name}>${p.value}</i:${p.name}>`).join('\n')
  
  return `<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:i="${namespace}">
   <soapenv:Header/>
   <soapenv:Body>
      <i:${action}>
${paramsXml || '         <!--No parameters-->'}
      </i:${action}>
   </soapenv:Body>
</soapenv:Envelope>`
}

function parseSoapResponse(xmlText: string): any[] {
  const returnMatch = xmlText.match(/<return[^>]*>([\s\S]*?)<\/return>/)
  
  if (!returnMatch) {
    throw new Error('无法解析SOAP响应：找不到<return>标签')
  }
  
  const jsonText = returnMatch[1]?.trim() || ''
  
  try {
    const data = JSON.parse(jsonText)
    return Array.isArray(data) ? data : [data]
  } catch (e) {
    throw new Error(`JSON解析失败: ${e instanceof Error ? e.message : '未知错误'}`)
  }
}

function getDataByPath(obj: any, path: string): any {
  if (!path) return obj
  
  const parts = path.split('.')
  let result = obj
  
  for (const part of parts) {
    if (result && typeof result === 'object' && part in result) {
      result = result[part]
    } else {
      return null
    }
  }
  
  return result
}

export function applyMapping(data: any[], mappingTables: any[]): any[] {
  if (!mappingTables || mappingTables.length === 0) return data
  
  return data.map(item => {
    const result: Record<string, any> = {}
    
    const mainMapping = mappingTables[0]
    if (mainMapping?.fieldMappings) {
      for (const mapping of mainMapping.fieldMappings) {
        const value = getDataByPath(item, mapping.sourceField)
        result[mapping.targetField] = value ?? mapping.defaultValue
      }
    }
    
    return result
  })
}