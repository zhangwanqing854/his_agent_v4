import request from './request'
import type { ApiResponse } from './request'

export interface DashboardStatsDto {
  totalPatients: number
  completedHandovers: number
  pendingHandovers: number
}

export function fetchDashboardStats(deptCode: string, doctorId: number) {
  return request.get<ApiResponse<DashboardStatsDto>>(`/dashboard/stats?deptCode=${deptCode}&doctorId=${doctorId}`)
}