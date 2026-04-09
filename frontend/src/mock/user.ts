import type { User, Role, Permission, Duty } from '@/types/user'

// 权限定义
export const permissions: Permission[] = [
  { id: 1, code: 'HANDOVER', name: '交班管理', description: '交班记录管理相关功能' },
  { id: 2, code: 'PATIENT', name: '患者管理', description: '患者信息管理相关功能' },
  { id: 3, code: 'TODO', name: '待办事项', description: '待办事项管理相关功能' },
  { id: 4, code: 'STATISTICS', name: '统计分析', description: '统计数据查看相关功能' },
  { id: 5, code: 'USER_MANAGE', name: '用户管理', description: '用户账号管理相关功能' },
  { id: 6, code: 'ROLE_MANAGE', name: '角色权限', description: '角色权限管理相关功能' },
  { id: 7, code: 'SYSTEM_SETTINGS', name: '系统设置', description: '系统配置管理相关功能' }
]

// 职责定义（对应具体功能）
export const duties: Duty[] = [
  // 交班管理
  { id: 1, code: 'HANDOVER_VIEW', name: '查看交班记录', description: '查看交班记录列表和详情', permissionId: 1 },
  { id: 2, code: 'HANDOVER_CREATE', name: '发起交班', description: '创建新的交班记录', permissionId: 1 },
  { id: 3, code: 'HANDOVER_EDIT', name: '编辑交班记录', description: '编辑交班记录内容', permissionId: 1 },
  { id: 4, code: 'HANDOVER_DELETE', name: '删除交班记录', description: '删除交班记录', permissionId: 1 },
  { id: 5, code: 'HANDOVER_REVIEW', name: '审核交班', description: '审核交班记录', permissionId: 1 },
  
  // 患者管理
  { id: 10, code: 'PATIENT_VIEW', name: '查看患者信息', description: '查看患者列表和详情', permissionId: 2 },
  { id: 11, code: 'PATIENT_EXPORT', name: '导出患者数据', description: '导出患者信息', permissionId: 2 },
  
  // 待办事项
  { id: 20, code: 'TODO_VIEW', name: '查看待办事项', description: '查看待办事项列表', permissionId: 3 },
  { id: 21, code: 'TODO_HANDLE', name: '处理待办事项', description: '处理待办事项', permissionId: 3 },
  
  // 统计分析
  { id: 30, code: 'STATISTICS_VIEW', name: '查看统计报表', description: '查看统计报表', permissionId: 4 },
  { id: 31, code: 'STATISTICS_EXPORT', name: '导出统计报表', description: '导出统计数据', permissionId: 4 },
  
  // 用户管理
  { id: 40, code: 'USER_VIEW', name: '查看用户列表', description: '查看用户列表和详情', permissionId: 5 },
  { id: 41, code: 'USER_CREATE', name: '新增用户', description: '创建新用户账号', permissionId: 5 },
  { id: 42, code: 'USER_EDIT', name: '编辑用户', description: '编辑用户信息', permissionId: 5 },
  { id: 43, code: 'USER_DELETE', name: '删除用户', description: '删除用户账号', permissionId: 5 },
  { id: 44, code: 'USER_ENABLE', name: '启用/禁用用户', description: '启用或禁用用户账号', permissionId: 5 },
  { id: 45, code: 'USER_RESET_PWD', name: '重置密码', description: '重置用户密码', permissionId: 5 },
  
  // 角色权限管理
  { id: 50, code: 'ROLE_VIEW', name: '查看角色列表', description: '查看角色列表和详情', permissionId: 6 },
  { id: 51, code: 'ROLE_CREATE', name: '新增角色', description: '创建新角色', permissionId: 6 },
  { id: 52, code: 'ROLE_EDIT', name: '编辑角色', description: '编辑角色信息', permissionId: 6 },
  { id: 53, code: 'ROLE_DELETE', name: '删除角色', description: '删除角色', permissionId: 6 },
  { id: 54, code: 'ROLE_ASSIGN_DUTY', name: '分配职责', description: '为角色分配职责', permissionId: 6 },
  
  // 系统设置
  { id: 60, code: 'SETTINGS_VIEW', name: '查看系统配置', description: '查看系统配置', permissionId: 7 },
  { id: 61, code: 'SETTINGS_EDIT', name: '修改系统配置', description: '修改系统配置', permissionId: 7 },
  { id: 62, code: 'INTERFACE_CONFIG', name: '接口配置管理', description: '管理接口配置', permissionId: 7 },
  { id: 63, code: 'INTERFACE_SYNC', name: '数据同步', description: '执行数据同步', permissionId: 7 }
]

