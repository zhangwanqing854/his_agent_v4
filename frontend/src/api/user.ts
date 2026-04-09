import request from './request'
import type { User, UserFilter, Role, Permission, Duty, RoleFormData } from '@/types/user'
import { checkUsercodeExists } from '@/mock/user'

const USE_MOCK = false

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export function fetchUserList(filter?: UserFilter): Promise<ApiResponse<User[]>> {
  if (USE_MOCK) {
    const users = getUserList({
      username: filter?.username,
      roleId: filter?.roleId,
      enabled: filter?.enabled
    })
    return Promise.resolve({
      code: 0,
      message: 'success',
      data: users
    })
  }
  return request.get('/users', { params: filter })
}

export function fetchUserById(id: number): Promise<ApiResponse<User | null>> {
  if (USE_MOCK) {
    const user = getUserById(id)
    return Promise.resolve({
      code: 0,
      message: 'success',
      data: user
    })
  }
  return request.get(`/users/${id}`)
}

export function createUserApi(data: {
  username: string
  password: string
  roleId: number
  hisStaffId: number | null
  enabled: boolean
}): Promise<ApiResponse<User>> {
  if (USE_MOCK) {
    if (checkUsernameExists(data.username)) {
      return Promise.resolve({
        code: 1,
        message: '用户名已存在',
        data: null as unknown as User
      })
    }
    const user = createUser({
      username: data.username,
      password: data.password,
      isSuperAdmin: false,
      hisStaffId: data.hisStaffId,
      roleId: data.roleId,
      enabled: data.enabled
    })
    return Promise.resolve({
      code: 0,
      message: '创建成功',
      data: user
    })
  }
  return request.post('/users', data)
}

export function updateUserApi(id: number, data: {
  username?: string
  password?: string
  roleId?: number
  hisStaffId?: number | null
  enabled?: boolean
}): Promise<ApiResponse<User | null>> {
  if (USE_MOCK) {
    if (data.username && checkUsernameExists(data.username, id)) {
      return Promise.resolve({
        code: 1,
        message: '用户名已存在',
        data: null
      })
    }
    const user = updateUser(id, data)
    if (user) {
      return Promise.resolve({
        code: 0,
        message: '更新成功',
        data: user
      })
    }
    return Promise.resolve({
      code: 1,
      message: '用户不存在',
      data: null
    })
  }
  return request.put(`/users/${id}`, data)
}

export function deleteUserApi(id: number): Promise<ApiResponse<boolean>> {
  if (USE_MOCK) {
    const success = deleteUser(id)
    if (success) {
      return Promise.resolve({
        code: 0,
        message: '删除成功',
        data: true
      })
    }
    return Promise.resolve({
      code: 1,
      message: '无法删除超级管理员账号',
      data: false
    })
  }
  return request.delete(`/users/${id}`)
}

export function enableUserApi(id: number): Promise<ApiResponse<User | null>> {
  if (USE_MOCK) {
    const user = enableUser(id)
    if (user) {
      return Promise.resolve({
        code: 0,
        message: '启用成功',
        data: user
      })
    }
    return Promise.resolve({
      code: 1,
      message: '用户不存在',
      data: null
    })
  }
  return request.post(`/users/${id}/enable`)
}

export function disableUserApi(id: number): Promise<ApiResponse<User | null>> {
  if (USE_MOCK) {
    const user = disableUser(id)
    if (user) {
      return Promise.resolve({
        code: 0,
        message: '禁用成功',
        data: user
      })
    }
    return Promise.resolve({
      code: 1,
      message: '无法禁用超级管理员账号',
      data: null
    })
  }
  return request.post(`/users/${id}/disable`)
}

export function resetPasswordApi(id: number, newPassword: string): Promise<ApiResponse<User | null>> {
  if (USE_MOCK) {
    const user = resetPassword(id, newPassword)
    if (user) {
      return Promise.resolve({
        code: 0,
        message: '密码重置成功',
        data: user
      })
    }
    return Promise.resolve({
      code: 1,
      message: '用户不存在',
      data: null
    })
  }
  return request.post(`/users/${id}/reset-password`, { newPassword })
}

export function fetchHisStaffList(): Promise<ApiResponse<{ id: number; staffCode: string; name: string; departmentId: number; departmentName: string; title: string; phone: string; syncTime: string }[]>> {
  if (USE_MOCK) {
    return Promise.resolve({
      code: 0,
      message: 'success',
      data: getHisStaffList()
    })
  }
  return request.get('/his-staff')
}

