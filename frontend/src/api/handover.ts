import request from './request'
import type { ApiResponse } from './request'

export interface HandoverDto {
  id: number
  deptId: number
  deptName: string
  handoverNo: string | null
  handoverDate: string
  shift: string
  fromDoctorId: number
  fromDoctorName: string
  toDoctorId: number | null
  toDoctorName: string | null
  status: string
  patientCount: number
  createdAt: string
  updatedAt: string
}

export interface HandoverPatientDto {
  id: number
  visitId: number
  visitNo: string
  patientId: number
  patientName: string
  gender: string
  age: number
  bedNo: string
  diagnosis: string
  filterReason: string
  currentCondition: string
  vitals: string | null
  observationItems: string | null
  mewsScore: number | null
  bradenScore: number | null
  fallRisk: number | null
}

export interface HandoverCreateRequest {
  deptId: number
  handoverDate: string
  shift: string
  fromDoctorId: number
  toDoctorId: number
}

export interface HandoverStatsDto {
  totalPatients: number
  admission: number
  transferOut: number
  discharge: number
  transferIn: number
}

export interface DutyStaffDto {
  staffId: number
  staffName: string
}

export interface DutyStaffListItemDto {
  id: number
  departmentId: number
  staffId: number
  staffCode: string
  staffName: string
  title: string
  displayOrder: number
}

export function fetchHandoverList(deptId: number): Promise<ApiResponse<HandoverDto[]>> {
  return request.get('/handovers', { params: { deptId } }) as Promise<ApiResponse<HandoverDto[]>>
}

export function fetchHandoverById(id: number): Promise<ApiResponse<HandoverDto>> {
  return request.get(`/handovers/${id}`) as Promise<ApiResponse<HandoverDto>>
}

export function fetchHandoverPatients(handoverId: number): Promise<ApiResponse<HandoverPatientDto[]>> {
  return request.get(`/handovers/${handoverId}/patients`) as Promise<ApiResponse<HandoverPatientDto[]>>
}

export function createHandover(data: HandoverCreateRequest): Promise<ApiResponse<HandoverDto>> {
  return request.post('/handovers', data) as Promise<ApiResponse<HandoverDto>>
}

export function fetchHandoverStats(deptId: number): Promise<ApiResponse<HandoverStatsDto>> {
  return request.get('/handovers/stats', { params: { deptId } }) as Promise<ApiResponse<HandoverStatsDto>>
}

export function fetchDutyStaff(deptId: number): Promise<ApiResponse<DutyStaffDto>> {
  return request.get('/handovers/duty-staff', { params: { deptId } }) as Promise<ApiResponse<DutyStaffDto>>
}

export function fetchDutyStaffList(): Promise<ApiResponse<DutyStaffListItemDto[]>> {
  return request.get('/scheduling/duty-staff') as Promise<ApiResponse<DutyStaffListItemDto[]>>
}

export function fetchHandoverPatientsForCreate(deptId: number): Promise<ApiResponse<HandoverPatientDto[]>> {
  return request.get('/handovers/handover-patients', { params: { deptId } }) as Promise<ApiResponse<HandoverPatientDto[]>>
}

export function deleteHandover(id: number): Promise<ApiResponse<void>> {
  return request.delete(`/handovers/${id}`) as Promise<ApiResponse<void>>
}

export function submitHandover(id: number): Promise<ApiResponse<HandoverDto>> {
  return request.post(`/handovers/${id}/submit`) as Promise<ApiResponse<HandoverDto>>
}

export function updateHandover(id: number, data: HandoverCreateRequest): Promise<ApiResponse<HandoverDto>> {
  return request.put(`/handovers/${id}`, data) as Promise<ApiResponse<HandoverDto>>
}

export function acceptHandover(id: number): Promise<ApiResponse<HandoverDto>> {
  return request.post(`/handovers/${id}/accept`) as Promise<ApiResponse<HandoverDto>>
}

export function rejectHandover(id: number, reason: string): Promise<ApiResponse<HandoverDto>> {
  return request.post(`/handovers/${id}/reject`, { reason }) as Promise<ApiResponse<HandoverDto>>
}

export function confirmHandover(id: number): Promise<ApiResponse<HandoverDto>> {
  return request.post(`/handovers/${id}/confirm`) as Promise<ApiResponse<HandoverDto>>
}

export interface TodoItemDto {
  id: number
  handoverId: number
  content: string
  dueTime: string | null
  status: string
  createdAt: string
}

export function getHandoverTodos(handoverId: number): Promise<ApiResponse<TodoItemDto[]>> {
  return request.get(`/handovers/${handoverId}/todos`) as Promise<ApiResponse<TodoItemDto[]>>
}

export function createHandoverTodo(handoverId: number, content: string, dueTime?: string): Promise<ApiResponse<TodoItemDto>> {
  return request.post(`/handovers/${handoverId}/todos`, { content, dueTime }) as Promise<ApiResponse<TodoItemDto>>
}

export interface HandoverPatientUpdateRequest {
  vitals?: string
  currentCondition?: string
  observationItems?: string
}

export function updateHandoverPatient(
  handoverId: number,
  patientId: number,
  data: HandoverPatientUpdateRequest
): Promise<ApiResponse<HandoverPatientDto>> {
  return request.put(`/handovers/${handoverId}/patients/${patientId}`, data) as Promise<ApiResponse<HandoverPatientDto>>
}

export function completeTodo(todoId: number): Promise<ApiResponse<TodoItemDto>> {
  return request.put(`/handovers/todos/${todoId}/complete`) as Promise<ApiResponse<TodoItemDto>>
}