// 角色定义
export const roles: Role[] = [
  { 
    id: 1, 
    name: '超级管理员', 
    code: 'SUPER_ADMIN', 
    isDefault: false, 
    description: '系统超级管理员，拥有所有权限',
    duties: duties // 拥有所有职责
  },
  { 
    id: 2, 
    name: '普通医生', 
    code: 'DOCTOR', 
    isDefault: true, 
    description: '普通医生角色',
    duties: duties.filter(d => [1, 2, 3, 4, 10, 11, 20, 21, 30, 31].includes(d.id))
  },
  { 
    id: 3, 
    name: '科室主任', 
    code: 'DEPT_HEAD', 
    isDefault: false, 
    description: '科室主任角色',
    duties: duties.filter(d => [1, 2, 3, 4, 5, 10, 11, 20, 21, 30, 31, 40, 44].includes(d.id))
  },
  { 
    id: 4, 
    name: '系统管理员', 
    code: 'SYSTEM_ADMIN', 
    isDefault: false, 
    description: '系统管理员，负责用户和权限管理',
    duties: duties.filter(d => d.permissionId >= 5) // 用户管理、角色权限、系统设置相关职责
  }
]

// HIS人员
export const hisStaffList = [
  { id: 1, staffCode: 'D001', name: '张三', departmentId: 1, departmentName: '心内科', title: '主任医师', phone: '13800138001', syncTime: '2026-03-25 10:00:00' },
  { id: 2, staffCode: 'D002', name: '李四', departmentId: 1, departmentName: '心内科', title: '副主任医师', phone: '13800138002', syncTime: '2026-03-25 10:00:00' },
  { id: 3, staffCode: 'D003', name: '王五', departmentId: 1, departmentName: '心内科', title: '主治医师', phone: '13800138003', syncTime: '2026-03-25 10:00:00' },
  { id: 4, staffCode: 'D004', name: '赵六', departmentId: 2, departmentName: '神经内科', title: '主任医师', phone: '13800138004', syncTime: '2026-03-25 10:00:00' },
  { id: 5, staffCode: 'D005', name: '钱七', departmentId: 2, departmentName: '神经内科', title: '副主任医师', phone: '13800138005', syncTime: '2026-03-25 10:00:00' },
  { id: 6, staffCode: 'D006', name: '孙八', departmentId: 3, departmentName: '呼吸科', title: '主任医师', phone: '13800138006', syncTime: '2026-03-25 10:00:00' },
  { id: 7, staffCode: 'D007', name: '周九', departmentId: 3, departmentName: '呼吸科', title: '主治医师', phone: '13800138007', syncTime: '2026-03-25 10:00:00' },
  { id: 8, staffCode: 'D008', name: '吴十', departmentId: 4, departmentName: '内分泌科', title: '副主任医师', phone: '13800138008', syncTime: '2026-03-25 10:00:00' }
]

