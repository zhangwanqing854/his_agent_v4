import type { InterfaceConfig } from '@/types/interface-config'

const interfaceConfigs: InterfaceConfig[] = [
  {
    id: 1,
    configCode: 'HIS_DEPT_SYNC',
    configName: 'HIS科室信息同步',
    system: 'HIS',
    mode: 'PULL',
    protocol: 'HTTP',
    apiProtocol: 'SOAP',
    method: undefined,
    url: 'http://10.193.162.82:8089/iih.pkuih.common.sync.i.IHISSyncHoService?p=YWNjZXNzX3Rva2VuPWNmYTBjMzhmLWJjMGUtNDkxNC04M2I2LWY3NmI0ODViYzhhZQ==',
    dataType: 'DEPARTMENT',
    syncMode: 'MANUAL',
    syncSchedule: '',
    requestTemplate: '',
    authType: 'NONE',
    retryInterval: 5,
    maxRetries: 3,
    onFailure: 'ALERT',
    enabled: true,
    soapAction: 'getDept',
    soapNamespace: 'http://i.sync.common.pkuih.iih/',
    soapParams: [
      { name: 'arg0', value: '' },
      { name: 'arg1', value: '' }
    ],
    syncTimeParamStart: 'arg0',
    syncTimeParamEnd: 'arg1',
    syncTimeFormat: 'yyyy-MM-dd HH:mm:ss',
    firstSyncDays: 0,
    incrementalSyncHours: 24,
    isFirstSync: true,
    lastSyncTime: undefined,
    lastSyncStatus: undefined,
    lastSyncCount: undefined,
    mappingTables: [
      {
        mappingCode: 'DEPT_MAIN',
        mappingName: '科室主表映射',
        targetTable: 'department',
        dataPath: 'return',
        isArray: true,
        sortOrder: 0,
        fieldMappings: [
          { sourceField: 'id_dep', targetField: 'his_id', transformType: 'DIRECT', isRequired: true, sortOrder: 0 },
          { sourceField: 'code', targetField: 'code', transformType: 'DIRECT', isRequired: true, sortOrder: 1 },
          { sourceField: 'name', targetField: 'name', transformType: 'DIRECT', isRequired: true, sortOrder: 2 }
        ],
        children: []
      }
    ]
  },
  {
    id: 2,
    configCode: 'HIS_STAFF_SYNC',
    configName: 'HIS人员信息同步',
    system: 'HIS',
    mode: 'PULL',
    protocol: 'HTTP',
    apiProtocol: 'SOAP',
    method: undefined,
    url: 'http://10.193.162.82:8089/iih.pkuih.common.sync.i.IHISSyncHoService?wsdl&access_token=cfa0c38f-bc0e-4914-83b6-f76b485bc8ae',
    dataType: 'STAFF',
    syncMode: 'SCHEDULED',
    syncSchedule: '0 0 7 * * ?',
    requestTemplate: '',
    authType: 'NONE',
    retryInterval: 5,
    maxRetries: 3,
    onFailure: 'ALERT',
    enabled: true,
    soapAction: 'getEmp',
    soapNamespace: 'http://i.sync.common.pkuih.iih/',
    soapParams: [
      { name: 'arg0', value: '' },
      { name: 'arg1', value: '' }
    ],
    syncTimeParamStart: 'arg0',
    syncTimeParamEnd: 'arg1',
    syncTimeFormat: 'yyyy-MM-dd HH:mm:ss',
    firstSyncDays: 0,
    incrementalSyncHours: 24,
    isFirstSync: true,
    lastSyncTime: undefined,
    lastSyncStatus: undefined,
    lastSyncCount: undefined,
    mappingTables: [
      {
        mappingCode: 'STAFF_MAIN',
        mappingName: '人员主表映射',
        targetTable: 'his_staff',
        dataPath: 'return',
        isArray: true,
        sortOrder: 0,
        fieldMappings: [
          { sourceField: 'code_user', targetField: 'staff_code', transformType: 'DIRECT', isRequired: true, sortOrder: 0 },
          { sourceField: 'name_user', targetField: 'name', transformType: 'DIRECT', isRequired: true, sortOrder: 1 },
          { sourceField: 'sd_emptitle', targetField: 'title_code', transformType: 'DIRECT', isRequired: false, sortOrder: 2 },
          { sourceField: 'name_emptitle', targetField: 'title', transformType: 'DIRECT', isRequired: false, sortOrder: 3 }
        ],
        children: []
      }
    ]
  },
  {
    id: 3,
    configCode: 'HIS_PATIENT_SYNC',
    configName: 'HIS患者信息同步',
    system: 'HIS',
    mode: 'PULL',
    protocol: 'HTTP',
    apiProtocol: 'SOAP',
    method: undefined,
    url: 'http://10.193.162.82:8089/iih.pkuih.common.sync.i.IHISSyncHoService?p=YWNjZXNzX3Rva2VuPWNmYTBjMzhmLWJjMGUtNDkxNC04M2I2LWY3NmI0ODViYzhhZQ==',
    dataType: 'PATIENT',
    syncMode: 'SCHEDULED',
    syncSchedule: '0 0 7,16 * * ?',
    requestTemplate: '',
    authType: 'NONE',
    retryInterval: 5,
    maxRetries: 3,
    onFailure: 'ALERT',
    enabled: true,
    soapAction: 'getPiPat',
    soapNamespace: 'http://i.sync.common.pkuih.iih/',
    soapParams: [
      { name: 'arg0', value: '' },
      { name: 'arg1', value: '' }
    ],
    syncTimeParamStart: 'arg0',
    syncTimeParamEnd: 'arg1',
    syncTimeFormat: 'yyyy-MM-dd HH:mm:ss',
    firstSyncDays: 30,
    incrementalSyncHours: 24,
    isFirstSync: true,
    lastSyncTime: undefined,
    lastSyncStatus: undefined,
    lastSyncCount: undefined,
    mappingTables: [
      {
        mappingCode: 'PATIENT_MAIN',
        mappingName: '患者主表映射',
        targetTable: 'patient',
        dataPath: 'return',
        isArray: true,
        sortOrder: 0,
        fieldMappings: [
          { sourceField: 'code', targetField: 'patient_no', transformType: 'DIRECT', isRequired: true, sortOrder: 0 },
          { sourceField: 'name', targetField: 'name', transformType: 'DIRECT', isRequired: true, sortOrder: 1 },
          { sourceField: 'sd_sex', targetField: 'gender_code', transformType: 'DIRECT', isRequired: true, sortOrder: 2 },
          { sourceField: 'name_sex', targetField: 'gender', transformType: 'DIRECT', isRequired: true, sortOrder: 3 },
          { sourceField: 'dt_birth', targetField: 'birth_date', transformType: 'DATE', isRequired: false, sortOrder: 4 },
          { sourceField: 'mob', targetField: 'phone', transformType: 'DIRECT', isRequired: false, sortOrder: 5 }
        ],
        children: []
      }
    ]
  },
  {
    id: 4,
    configCode: 'HIS_VISIT_SYNC',
    configName: 'HIS就诊信息同步',
    system: 'HIS',
    mode: 'PULL',
    protocol: 'HTTP',
    apiProtocol: 'SOAP',
    method: undefined,
    url: 'http://10.193.162.82:8089/iih.pkuih.common.sync.i.IHISSyncHoService?p=YWNjZXNzX3Rva2VuPWNmYTBjMzhmLWJjMGUtNDkxNC04M2I2LWY3NmI0ODViYzhhZQ==',
    dataType: 'VISIT',
    syncMode: 'SCHEDULED',
    syncSchedule: '0 0 7,16 * * ?',
    requestTemplate: '',
    authType: 'NONE',
    retryInterval: 5,
    maxRetries: 3,
    onFailure: 'ALERT',
    enabled: true,
    soapAction: 'getEnt',
    soapNamespace: 'http://i.sync.common.pkuih.iih/',
    soapParams: [
      { name: 'arg0', value: '' },
      { name: 'arg1', value: '' }
    ],
    syncTimeParamStart: 'arg0',
    syncTimeParamEnd: 'arg1',
    syncTimeFormat: 'yyyy-MM-dd HH:mm:ss',
    firstSyncDays: 30,
    incrementalSyncHours: 24,
    isFirstSync: true,
    lastSyncTime: undefined,
    lastSyncStatus: undefined,
    lastSyncCount: undefined,
    mappingTables: [
      {
        mappingCode: 'VISIT_MAIN',
        mappingName: '就诊主表映射',
        targetTable: 'visit',
        dataPath: 'return',
        isArray: true,
        sortOrder: 0,
        fieldMappings: [
          { sourceField: 'code_ent', targetField: 'visit_no', transformType: 'DIRECT', isRequired: true, sortOrder: 0 },
          { sourceField: 'code_pat', targetField: 'patient_no', transformType: 'DIRECT', isRequired: true, sortOrder: 1 },
          { sourceField: 'name_pat', targetField: 'patient_name', transformType: 'DIRECT', isRequired: true, sortOrder: 2 },
          { sourceField: 'id_pat', targetField: 'patient_his_id', transformType: 'DIRECT', isRequired: true, sortOrder: 3 },
          { sourceField: 'code_dep_phy', targetField: 'dept_his_id', transformType: 'DIRECT', isRequired: true, sortOrder: 4 },
          { sourceField: 'name_dep_phy', targetField: 'dept_name', transformType: 'DIRECT', isRequired: true, sortOrder: 5 },
          { sourceField: 'sd_level_nur', targetField: 'nurse_level_code', transformType: 'DIRECT', isRequired: true, sortOrder: 6 },
          { sourceField: 'name_level_nur', targetField: 'nurse_level', transformType: 'DIRECT', isRequired: true, sortOrder: 7 },
          { sourceField: 'dt_acpt', targetField: 'admission_datetime', transformType: 'DATE', isRequired: true, sortOrder: 8 },
          { sourceField: 'dt_end', targetField: 'discharge_datetime', transformType: 'DATE', isRequired: false, sortOrder: 9 },
          { sourceField: 'code_entp', targetField: 'visit_type', transformType: 'DIRECT', isRequired: false, sortOrder: 10 },
          { sourceField: 'times_ip', targetField: 'admission_count', transformType: 'DIRECT', isRequired: false, sortOrder: 11 },
          { sourceField: 'id_dep_nur', targetField: 'nurse_area_id', transformType: 'DIRECT', isRequired: false, sortOrder: 12 },
          { sourceField: 'code_dep_nur', targetField: 'nurse_area_code', transformType: 'DIRECT', isRequired: false, sortOrder: 13 },
          { sourceField: 'name_dep_nur', targetField: 'nurse_area_name', transformType: 'DIRECT', isRequired: false, sortOrder: 14 }
        ],
        children: []
      }
    ]
  },
  {
    id: 5,
    configCode: 'HIS_TRANSFER_SYNC',
    configName: 'HIS转科信息同步',
    system: 'HIS',
    mode: 'PULL',
    protocol: 'HTTP',
    apiProtocol: 'SOAP',
    method: undefined,
    url: 'http://10.193.162.82:8089/iih.pkuih.common.sync.i.IHISSyncHoService?p=YWNjZXNzX3Rva2VuPWNmYTBjMzhmLWJjMGUtNDkxNC04M2I2LWY3NmI0ODViYzhhZQ==',
    dataType: 'TRANSFER',
    syncMode: 'SCHEDULED',
    syncSchedule: '0 30 7,16 * * ?',
    requestTemplate: '',
    authType: 'NONE',
    retryInterval: 5,
    maxRetries: 3,
    onFailure: 'ALERT',
    enabled: true,
    soapAction: 'getEntDept',
    soapNamespace: 'http://i.sync.common.pkuih.iih/',
    soapParams: [
      { name: 'arg0', value: '' },
      { name: 'arg1', value: '' }
    ],
    syncTimeParamStart: 'arg0',
    syncTimeParamEnd: 'arg1',
    syncTimeFormat: 'yyyy-MM-dd HH:mm:ss',
    firstSyncDays: 30,
    incrementalSyncHours: 24,
    isFirstSync: true,
    lastSyncTime: undefined,
    lastSyncStatus: undefined,
    lastSyncCount: undefined,
    mappingTables: [
      {
        mappingCode: 'TRANSFER_MAIN',
        mappingName: '转科记录映射',
        targetTable: 'transfer_record',
        dataPath: 'return',
        isArray: true,
        sortOrder: 0,
        fieldMappings: [
          { sourceField: 'code_ent', targetField: 'visit_no', transformType: 'DIRECT', isRequired: true, sortOrder: 0 },
          { sourceField: 'from_dep_code', targetField: 'from_dept_code', transformType: 'DIRECT', isRequired: false, sortOrder: 1 },
          { sourceField: 'from_dep_name', targetField: 'from_dept_name', transformType: 'DIRECT', isRequired: false, sortOrder: 2 },
          { sourceField: 'to_dep_code', targetField: 'to_dept_code', transformType: 'DIRECT', isRequired: true, sortOrder: 3 },
          { sourceField: 'to_dep_name', targetField: 'to_dept_name', transformType: 'DIRECT', isRequired: true, sortOrder: 4 },
          { sourceField: 'sv', targetField: 'transfer_time', transformType: 'DATE', isRequired: true, sortOrder: 5 }
        ],
        children: []
      }
    ]
  },
  {
    id: 6,
    configCode: 'HIS_DIAGNOSIS_SYNC',
    configName: 'HIS诊断信息同步',
    system: 'HIS',
    mode: 'PULL',
    protocol: 'HTTP',
    apiProtocol: 'SOAP',
    method: undefined,
    url: 'http://10.193.162.82:8089/iih.pkuih.common.sync.i.IHISSyncHoService?p=YWNjZXNzX3Rva2VuPWNmYTBjMzhmLWJjMGUtNDkxNC04M2I2LWY3NmI0ODViYzhhZQ==',
    dataType: 'DIAGNOSIS',
    syncMode: 'ON_DEMAND',
    syncSchedule: '',
    requestTemplate: '',
    authType: 'NONE',
    retryInterval: 5,
    maxRetries: 3,
    onFailure: 'ALERT',
    enabled: true,
    soapAction: 'getDiadef',
    soapNamespace: 'http://i.sync.common.pkuih.iih/',
    soapParams: [
      { name: 'arg0', value: '' }
    ],
    syncTimeParamStart: undefined,
    syncTimeParamEnd: undefined,
    syncTimeFormat: 'yyyy-MM-dd HH:mm:ss',
    firstSyncDays: 0,
    incrementalSyncHours: 24,
    isFirstSync: true,
    lastSyncTime: undefined,
    lastSyncStatus: undefined,
    lastSyncCount: undefined,
    mappingTables: [
      {
        mappingCode: 'DIAGNOSIS_MAIN',
        mappingName: '诊断主表映射',
        targetTable: 'diagnosis_main',
        dataPath: 'return',
        isArray: true,
        sortOrder: 0,
        fieldMappings: [
          { sourceField: 'code_ent', targetField: 'visit_no', transformType: 'DIRECT', isRequired: true, sortOrder: 0 },
          { sourceField: 'code_pat', targetField: 'patient_no', transformType: 'DIRECT', isRequired: true, sortOrder: 1 },
          { sourceField: 'sd_ditp', targetField: 'diagnosis_type_code', transformType: 'DIRECT', isRequired: true, sortOrder: 2 },
          { sourceField: 'name_ditp', targetField: 'diagnosis_type', transformType: 'DIRECT', isRequired: true, sortOrder: 3 },
          { sourceField: 'dt_di', targetField: 'diagnosis_time', transformType: 'DATE', isRequired: true, sortOrder: 4 },
          { sourceField: 'id_di', targetField: 'his_id', transformType: 'DIRECT', isRequired: true, sortOrder: 5 }
        ],
        children: [
          {
            mappingCode: 'DIAGNOSIS_ITEM',
            mappingName: '诊断子表映射',
            targetTable: 'diagnosis_item',
            dataPath: 'diitm',
            isArray: true,
            parentMappingId: 0,
            relationField: 'main_his_id',
            sortOrder: 0,
            fieldMappings: [
              { sourceField: 'code_didef', targetField: 'diagnosis_code', transformType: 'DIRECT', isRequired: true, sortOrder: 0 },
              { sourceField: 'name_didef', targetField: 'diagnosis_name', transformType: 'DIRECT', isRequired: true, sortOrder: 1 },
              { sourceField: 'fg_majdi', targetField: 'is_main', transformType: 'DIRECT', isRequired: true, sortOrder: 2 },
              { sourceField: 'sortno', targetField: 'sort_order', transformType: 'NUMBER', isRequired: false, sortOrder: 3 }
            ]
          }
        ]
      }
    ]
  },
  {
    id: 7,
    configCode: 'HIS_ORDER_SYNC',
    configName: 'HIS医嘱信息同步',
    system: 'HIS',
    mode: 'PULL',
    protocol: 'HTTP',
    apiProtocol: 'SOAP',
    method: undefined,
    url: 'http://10.193.162.82:8089/iih.pkuih.common.sync.i.IHISSyncHoService?p=YWNjZXNzX3Rva2VuPWNmYTBjMzhmLWJjMGUtNDkxNC04M2I2LWY3NmI0ODViYzhhZQ==',
    dataType: 'ORDER',
    syncMode: 'ON_DEMAND',
    syncSchedule: '',
    requestTemplate: '',
    authType: 'NONE',
    retryInterval: 5,
    maxRetries: 3,
    onFailure: 'ALERT',
    enabled: true,
    soapAction: 'getOrders',
    soapNamespace: 'http://i.sync.common.pkuih.iih/',
    soapParams: [
      { name: 'arg0', value: '' },
      { name: 'arg1', value: '' },
      { name: 'arg2', value: '' }
    ],
    syncTimeParamStart: 'arg1',
    syncTimeParamEnd: 'arg2',
    syncTimeFormat: 'yyyy-MM-dd HH:mm:ss',
    firstSyncDays: 30,
    incrementalSyncHours: 24,
    isFirstSync: true,
    lastSyncTime: undefined,
    lastSyncStatus: undefined,
    lastSyncCount: undefined,
    mappingTables: [
      {
        mappingCode: 'ORDER_MAIN',
        mappingName: '医嘱主表映射',
        targetTable: 'order_main',
        dataPath: 'return',
        isArray: true,
        sortOrder: 0,
        fieldMappings: [
          { sourceField: 'id_or', targetField: 'his_id', transformType: 'DIRECT', isRequired: true, sortOrder: 0 },
          { sourceField: 'code_ent', targetField: 'visit_no', transformType: 'DIRECT', isRequired: true, sortOrder: 1 },
          { sourceField: 'code_pat', targetField: 'patient_no', transformType: 'DIRECT', isRequired: true, sortOrder: 2 },
          { sourceField: 'code_or', targetField: 'order_no', transformType: 'DIRECT', isRequired: true, sortOrder: 3 },
          { sourceField: 'name_or', targetField: 'order_name', transformType: 'DIRECT', isRequired: true, sortOrder: 4 },
          { sourceField: 'fg_long', targetField: 'order_type', transformType: 'ENUM', isRequired: true, sortOrder: 5 },
          { sourceField: 'sd_srvtp', targetField: 'service_type', transformType: 'DIRECT', isRequired: true, sortOrder: 6 },
          { sourceField: 'dt_effe', targetField: 'start_time', transformType: 'DATE', isRequired: true, sortOrder: 7 },
          { sourceField: 'dt_end', targetField: 'end_time', transformType: 'DATE', isRequired: false, sortOrder: 8 }
        ],
        children: [
          {
            mappingCode: 'ORDER_ITEM',
            mappingName: '医嘱子表映射',
            targetTable: 'order_item',
            dataPath: 'ciitm',
            isArray: true,
            parentMappingId: 0,
            relationField: 'main_his_id',
            sortOrder: 0,
            fieldMappings: [
              { sourceField: 'id_orsrv', targetField: 'his_id', transformType: 'DIRECT', isRequired: true, sortOrder: 0 },
              { sourceField: 'name', targetField: 'item_name', transformType: 'DIRECT', isRequired: true, sortOrder: 1 },
              { sourceField: 'quan_medu', targetField: 'dosage', transformType: 'DIRECT', isRequired: false, sortOrder: 2 },
              { sourceField: 'name_medu', targetField: 'dosage_unit', transformType: 'DIRECT', isRequired: false, sortOrder: 3 },
              { sourceField: 'name_route', targetField: 'route', transformType: 'DIRECT', isRequired: false, sortOrder: 4 }
            ]
          }
        ]
      }
    ]
  },
  {
    id: 8,
    configCode: 'HIS_DOCTOR_DEPT_SYNC',
    configName: 'HIS科室人员关系同步',
    system: 'HIS',
    mode: 'PULL',
    protocol: 'HTTP',
    apiProtocol: 'SOAP',
    method: undefined,
    url: 'http://10.193.162.82:8089/iih.pkuih.common.sync.i.IHISSyncHoService?p=YWNjZXNzX3Rva2VuPWNmYTBjMzhmLWJjMGUtNDkxNC04M2I2LWY3NmI0ODViYzhhZQ==',
    dataType: 'DOCTOR_DEPT',
    syncMode: 'SCHEDULED',
    syncSchedule: '0 0 8 * * ?',
    requestTemplate: '',
    authType: 'NONE',
    retryInterval: 5,
    maxRetries: 3,
    onFailure: 'ALERT',
    enabled: true,
    soapAction: 'getDeptVsEmp',
    soapNamespace: 'http://i.sync.common.pkuih.iih/',
    soapParams: [
      { name: 'arg0', value: '' },
      { name: 'arg1', value: '' }
    ],
    syncTimeParamStart: 'arg0',
    syncTimeParamEnd: 'arg1',
    syncTimeFormat: 'yyyy-MM-dd HH:mm:ss',
    firstSyncDays: 0,
    incrementalSyncHours: 24,
    isFirstSync: true,
    lastSyncTime: undefined,
    lastSyncStatus: undefined,
    lastSyncCount: undefined,
    mappingTables: [
      {
        mappingCode: 'DOCTOR_DEPT_MAIN',
        mappingName: '科室人员关系映射',
        targetTable: 'doctor_department',
        dataPath: 'return',
        isArray: true,
        sortOrder: 0,
        fieldMappings: [
          { sourceField: 'code_user', targetField: 'code_user', transformType: 'DIRECT', isRequired: true, sortOrder: 0 },
          { sourceField: 'code_dept', targetField: 'code_dept', transformType: 'DIRECT', isRequired: true, sortOrder: 1 },
          { sourceField: 'name_user', targetField: 'name_user', transformType: 'DIRECT', isRequired: false, sortOrder: 2 },
          { sourceField: 'name_dept', targetField: 'name_dept', transformType: 'DIRECT', isRequired: false, sortOrder: 3 }
        ],
        children: []
      }
    ]
  }
]

let nextId = 9

export function getInterfaceConfigList(): InterfaceConfig[] {
  return [...interfaceConfigs]
}

export function getInterfaceConfigById(id: number): InterfaceConfig | null {
  return interfaceConfigs.find(c => c.id === id) || null
}

export function createInterfaceConfig(data: InterfaceConfig): InterfaceConfig {
  const newConfig = { ...data, id: nextId++ }
  interfaceConfigs.push(newConfig)
  return newConfig
}

export function updateInterfaceConfig(id: number, data: Partial<InterfaceConfig>): InterfaceConfig | null {
  const index = interfaceConfigs.findIndex(c => c.id === id)
  if (index === -1) return null
  const updated = { ...interfaceConfigs[index], ...data } as InterfaceConfig
  interfaceConfigs[index] = updated
  return updated
}

export function deleteInterfaceConfig(id: number): boolean {
  const index = interfaceConfigs.findIndex(c => c.id === id)
  if (index === -1) return false
  interfaceConfigs.splice(index, 1)
  return true
}

export function checkConfigCodeExists(code: string, excludeId?: number): boolean {
  return interfaceConfigs.some(c => c.configCode === code && c.id !== excludeId)
}