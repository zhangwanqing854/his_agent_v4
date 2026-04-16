import request from './request'
import { useAuthStore } from '@/stores/auth'

export interface SchedulingDetail {
  id: number
  dutyDate: string
  staffId: number | null
  staffName: string | null
  remark: string
}

export interface Scheduling {
  id: number
  departmentId: number
  departmentName: string
  yearMonth: string
  status: 'draft' | 'published'
  details: SchedulingDetail[]
  createdBy: number
  createdByName: string
  createdAt: string
  updatedAt: string
}

export interface SchedulingListItem {
  id: number
  departmentId: number
  departmentName: string
  yearMonth: string
  status: 'draft' | 'published'
  createdBy: number
  createdByName: string
  createdAt: string
}

export interface Staff {
  id: number
  name: string
  staffCode: string
  title: string
}

export interface SchedulingConfig {
  departmentId: number
  staffOrder: number[]
  lastPosition: number | null
  updatedAt: string
}

export interface DutyStaff {
  id: number
  departmentId: number
  staffId: number
  staffCode: string
  staffName: string
  title: string
  displayOrder: number
}

const USE_MOCK = false

const mockSchedulingConfig: SchedulingConfig = {
  departmentId: 190,
  staffOrder: [1, 2, 3, 4, 5],
  lastPosition: null
}

const mockStaffList: Staff[] = [
  { id: 1, name: '张护士', staffCode: 'N001', title: '护士' },
  { id: 2, name: '李护士', staffCode: 'N002', title: '护士' },
  { id: 3, name: '王护士', staffCode: 'N003', title: '护士' },
  { id: 4, name: '赵护士', staffCode: 'N004', title: '护士' },
  { id: 5, name: '刘护士', staffCode: 'N005', title: '护士长' }
]

// 存储排班详情数据
const mockSchedulingDetails: Record<number, SchedulingDetail[]> = {
  1: [], // 2026-04 - 已有数据，会在首次加载时生成
  2: []  // 2026-05 - 空白排班
}

// 生成一个月的排班明细
function generateMonthDetails(yearMonth: string): SchedulingDetail[] {
  const [year, month] = yearMonth.split('-')
  const daysInMonth = new Date(parseInt(year), parseInt(month), 0).getDate()
  const details: SchedulingDetail[] = []
  
  for (let day = 1; day <= daysInMonth; day++) {
    const dutyDate = `${yearMonth}-${day.toString().padStart(2, '0')}`
    const staffIndex = Math.floor(Math.random() * mockStaffList.length)
    const staff = mockStaffList[staffIndex]
    
    details.push({
      id: day,
      dutyDate,
      staffId: staff.id,
      staffName: staff.name,
      remark: ''
    })
  }
  
  return details
}

// 生成空白排班明细
function generateEmptyDetails(yearMonth: string): SchedulingDetail[] {
  const [year, month] = yearMonth.split('-')
  const daysInMonth = new Date(parseInt(year), parseInt(month), 0).getDate()
  const details: SchedulingDetail[] = []
  
  for (let day = 1; day <= daysInMonth; day++) {
    const dutyDate = `${yearMonth}-${day.toString().padStart(2, '0')}`
    
    details.push({
      id: day,
      dutyDate,
      staffId: null,
      staffName: null,
      remark: ''
    })
  }
  
  return details
}

const mockSchedulingList: SchedulingListItem[] = [
  {
    id: 1,
    departmentId: 190,
    departmentName: '康复医学科病房',
    yearMonth: '2026-04',
    status: 'published',
    createdBy: 1,
    createdByName: 'admin',
    createdAt: '2026-04-01 10:00:00'
  },
  {
    id: 2,
    departmentId: 190,
    departmentName: '康复医学科病房',
    yearMonth: '2026-05',
    status: 'draft',
    createdBy: 1,
    createdByName: 'admin',
    createdAt: '2026-04-10 14:00:00'
  }
]

// 获取排班列表
export async function fetchSchedulingList(yearMonth?: string): Promise<{ code: number; data: SchedulingListItem[] }> {
  if (USE_MOCK) {
    let list = mockSchedulingList
    if (yearMonth) {
      list = list.filter(item => item.yearMonth === yearMonth)
    }
    return Promise.resolve({ code: 0, data: list })
  }
  return request.get('/scheduling', { params: { yearMonth } })
}

// 获取排班详情
export async function fetchSchedulingDetail(id: number): Promise<{ code: number; data: Scheduling }> {
  if (USE_MOCK) {
    const item = mockSchedulingList.find(s => s.id === id)
    if (!item) {
      return Promise.resolve({ code: 1, data: null as any })
    }
    
    let details = mockSchedulingDetails[id]
    if (!details) {
      details = generateEmptyDetails(item.yearMonth)
      mockSchedulingDetails[id] = details
    }
    
    const detail: Scheduling = {
      ...item,
      details,
      updatedAt: item.createdAt
    }
    
    return Promise.resolve({ code: 0, data: detail })
  }
  return request.get(`/scheduling/${id}`)
}