// 用户数据
let users: User[] = [
  {
    id: 1,
    usercode: 'admin',
    username: 'admin',
    password: 'admin',
    isSuperAdmin: true,
    hisStaffId: null,
    roleId: 1,
    enabled: true,
    createdAt: '2026-03-01 00:00:00',
    updatedAt: '2026-03-01 00:00:00',
    role: roles[0]
  },
  {
    id: 2,
    usercode: 'd001',
    username: 'D001',
    password: 'D001',
    isSuperAdmin: false,
    hisStaffId: 1,
    roleId: 3,
    enabled: true,
    createdAt: '2026-03-25 10:00:00',
    updatedAt: '2026-03-25 10:00:00',
    hisStaff: hisStaffList[0],
    role: roles[2]
  },
  {
    id: 3,
    usercode: 'd002',
    username: 'D002',
    password: 'D002',
    isSuperAdmin: false,
    hisStaffId: 2,
    roleId: 2,
    enabled: true,
    createdAt: '2026-03-25 10:00:00',
    updatedAt: '2026-03-25 10:00:00',
    hisStaff: hisStaffList[1],
    role: roles[1]
  },
  {
    id: 4,
    usercode: 'd003',
    username: 'D003',
    password: 'D003',
    isSuperAdmin: false,
    hisStaffId: 3,
    roleId: 2,
    enabled: true,
    createdAt: '2026-03-25 10:00:00',
    updatedAt: '2026-03-25 10:00:00',
    hisStaff: hisStaffList[2],
    role: roles[1]
  },
  {
    id: 5,
    usercode: 'sysadmin',
    username: 'sysadmin',
    password: 'sysadmin',
    isSuperAdmin: false,
    hisStaffId: null,
    roleId: 4,
    enabled: true,
    createdAt: '2026-03-25 10:00:00',
    updatedAt: '2026-03-25 10:00:00',
    role: roles[3]
  }
]

let nextUserId = 6
let nextRoleId = 5

// ==================== 用户相关函数 ====================

export function getUserList(filter?: { username?: string; roleId?: number | null; enabled?: boolean | null }): User[] {
  let result = [...users]
  
  if (filter) {
    if (filter.username) {
      result = result.filter(u => u.username.includes(filter.username!))
    }
    if (filter.roleId !== null && filter.roleId !== undefined) {
      result = result.filter(u => u.roleId === filter.roleId)
    }
    if (filter.enabled !== null && filter.enabled !== undefined) {
      result = result.filter(u => u.enabled === filter.enabled)
    }
  }
  
  return result.sort((a, b) => b.id - a.id)
}

export function getUserById(id: number): User | null {
  return users.find(u => u.id === id) || null
}

export function createUser(data: Omit<User, 'id' | 'createdAt' | 'updatedAt' | 'role' | 'hisStaff'>): User {
  const now = new Date().toISOString().slice(0, 19).replace('T', ' ')
  const role = roles.find(r => r.id === data.roleId)
  const hisStaff = data.hisStaffId ? hisStaffList.find(s => s.id === data.hisStaffId) : undefined
  const newUser: User = {
    ...data,
    id: nextUserId++,
    createdAt: now,
    updatedAt: now,
    role,
    hisStaff
  }
  users.push(newUser)
  return newUser
}

export function updateUser(id: number, data: Partial<Omit<User, 'id' | 'createdAt' | 'updatedAt'>>): User | null {
  const index = users.findIndex(u => u.id === id)
  if (index === -1) return null
  
  const user = users[index]
  const role = data.roleId !== undefined ? roles.find(r => r.id === data.roleId) : user.role
  const hisStaff = data.hisStaffId !== undefined 
    ? (data.hisStaffId ? hisStaffList.find(s => s.id === data.hisStaffId) : undefined)
    : user.hisStaff
  
  users[index] = {
    ...user,
    ...data,
    updatedAt: new Date().toISOString().slice(0, 19).replace('T', ' '),
    role,
    hisStaff
  }
  return users[index]
}

export function deleteUser(id: number): boolean {
  const user = users.find(u => u.id === id)
  if (!user) return false
  if (user.username === 'admin' && user.isSuperAdmin) return false
  
  users = users.filter(u => u.id !== id)
  return true
}

export function enableUser(id: number): User | null {
  return updateUser(id, { enabled: true })
}

