import request from './request'

export interface DepartmentDto {
  id: number
  code: string
  name: string
  bedCount: number
}

export function fetchDepartmentList() {
  return request.get<DepartmentDto[]>('/departments')
}

export function fetchDepartmentById(id: number) {
  return request.get<DepartmentDto>(`/departments/${id}`)
}

export interface DepartmentCreateRequest {
  code: string
  name: string
  bedCount?: number
}

export interface DepartmentUpdateRequest {
  name?: string
  bedCount?: number
}

export function createDepartment(data: DepartmentCreateRequest) {
  return request.post<DepartmentDto>('/departments', data)
}

export function updateDepartment(id: number, data: DepartmentUpdateRequest) {
  return request.put<DepartmentDto>(`/departments/${id}`, data)
}

export function deleteDepartment(id: number) {
  return request.delete<void>(`/departments/${id}`)
}