// 创建排班计划
export async function createScheduling(yearMonth: string): Promise<{ code: number; data: SchedulingListItem }> {
  if (USE_MOCK) {
    const authStore = useAuthStore()
    const newId = mockSchedulingList.length + 1
    const newItem: SchedulingListItem = {
      id: newId,
      departmentId: authStore.currentDepartmentId || 190,
      departmentName: authStore.currentDepartmentName || '康复医学科病房',
      yearMonth,
      status: 'draft',
      createdBy: 1,
      createdByName: 'admin',
      createdAt: new Date().toISOString().replace('T', ' ').substring(0, 19)
    }
    mockSchedulingList.push(newItem)
    mockSchedulingDetails[newId] = generateEmptyDetails(yearMonth)
    return Promise.resolve({ code: 0, data: newItem })
  }
  return request.post('/scheduling', { yearMonth })
}

// 更新排班状态
export async function updateSchedulingStatus(id: number, status: 'draft' | 'published'): Promise<{ code: number; message: string }> {
  if (USE_MOCK) {
    const item = mockSchedulingList.find(s => s.id === id)
    if (item) {
      item.status = status
    }
    return Promise.resolve({ code: 0, message: '更新成功' })
  }
  return request.put(`/scheduling/${id}`, { status })
}

// 删除排班计划
export async function deleteScheduling(id: number): Promise<{ code: number; message: string }> {
  if (USE_MOCK) {
    const index = mockSchedulingList.findIndex(s => s.id === id)
    if (index > -1) {
      mockSchedulingList.splice(index, 1)
      delete mockSchedulingDetails[id]
    }
    return Promise.resolve({ code: 0, message: '删除成功' })
  }
  return request.delete(`/scheduling/${id}`)
}

// 批量更新排班明细
export async function updateSchedulingDetails(
  id: number,
  details: { dutyDate: string; staffId: number | null; remark?: string }[]
): Promise<{ code: number; message: string }> {
  if (USE_MOCK) {
    const existingDetails = mockSchedulingDetails[id] || []
    details.forEach(newDetail => {
      const existing = existingDetails.find(d => d.dutyDate === newDetail.dutyDate)
      if (existing) {
        existing.staffId = newDetail.staffId
        const staff = mockStaffList.find(s => s.id === newDetail.staffId)
        existing.staffName = staff?.name || null
        existing.remark = newDetail.remark || ''
      }
    })
    return Promise.resolve({ code: 0, message: '更新成功' })
  }
  return request.put(`/scheduling/${id}/details`, { details })
}

// 获取可排班人员
export async function fetchSchedulingStaff(): Promise<{ code: number; data: Staff[] }> {
  if (USE_MOCK) {
    return Promise.resolve({ code: 0, data: mockStaffList })
  }
  return request.get('/scheduling/staff')
}

// 获取科室排班配置
export async function fetchSchedulingConfig(): Promise<{ code: number; data: SchedulingConfig }> {
  if (USE_MOCK) {
    return Promise.resolve({ code: 0, data: mockSchedulingConfig })
  }
  return request.get('/scheduling/config')
}

// 更新科室排班配置
export async function updateSchedulingConfig(
  staffOrder: number[]
): Promise<{ code: number; message: string }> {
  if (USE_MOCK) {
    mockSchedulingConfig.staffOrder = staffOrder
    mockSchedulingConfig.updatedAt = new Date().toISOString().replace('T', ' ').substring(0, 19)
    return Promise.resolve({ code: 0, message: '更新成功' })
  }
  return request.put('/scheduling/config', { staffOrder })
}

// 快速生成排班
export async function autoGenerateScheduling(
  schedulingId: number,
  params: {
    startDate: string
    endDate: string
    startPosition: number
    staffOrder: number[]
  }
): Promise<{ code: number; data: SchedulingDetail[] }> {
  if (USE_MOCK) {
    const [startYear, startMonth, startDay] = params.startDate.split('-')
    const [endYear, endMonth, endDay] = params.endDate.split('-')
    
    const start = new Date(params.startDate)
    const end = new Date(params.endDate)
    const days = Math.floor((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24)) + 1
    
    const details: SchedulingDetail[] = []
    const order = params.staffOrder
    
    for (let i = 0; i < days; i++) {
      const date = new Date(start)
      date.setDate(date.getDate() + i)
      
      const dutyDate = `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
      const staffIndex = (params.startPosition + i) % order.length
      const staffId = order[staffIndex]
      const staff = mockStaffList.find(s => s.id === staffId)
      
      details.push({
        id: i + 1,
        dutyDate,
        staffId,
        staffName: staff?.name || null,
        remark: ''
      })
    }
    
    mockSchedulingConfig.lastPosition = (params.startPosition + days - 1) % order.length
    mockSchedulingDetails[schedulingId] = details
    
    return Promise.resolve({ code: 0, data: details })
  }
  return request.post(`/scheduling/${schedulingId}/auto-generate`, params)
}

export async function fetchDutyStaff(): Promise<{ code: number; data: DutyStaff[] }> {
  return request.get('/scheduling/duty-staff')
}

export async function addDutyStaff(staffIds: number[]): Promise<{ code: number; data: DutyStaff[] }> {
  return request.post('/scheduling/duty-staff', { staffIds })
}

export async function removeDutyStaff(staffId: number): Promise<{ code: number; message: string }> {
  return request.delete(`/scheduling/duty-staff/${staffId}`)
}

export async function updateDutyStaffOrder(staffIds: number[]): Promise<{ code: number; data: DutyStaff[] }> {
  return request.put('/scheduling/duty-staff/order', { staffIds })
}

export async function initializeDutyStaff(): Promise<{ code: number; data: DutyStaff[] }> {
  return request.post('/scheduling/duty-staff/initialize')
}