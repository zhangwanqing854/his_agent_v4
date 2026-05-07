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

export function importDoctorDepartment(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request.post<ImportResult>('/doctor-department-import', formData)
}