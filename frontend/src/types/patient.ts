// 患者完整信息接口
export interface Patient {
  id: number
  bedNumber: string
  name: string
  age: number
  gender: '男' | '女'
  admissionDate: string
  hospitalId: string
  department: string
  departmentId: number
  diagnosis: string
  attendingDoctor: string
  nurseLevel: '特级' | '一级' | '二级' | '三级'
  isCritical: boolean
  allergies: string[]
  medicalHistory: string[]
  mewsScore: number
  bradenScore: number
  fallRisk: number
  vitalsHistory: VitalRecord[]
  nursingNotes: string
  handoverRecords: HandoverRecord[]
}

// 生命体征记录
export interface VitalRecord {
  time: string
  temperature: number
  pulse: number
  respiration: number
  systolicBp: number
  diastolicBp: number
  oxygenSaturation: number
}

// 交班记录（简化）
export interface HandoverRecord {
  id: number
  time: string
  fromDoctor: string
  toDoctor: string
  summary: string
}

// 简化患者信息（用于表格/卡片列表）
export interface PatientListItem {
  id: number
  bedNumber: string
  name: string
  age: number
  gender: '男' | '女'
  admissionDate: string
  diagnosis: string
  attendingDoctor: string
  nurseLevel: '特级' | '一级' | '二级' | '三级'
  isCritical: boolean
  allergies: string[]
  mewsScore: number
  bradenScore: number
  fallRisk: number
  latestVitals: VitalRecord
}

// 筛选项类型
export interface PatientFilter {
  bedNumber: string
  name: string
  nurseLevel: '' | '特级' | '一级' | '二级' | '三级'
  riskLevel: '' | '高风险' | '中风险' | '低风险'
  admissionDateStart: string
  admissionDateEnd: string
}

export interface PatientHandover {
  id: number
  bedNumber: string
  name: string
  age: number
  gender: '男' | '女'
  diagnosis: string
  isCritical: boolean
  vitals: string
  currentCondition: string
  observationItems: string
}

export interface HandoverForm {
  handoverNo?: string | null
  shift: '白班' | '夜班'
  toDoctorId: number | null
  patients: PatientHandover[]
  stats: {
    admission: number
    transferOut: number
    discharge: number
    transferIn: number
    death: number
    surgery: number
  }
}

export interface PatientInfo {
  id: number
  bedNumber: string
  name: string
  diagnosis: string
  condition: string
}