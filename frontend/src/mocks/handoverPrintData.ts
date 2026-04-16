/**
 * 交班报表打印 - Mock 数据
 *
 * 用于 ReportPreview 组件的测试数据
 */

import type { HandoverDto, HandoverPatientDto } from '@/api/handover'
import type { ApiResponse } from '@/api/request'

export interface PrintableReport {
  // 基本信息
  id: number
  departmentId: number
  departmentName: string
  handoverDate: string
  shift: '白班' | '夜班' | '大夜班'
  fromDoctorId: number
  fromDoctorName: string
  toDoctorId: number
  toDoctorName: string
  status: 'DRAFT' | 'PENDING' | 'TRANSFERRING' | 'COMPLETED'

  // 科室统计
  stats: {
    totalPatients: number
    admission: number
    transferOut: number
    discharge: number
    transferIn: number
    death: number
    surgery: number
    criticalCount: number
  }

  // 患者详情列表
  patients: PrintablePatient[]

  // SBAR交班报告内容
  reportContent: {
    situation: string
    background: string
    assessment: string
    recommendation: string
  }

  // 待办事项
  todos: PrintableTodo[]

  // 打印元数据
  printMetadata: {
    generatedAt: string
    version: number
    printedAt?: string
    isSigned: boolean
  }
}

// 打印患者详情
export interface PrintablePatient {
  id: number
  bedNumber: string
  name: string
  age?: number
  gender?: '男' | '女'
  diagnosis: string
  condition: string
  isCritical: boolean

  // 生命体征 (最新)
  vitals?: {
    temperature?: number
    heartRate?: number
    bloodPressure?: string
    respiratoryRate?: number
    spo2?: number
  }

  // 风险评估
  riskScores: {
    mews?: number
    mewsLevel: 'low' | 'moderate' | 'high'
    braden?: number
    bradenLevel: 'low' | 'moderate' | 'high'
    fallRisk?: number
    fallRiskLevel: 'low' | 'moderate' | 'high'
  }
}

// 待办事项
export interface PrintableTodo {
  id: number
  patientId?: number
  patientName?: string
  content: string
  dueTime?: string
  priority: 'high' | 'normal' | 'low'
  status: 'pending' | 'completed'
}

