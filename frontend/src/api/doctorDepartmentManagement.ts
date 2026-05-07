import request from './request'

export interface DoctorDepartmentManagementDto {
  id: number
  doctorId: number
  doctorName: string
  doctorUsercode: string
  departmentId: number
  departmentCode: string
  departmentName: string
  isPrimary: boolean
  createdAt: string
}

export interface DoctorDepartmentListResult {
  list: DoctorDepartmentManagementDto[]
  total: number
  page: number
  size: number
}

export function fetchDoctorDepartmentList(page: number, size: number, search?: string) {
  const params = new URLSearchParams()
  params.append('page', page.toString())
  params.append('size', size.toString())
  if (search) params.append('search', search)
  
  return request.get<DoctorDepartmentListResult>(`/doctor-department-management?${params.toString()}`)
}

export function createDoctorDepartment(doctorId: number, departmentId: number, isPrimary: boolean) {
  const params = new URLSearchParams()
  params.append('doctorId', doctorId.toString())
  params.append('departmentId', departmentId.toString())
  params.append('isPrimary', isPrimary.toString())
  
  return request.post<DoctorDepartmentManagementDto>(`/doctor-department-management?${params.toString()}`)
}

export function updateDoctorDepartment(id: number, isPrimary: boolean) {
  const params = new URLSearchParams()
  params.append('isPrimary', isPrimary.toString())
  
  return request.put<DoctorDepartmentManagementDto>(`/doctor-department-management/${id}?${params.toString()}`)
}

export function deleteDoctorDepartment(id: number) {
  return request.delete(`/doctor-department-management/${id}`)
}

export function setPrimaryDepartment(doctorId: number, departmentId: number) {
  const params = new URLSearchParams()
  params.append('doctorId', doctorId.toString())
  params.append('departmentId', departmentId.toString())
  
  return request.post(`/doctor-department-management/set-primary?${params.toString()}`)
}