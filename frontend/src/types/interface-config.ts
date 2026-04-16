// 字段映射配置
export interface FieldMapping {
  id?: number
  sourceField: string
  targetField: string
  transformType: 'DIRECT' | 'DATE' | 'NUMBER' | 'ENUM' | 'EXPRESSION'
  transformConfig?: string
  defaultValue?: string
  isRequired: boolean
  sortOrder: number
}

// 映射表配置
export interface MappingTable {
  id?: number
  mappingCode: string
  mappingName: string
  targetTable: string
  dataPath: string
  isArray: boolean
  parentMappingId?: number
  relationField?: string
  sortOrder: number
  fieldMappings: FieldMapping[]
  children?: MappingTable[]
}

// 接口配置
export interface InterfaceConfig {
  id?: number
  configCode: string
  configName: string
  system: 'HIS' | 'MOBILE_NURSING' | 'CDR' | 'OTHER'
  mode: 'PULL' | 'PUSH'
  protocol: 'HTTP' | 'HTTPS' | 'WEBSOCKET'
  apiProtocol: 'REST' | 'SOAP' | 'HL7' | 'FHIR'
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  url: string
  dataType?: 'DEPARTMENT' | 'STAFF' | 'PATIENT' | 'DIAGNOSIS' | 'ORDER' | 'VISIT' | 'TRANSFER' | 'DOCTOR_DEPT' | 'VITAL_SIGN' | 'EXAM_REPORT' | 'LAB_REPORT'
  syncMode: 'MANUAL' | 'SCHEDULED' | 'ON_DEMAND'
  syncSchedule?: string
  requestTemplate?: string
  authType: 'NONE' | 'BASIC' | 'BEARER' | 'API_KEY'
  authConfig?: Record<string, string>
  retryInterval: number
  maxRetries: number
  onFailure: 'STOP' | 'SKIP' | 'ALERT'
  enabled: boolean
  description?: string
  mappingTables: MappingTable[]
  // SOAP特有配置
  soapAction?: string
  soapNamespace?: string
  soapParams?: SoapParam[]
  // 增量同步配置
  syncTimeParamStart?: string
  syncTimeParamEnd?: string
  syncTimeFormat?: string
  firstSyncDays?: number
  incrementalSyncHours?: number
  isFirstSync?: boolean
  // 同步状态（运行时字段）
  lastSyncTime?: string
  lastSyncStatus?: 'SUCCESS' | 'FAILED' | 'PARTIAL'
  lastSyncCount?: number
}

// SOAP参数
export interface SoapParam {
  name: string
  value: string
}

// 同步执行结果
export interface SyncResult {
  success: boolean
  message: string
  data?: any[]
  count?: number
  time?: string
}

// 目标表选项
export const TARGET_TABLE_OPTIONS = [
  { value: 'department', label: '科室表 (department)' },
  { value: 'his_staff', label: '医护信息表 (his_staff)' },
  { value: 'patient', label: '患者表 (patient)' },
  { value: 'visit', label: '就诊表 (visit)' },
  { value: 'transfer_record', label: '转科记录表 (transfer_record)' },
  { value: 'diagnosis_main', label: '诊断主表 (diagnosis_main)' },
  { value: 'diagnosis_item', label: '诊断子表 (diagnosis_item)' },
  { value: 'order_main', label: '医嘱主表 (order_main)' },
  { value: 'order_item', label: '医嘱子表 (order_item)' }
]

// 转换类型选项
export const TRANSFORM_TYPE_OPTIONS = [
  { value: 'DIRECT', label: '直接映射' },
  { value: 'DATE', label: '日期转换' },
  { value: 'NUMBER', label: '数字转换' },
  { value: 'ENUM', label: '枚举映射' },
  { value: 'EXPRESSION', label: '表达式' }
]

// 系统选项
export const SYSTEM_OPTIONS = [
  { value: 'HIS', label: 'HIS系统' },
  { value: 'MOBILE_NURSING', label: '移动护理' },
  { value: 'CDR', label: 'CDR系统' },
  { value: 'OTHER', label: '其他' }
]

// 数据类型选项
export const DATA_TYPE_OPTIONS = [
  { value: 'DEPARTMENT', label: '科室信息' },
  { value: 'STAFF', label: '医护人员' },
  { value: 'PATIENT', label: '患者信息' },
  { value: 'VISIT', label: '就诊信息' },
  { value: 'TRANSFER', label: '转科信息' },
  { value: 'DIAGNOSIS', label: '诊断信息' },
  { value: 'ORDER', label: '医嘱信息' },
  { value: 'DOCTOR_DEPT', label: '科室人员关系' },
  { value: 'VITAL_SIGN', label: '体征数据' },
  { value: 'EXAM_REPORT', label: '检查报告' },
  { value: 'LAB_REPORT', label: '检验报告' }
]