// 完整的 Mock 报表数据
export const mockPrintableReport: PrintableReport = {
  id: 1,
  departmentId: 1,
  departmentName: '心内科',
  handoverDate: '2026-03-27',
  shift: '白班',
  fromDoctorId: 1,
  fromDoctorName: '张医生',
  toDoctorId: 2,
  toDoctorName: '李医生',
  status: 'PENDING',

  stats: {
    totalPatients: 8,
    admission: 1,
    transferOut: 1,
    discharge: 1,
    transferIn: 0,
    death: 0,
    surgery: 0,
    criticalCount: 2
  },

  patients: [
    {
      id: 1,
      bedNumber: '1床',
      name: '张三',
      age: 65,
      gender: '男',
      diagnosis: '2型糖尿病、高血压3级',
      condition: '主诉头痛、恶心、呕吐、视物模糊1周。入院查体，神志清楚，双侧瞳孔等大等圆，对光反射迟钝。今日血糖波动较大，最高15.2mmol/L。',
      isCritical: true,
      vitals: {
        temperature: 36.5,
        heartRate: 78,
        bloodPressure: '135/85',
        respiratoryRate: 18,
        spo2: 98
      },
      riskScores: {
        mews: 2,
        mewsLevel: 'moderate',
        braden: 16,
        bradenLevel: 'moderate',
        fallRisk: 25,
        fallRiskLevel: 'high'
      }
    },
    {
      id: 2,
      bedNumber: '2床',
      name: '李四',
      age: 58,
      gender: '女',
      diagnosis: '冠心病、心功能不全',
      condition: '今日胸闷症状加重，活动后气促。心电图示ST段压低。',
      isCritical: true,
      vitals: {
        temperature: 36.8,
        heartRate: 88,
        bloodPressure: '145/90',
        respiratoryRate: 20,
        spo2: 95
      },
      riskScores: {
        mews: 1,
        mewsLevel: 'low',
        braden: 18,
        bradenLevel: 'moderate',
        fallRisk: 15,
        fallRiskLevel: 'low'
      }
    },
    {
      id: 3,
      bedNumber: '3床',
      name: '王五',
      age: 72,
      gender: '男',
      diagnosis: '急性心肌梗死',
      condition: '术后第2天，生命体征平稳，切口愈合良好。',
      isCritical: false,
      vitals: {
        temperature: 37.0,
        heartRate: 72,
        bloodPressure: '125/75',
        respiratoryRate: 16,
        spo2: 97
      },
      riskScores: {
        mews: 0,
        mewsLevel: 'low',
        braden: 20,
        bradenLevel: 'low',
        fallRisk: 20,
        fallRiskLevel: 'moderate'
      }
    },
    {
      id: 4,
      bedNumber: '4床',
      name: '赵六',
      age: 55,
      gender: '男',
      diagnosis: '高血压2级',
      condition: '血压控制稳定，无特殊不适。',
      isCritical: false,
      vitals: {
        temperature: 36.6,
        heartRate: 70,
        bloodPressure: '130/80',
        respiratoryRate: 16,
        spo2: 99
      },
      riskScores: {
        mews: 0,
        mewsLevel: 'low',
        braden: 22,
        bradenLevel: 'low',
        fallRisk: 10,
        fallRiskLevel: 'low'
      }
    },
    {
      id: 5,
      bedNumber: '5床',
      name: '钱七',
      age: 60,
      gender: '女',
      diagnosis: '2型糖尿病',
      condition: '血糖控制良好，饮食控制中。',
      isCritical: false,
      vitals: {
        temperature: 36.4,
        heartRate: 75,
        bloodPressure: '128/78',
        respiratoryRate: 18,
        spo2: 98
      },
      riskScores: {
        mews: 0,
        mewsLevel: 'low',
        braden: 20,
        bradenLevel: 'low',
        fallRisk: 15,
        fallRiskLevel: 'low'
      }
    },
    {
      id: 6,
      bedNumber: '12床',
      name: '孙八',
      age: 68,
      gender: '男',
      diagnosis: '心律失常',
      condition: '房颤，心率控制可。',
      isCritical: false,
      vitals: {
        temperature: 36.7,
        heartRate: 76,
        bloodPressure: '132/82',
        respiratoryRate: 18,
        spo2: 97
      },
      riskScores: {
        mews: 0,
        mewsLevel: 'low',
        braden: 19,
        bradenLevel: 'low',
        fallRisk: 20,
        fallRiskLevel: 'moderate'
      }
    },
    {
      id: 7,
      bedNumber: '15床',
      name: '周九',
      age: 52,
      gender: '女',
      diagnosis: '心力衰竭',
      condition: '心功能III级，利尿治疗中。',
      isCritical: false,
      vitals: {
        temperature: 36.5,
        heartRate: 82,
        bloodPressure: '138/88',
        respiratoryRate: 19,
        spo2: 96
      },
      riskScores: {
        mews: 1,
        mewsLevel: 'low',
        braden: 18,
        bradenLevel: 'moderate',
        fallRisk: 25,
        fallRiskLevel: 'high'
      }
    },
    {
      id: 8,
      bedNumber: '20床',
      name: '吴十',
      age: 45,
      gender: '男',
      diagnosis: '高血压1级',
      condition: '血压控制良好。',
      isCritical: false,
      vitals: {
        temperature: 36.6,
        heartRate: 68,
        bloodPressure: '125/75',
        respiratoryRate: 16,
        spo2: 99
      },
      riskScores: {
        mews: 0,
        mewsLevel: 'low',
        braden: 22,
        bradenLevel: 'low',
        fallRisk: 5,
        fallRiskLevel: 'low'
      }
    }
  ],

  reportContent: {
    situation: '心内科今日共8名患者，重点患者2名。张三（1床）血糖波动较大，最高15.2mmol/L；李四（2床）胸闷症状加重。其余患者病情平稳。',
    background: '张三：入院日期2026-03-15，2型糖尿病病史5年，今日血糖控制不佳。李四：入院日期2026-03-10，冠心病史，今日胸闷加重。王五：术后第2天，恢复良好。',
    assessment: '张三：血糖控制不佳，MEWS评分2分（中风险），Braden评分16分（中风险），跌倒风险高。需加强监测。李四：病情相对稳定，MEWS评分1分，需继续观察胸闷症状。王五：术后恢复良好，生命体征平稳。',
    recommendation: '张三：监测Q4H血糖，胰岛素调整，明日内分泌科会诊，加强跌倒预防措施。李四：继续监测血压、心率，完善心脏彩超检查。王五：继续常规术后护理，预防感染。'
  },

  todos: [
    {
      id: 1,
      patientId: 1,
      patientName: '张三',
      content: '明日空腹血糖复查',
      dueTime: '2026-03-28 06:00',
      priority: 'high',
      status: 'pending'
    },
    {
      id: 2,
      patientId: 1,
      patientName: '张三',
      content: '内分泌科会诊',
      dueTime: '2026-03-28 14:00',
      priority: 'high',
      status: 'pending'
    },
    {
      id: 3,
      patientId: 2,
      patientName: '李四',
      content: '心脏彩超检查',
      dueTime: '2026-03-28 10:00',
      priority: 'normal',
      status: 'pending'
    },
    {
      id: 4,
      patientId: 3,
      patientName: '王五',
      content: '换药',
      dueTime: '2026-03-27 16:00',
      priority: 'normal',
      status: 'pending'
    }
  ],

  printMetadata: {
    generatedAt: '2026-03-27T08:30:00Z',
    version: 1,
    isSigned: false
  }
}

