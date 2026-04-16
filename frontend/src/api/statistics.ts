import request from './request'

export interface StatisticsDto {
  deptId: number
  deptName: string
  totalHandovers: number
  completedHandovers: number
  draftHandovers: number
  totalPatients: number
  newAdmissionPatients: number
  level1NursingPatients: number
}

export function fetchDepartmentStatistics(deptId: number) {
  return request.get<StatisticsDto>(`/statistics/department/${deptId}`)
}

export function fetchAllDepartmentStatistics() {
  return request.get<StatisticsDto[]>('/statistics/departments')
}