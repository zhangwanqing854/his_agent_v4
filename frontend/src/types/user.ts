export interface User {
  id: number
  usercode: string
  username: string
  password?: string
  isSuperAdmin: boolean
  hisStaffId: number | null
  roleId: number
  enabled: boolean
  createdAt: string
  updatedAt: string
  hisStaff?: HisStaff
  role?: Role
}

export interface Role {
  id: number
  name: string
  code: string
  isDefault: boolean
  description: string
  duties?: Duty[]
}

export interface Duty {
  id: number
  code: string
  name: string
  description: string
}

export interface HisStaff {
  id: number
  staffCode: string
  name: string
  departmentId: number
  departmentName: string
  title: string
  phone: string
  syncTime: string
}

export interface UserFilter {
  username: string
  roleId: number | null
  enabled: boolean | null
}

export interface UserFormData {
  id?: number
  usercode: string
  username: string
  password: string
  roleId: number
  hisStaffId: number | null
  enabled: boolean
}

export interface RoleFormData {
  id?: number
  name: string
  code: string
  description: string
  isDefault: boolean
  dutyIds: number[]
}

export interface UserInfo {
  id: number
  hisStaffId: number | null
  username: string
  usercode: string
  name: string
  role: string
  avatar: string
  isSuperAdmin: boolean
  departments: Department[]
  duties?: Duty[]
}

export interface Department {
  id: number
  code: string
  name: string
  isPrimary: boolean
}