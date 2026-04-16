import request from './request'

export interface HisStaffDto {
  id: number
  staffCode: string
  name: string
  departmentId: number | null
  departmentName: string | null
  title: string | null
  phone: string | null
  syncTime: string | null
}

export interface HisStaffCreateRequest {
  staffCode: string
  name: string
  departmentId?: number
  title?: string
  phone?: string
}

export interface HisStaffUpdateRequest {
  name?: string
  departmentId?: number
  title?: string
  phone?: string
}

export function fetchHisStaffList(departmentId?: number) {
  return request.get<HisStaffDto[]>('/his-staff', { params: { departmentId } })
}

export function fetchHisStaffById(id: number) {
  return request.get<HisStaffDto>(`/his-staff/${id}`)
}

export function fetchUnlinkedHisStaff() {
  return request.get<HisStaffDto[]>('/his-staff/unlinked')
}

export function createHisStaff(data: HisStaffCreateRequest) {
  return request.post<HisStaffDto>('/his-staff', data)
}

export function updateHisStaff(id: number, data: HisStaffUpdateRequest) {
  return request.put<HisStaffDto>(`/his-staff/${id}`, data)
}

export function deleteHisStaff(id: number) {
  return request.delete<void>(`/his-staff/${id}`)
}