interface PreviewFormData {
  shift?: string
  toDoctorId?: number
  patients?: Array<{
    id: number
    bedNumber?: string
    name?: string
    diagnosis?: string
    condition?: string
  }>
  stats?: {
    admission?: number
    transferOut?: number
    discharge?: number
    transferIn?: number
    death?: number
    surgery?: number
  }
}

export function generatePreviewData(formData: PreviewFormData): PrintableReport {
  return {
    ...mockPrintableReport,
    shift: formData.shift || '白班',
    toDoctorId: formData.toDoctorId || 0,
    toDoctorName: formData.toDoctorId ? getDoctorName(formData.toDoctorId) : '待选择',
    patients: formData.patients?.map((p) => ({
      id: p.id,
      bedNumber: p.bedNumber || '',
      name: p.name || '',
      diagnosis: p.diagnosis || '',
      condition: p.condition || '',
      isCritical: p.condition && p.condition.length > 20
    })) || [],
    stats: {
      ...mockPrintableReport.stats,
      totalPatients: formData.patients?.length || 0,
      admission: formData.stats?.admission || 0,
      transferOut: formData.stats?.transferOut || 0,
      discharge: formData.stats?.discharge || 0,
      transferIn: formData.stats?.transferIn || 0,
      death: formData.stats?.death || 0,
      surgery: formData.stats?.surgery || 0,
      criticalCount: formData.patients?.filter((p) => p.condition?.length > 20).length || 0
    }
  }
}

// 辅助函数：根据ID获取医生姓名
function getDoctorName(doctorId: number): string {
  const doctors: Record<number, string> = {
    2: '李医生',
    3: '王医生',
    4: '赵医生',
    5: '钱医生',
    6: '孙医生',
    7: '周医生'
  }
  return doctors[doctorId] || '未知医生'
}

export async function loadFullReportData(handoverId: number): Promise<PrintableReport> {
  const request = await import('@/api/request').then(m => m.default)
  const token = localStorage.getItem('token')
  
  const headers: Record<string, string> = {}
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }
  
  const handoverRes = await request.get<ApiResponse<HandoverDto>>(`/api/handovers/${handoverId}`, { headers })
  const patientsRes = await request.get<ApiResponse<HandoverPatientDto[]>>(`/api/handovers/${handoverId}/patients`, { headers })
  
  const handover = handoverRes.data
  const patients = patientsRes.data || []
  
  return {
    id: handover.id,
    departmentId: handover.deptId,
    departmentName: handover.deptName,
    handoverDate: handover.handoverDate,
    shift: handover.shift,
    fromDoctorId: handover.fromDoctorId,
    fromDoctorName: handover.fromDoctorName,
    toDoctorId: handover.toDoctorId || 0,
    toDoctorName: handover.toDoctorName || '待选择',
    status: handover.status,
    stats: {
      totalPatients: patients.length,
      admission: 0,
      transferOut: 0,
      discharge: 0,
      transferIn: 0,
      death: 0,
      surgery: 0,
      criticalCount: patients.filter((p) => p.currentCondition && p.currentCondition.length > 20).length
    },
    patients: patients.map((p) => ({
      id: p.id,
      bedNumber: p.bedNo || '',
      name: p.patientName || '',
      gender: p.gender || '',
      age: p.age,
      diagnosis: p.diagnosis || '',
      condition: p.currentCondition || '',
      isCritical: p.currentCondition && p.currentCondition.length > 20,
      vitals: p.vitals ? {
        temperature: parseFloat(p.vitals.split(',')[0]?.split(':')[1] || '0'),
        heartRate: parseInt(p.vitals.split(',')[1]?.split(':')[1] || '0'),
        bloodPressure: p.vitals.split(',')[2]?.split(':')[1] || '',
        respiratoryRate: parseInt(p.vitals.split(',')[3]?.split(':')[1] || '0'),
        spo2: parseInt(p.vitals.split(',')[4]?.split(':')[1] || '0')
      } : undefined,
      riskScores: {
        mews: p.mewsScore,
        mewsLevel: p.mewsScore && p.mewsScore > 5 ? 'high' : p.mewsScore && p.mewsScore > 2 ? 'moderate' : 'low',
        braden: p.bradenScore,
        bradenLevel: p.bradenScore && p.bradenScore < 12 ? 'high' : p.bradenScore && p.bradenScore < 18 ? 'moderate' : 'low',
        fallRisk: p.fallRisk,
        fallRiskLevel: p.fallRisk && p.fallRisk > 25 ? 'high' : p.fallRisk && p.fallRisk > 15 ? 'moderate' : 'low'
      }
    })),
    reportContent: {
      situation: '',
      background: '',
      assessment: '',
      recommendation: ''
    },
    todos: [],
    printMetadata: {
      generatedAt: new Date().toISOString(),
      version: 1,
      isSigned: false
    }
  }
}
