import request from './request'
import type { ApiResponse } from './request'

export interface InpatientDto {
  id: number
  bedNo: string
  patientNo: string
  patientName: string
  nurseLevel: string
  isCritical: boolean
  admissionDatetime: string
  patientStatus: string
  deptName: string
}

export function fetchInpatients(deptId: number) {
  return request.get<ApiResponse<InpatientDto[]>>(`/visits/inpatients?deptId=${deptId}`)
}