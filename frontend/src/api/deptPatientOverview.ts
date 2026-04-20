import request from './request'

export interface DeptPatientOverview {
  deptCode: string
  deptId: string | null
  totalNum: number
  diseNum: number
  newInHos: number
  transIn: number
  transOut: number
  outNum: number
  surgNum: number
  deathNum: number
  syncedAt: string | null
}

export function fetchDeptPatientOverview(deptCode?: string): Promise<{ data: DeptPatientOverview }> {
  const params = deptCode ? { deptCode } : {}
  return request.get('/dept-patient-overview', { params })
}