export function fetchUnlinkedHisStaffList(): Promise<ApiResponse<{ id: number; staffCode: string; name: string; departmentId: number; departmentName: string; title: string; phone: string; syncTime: string }[]>> {
  if (USE_MOCK) {
    return Promise.resolve({
      code: 0,
      message: 'success',
      data: getUnlinkedHisStaffList()
    })
  }
  return request.get('/his-staff/unlinked')
}

export function fetchRoleList(): Promise<ApiResponse<Role[]>> {
  if (USE_MOCK) {
    return Promise.resolve({
      code: 0,
      message: 'success',
      data: getRoleList()
    })
  }
  return request.get('/roles')
}

export function fetchRoleById(id: number): Promise<ApiResponse<Role | null>> {
  if (USE_MOCK) {
    const role = getRoleById(id)
    return Promise.resolve({
      code: 0,
      message: 'success',
      data: role
    })
  }
  return request.get(`/roles/${id}`)
}

export function createRoleApi(data: RoleFormData): Promise<ApiResponse<Role>> {
  if (USE_MOCK) {
    if (checkRoleCodeExists(data.code)) {
      return Promise.resolve({
        code: 1,
        message: '角色编码已存在',
        data: null as unknown as Role
      })
    }
    const role = createRole({
      name: data.name,
      code: data.code,
      description: data.description,
      isDefault: data.isDefault,
      dutyIds: data.dutyIds
    })
    return Promise.resolve({
      code: 0,
      message: '创建成功',
      data: role
    })
  }
  return request.post('/roles', data)
}

export function updateRoleApi(id: number, data: Partial<RoleFormData>): Promise<ApiResponse<Role | null>> {
  if (USE_MOCK) {
    if (data.code && checkRoleCodeExists(data.code, id)) {
      return Promise.resolve({
        code: 1,
        message: '角色编码已存在',
        data: null
      })
    }
    const role = updateRole(id, data)
    if (role) {
      return Promise.resolve({
        code: 0,
        message: '更新成功',
        data: role
      })
    }
    return Promise.resolve({
      code: 1,
      message: '无法修改超级管理员角色',
      data: null
    })
  }
  return request.put(`/roles/${id}`, data)
}

export function deleteRoleApi(id: number): Promise<ApiResponse<boolean>> {
  if (USE_MOCK) {
    const success = deleteRole(id)
    if (success) {
      return Promise.resolve({
        code: 0,
        message: '删除成功',
        data: true
      })
    }
    return Promise.resolve({
      code: 1,
      message: '无法删除预置角色或有用户使用的角色',
      data: false
    })
  }
  return request.delete(`/roles/${id}`)
}

export function fetchPermissionList(): Promise<ApiResponse<Permission[]>> {
  if (USE_MOCK) {
    return Promise.resolve({
      code: 0,
      message: 'success',
      data: getPermissionList()
    })
  }
  return request.get('/permissions')
}

export function fetchPermissionById(id: number): Promise<ApiResponse<Permission | null>> {
  if (USE_MOCK) {
    const perm = getPermissionById(id)
    return Promise.resolve({
      code: 0,
      message: 'success',
      data: perm
    })
  }
  return request.get(`/permissions/${id}`)
}

export function fetchDutyList(): Promise<ApiResponse<Duty[]>> {
  if (USE_MOCK) {
    return Promise.resolve({
      code: 0,
      message: 'success',
      data: getDutyList()
    })
  }
  return request.get('/duties')
}

export function fetchDutiesByPermission(permissionId: number): Promise<ApiResponse<Duty[]>> {
  if (USE_MOCK) {
    return Promise.resolve({
      code: 0,
      message: 'success',
      data: getDutiesByPermissionId(permissionId)
    })
  }
  return request.get(`/permissions/${permissionId}/duties`)
}

export function fetchDutiesByRole(roleId: number): Promise<ApiResponse<Duty[]>> {
  if (USE_MOCK) {
    return Promise.resolve({
      code: 0,
      message: 'success',
      data: getDutiesByRoleId(roleId)
    })
  }
  return request.get(`/roles/${roleId}/duties`)
}

export { checkUsercodeExists }