export function disableUser(id: number): User | null {
  const user = users.find(u => u.id === id)
  if (!user) return null
  if (user.username === 'admin' && user.isSuperAdmin) return null
  
  return updateUser(id, { enabled: false })
}

export function resetPassword(id: number, newPassword: string): User | null {
  return updateUser(id, { password: newPassword })
}

export function checkUsernameExists(username: string, excludeId?: number): boolean {
  return users.some(u => u.username === username && u.id !== excludeId)
}

export function checkUsercodeExists(usercode: string, excludeId?: number): boolean {
  const normalized = usercode.toLowerCase()
  return users.some(u => 
    u.usercode.toLowerCase() === normalized && u.id !== excludeId
  )
}

export function getHisStaffList() {
  return [...hisStaffList]
}

export function getUnlinkedHisStaffList() {
  const linkedIds = users.filter(u => u.hisStaffId).map(u => u.hisStaffId as number)
  return hisStaffList.filter(s => !linkedIds.includes(s.id))
}

// ==================== 角色相关函数 ====================

export function getRoleList(): Role[] {
  return roles.map(role => ({
    ...role,
    duties: duties.filter(d => role.duties?.some(rd => rd.id === d.id))
  }))
}

export function getRoleById(id: number): Role | null {
  const role = roles.find(r => r.id === id)
  if (!role) return null
  return {
    ...role,
    duties: duties.filter(d => role.duties?.some(rd => rd.id === d.id))
  }
}

export function createRole(data: { name: string; code: string; description: string; isDefault: boolean; dutyIds: number[] }): Role {
  const newRole: Role = {
    id: nextRoleId++,
    name: data.name,
    code: data.code,
    description: data.description,
    isDefault: data.isDefault,
    duties: duties.filter(d => data.dutyIds.includes(d.id))
  }
  roles.push(newRole)
  return newRole
}

export function updateRole(id: number, data: Partial<{ name: string; code: string; description: string; isDefault: boolean; dutyIds: number[] }>): Role | null {
  const index = roles.findIndex(r => r.id === id)
  if (index === -1) return null
  
  const role = roles[index]
  
  // 超级管理员不能修改
  if (role.code === 'SUPER_ADMIN') return null
  
  roles[index] = {
    ...role,
    ...data,
    duties: data.dutyIds ? duties.filter(d => data.dutyIds!.includes(d.id)) : role.duties
  }
  return roles[index]
}

export function deleteRole(id: number): boolean {
  const role = roles.find(r => r.id === id)
  if (!role) return false
  
  // 预置角色不能删除
  if (['SUPER_ADMIN', 'DOCTOR'].includes(role.code)) return false
  
  // 检查是否有用户在使用此角色
  const usersWithRole = users.filter(u => u.roleId === id)
  if (usersWithRole.length > 0) return false
  
  const index = roles.findIndex(r => r.id === id)
  if (index > -1) {
    roles.splice(index, 1)
    return true
  }
  return false
}

export function checkRoleCodeExists(code: string, excludeId?: number): boolean {
  return roles.some(r => r.code === code && r.id !== excludeId)
}

// ==================== 权限相关函数 ====================

export function getPermissionList(): Permission[] {
  return permissions.map(perm => ({
    ...perm,
    duties: duties.filter(d => d.permissionId === perm.id)
  }))
}

export function getPermissionById(id: number): Permission | null {
  const perm = permissions.find(p => p.id === id)
  if (!perm) return null
  return {
    ...perm,
    duties: duties.filter(d => d.permissionId === perm.id)
  }
}

// ==================== 职责相关函数 ====================

export function getDutyList(): Duty[] {
  return duties.map(duty => ({
    ...duty,
    permission: permissions.find(p => p.id === duty.permissionId)
  }))
}

export function getDutiesByPermissionId(permissionId: number): Duty[] {
  return duties.filter(d => d.permissionId === permissionId)
}

export function getDutiesByRoleId(roleId: number): Duty[] {
  const role = roles.find(r => r.id === roleId)
  if (!role || !role.duties) return []
  return role.duties
}