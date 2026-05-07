import request from './request'

export interface ImportError {
  lineNumber: number
  message: string
}

export interface ImportResult {
  totalCount: number
  insertCount: number
  updateCount: number
  skipCount: number
  failCount: number
  errors: ImportError[]
}

export function importPatient(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request.post<ImportResult>('/patient-import', formData)
}

export function importVisit(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request.post<ImportResult>('/visit-import', formData)
}

export interface PatientDto {
  id: number
  patientNo: string
  name: string
  gender: string
  age: number
  idCard: string | null
  phone: string | null
  address: string | null
}

export interface VisitDto {
  id: number
  visitNo: string
  patientId: number
  patientName: string
  departmentId: number
  departmentName: string
  bedNo: string
  diagnosis: string
  nursingLevel: string
  visitStatus: string
  admitTime: string | null
  dischargeTime: string | null
}

export interface PatientFilterRequest {
  departmentId?: number
  patientName?: string
  bedNo?: string
  visitStatus?: string
}

export function fetchPatientList(filter?: PatientFilterRequest) {
  return request.get<VisitDto[]>('/patients', { params: filter })
}

export function fetchPatientById(id: number) {
  return request.get<PatientDto>(`/patients/${id}`)
}

export function fetchVisitByPatientId(patientId: number) {
  return request.get<VisitDto>(`/patients/${patientId}/visit`)
}