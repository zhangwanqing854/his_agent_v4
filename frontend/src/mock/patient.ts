import type { Patient, VitalRecord, PatientListItem, HandoverRecord, PatientHandover } from '@/types/patient'

function generateVitalsHistory(baseTemp: number = 36.5): VitalRecord[] {
  const records: VitalRecord[] = []
  const now = new Date()
  
  for (let i = 6; i >= 0; i--) {
    const date = new Date(now)
    date.setDate(date.getDate() - i)
    const dateStr = date.toISOString().slice(0, 10)
    
    for (const hour of [8, 12, 16, 20]) {
      records.push({
        time: `${dateStr} ${String(hour).padStart(2, '0')}:00`,
        temperature: Math.max(35, Math.min(42, baseTemp + Math.random() * 1.5 - 0.5)),
        pulse: Math.max(40, Math.min(120, 60 + Math.floor(Math.random() * 40))),
        respiration: Math.max(10, Math.min(30, 16 + Math.floor(Math.random() * 8))),
        systolicBp: Math.max(80, Math.min(200, 110 + Math.floor(Math.random() * 40))),
        diastolicBp: Math.max(50, Math.min(120, 70 + Math.floor(Math.random() * 20))),
        oxygenSaturation: Math.max(85, Math.min(100, 95 + Math.floor(Math.random() * 5)))
      })
    }
  }
  
  return records
}

function generateHandoverRecords(patientId: number): HandoverRecord[] {
  const doctors = ['张医生', '李医生', '王医生', '赵医生']
  const records: HandoverRecord[] = []
  
  for (let i = 0; i < 3; i++) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    const fromIdx = i % doctors.length
    const toIdx = (i + 1) % doctors.length
    
    records.push({
      id: patientId * 100 + i,
      time: `${date.toISOString().slice(0, 10)} 08:00`,
      fromDoctor: doctors[fromIdx],
      toDoctor: doctors[toIdx],
      summary: '患者情况稳定，继续观察'
    })
  }
  
  return records
}

const departmentPatients: Record<number, Patient[]> = {
  1: [
    {
      id: 1, bedNumber: '1床', name: '张三', age: 65, gender: '男',
      admissionDate: '2026-03-10', hospitalId: 'H202603001',
      department: '心内科', departmentId: 1,
      diagnosis: '2型糖尿病、高血压3级', attendingDoctor: '王主任',
      nurseLevel: '一级', isCritical: true,
      allergies: ['青霉素'], medicalHistory: ['高血压病史10年', '糖尿病病史5年'],
      mewsScore: 2, bradenScore: 16, fallRisk: 25,
      vitalsHistory: generateVitalsHistory(37.0),
      nursingNotes: '每日监测血糖4次，注意低血糖症状观察',
      handoverRecords: generateHandoverRecords(1)
    },
    {
      id: 2, bedNumber: '2床', name: '李四', age: 58, gender: '男',
      admissionDate: '2026-03-12', hospitalId: 'H202603002',
      department: '心内科', departmentId: 1,
      diagnosis: '冠心病、心功能不全', attendingDoctor: '李主任',
      nurseLevel: '一级', isCritical: true,
      allergies: [], medicalHistory: ['冠心病病史3年'],
      mewsScore: 1, bradenScore: 18, fallRisk: 20,
      vitalsHistory: generateVitalsHistory(36.8),
      nursingNotes: '注意心率变化，限制活动量',
      handoverRecords: generateHandoverRecords(2)
    },
    {
      id: 3, bedNumber: '3床', name: '王五', age: 72, gender: '男',
      admissionDate: '2026-03-15', hospitalId: 'H202603003',
      department: '心内科', departmentId: 1,
      diagnosis: '急性心肌梗死', attendingDoctor: '王主任',
      nurseLevel: '特级', isCritical: true,
      allergies: ['磺胺类药物'], medicalHistory: ['心肌梗死病史'],
      mewsScore: 3, bradenScore: 14, fallRisk: 40,
      vitalsHistory: generateVitalsHistory(38.2),
      nursingNotes: '绝对卧床，持续心电监护，严密观察生命体征',
      handoverRecords: generateHandoverRecords(3)
    },
    {
      id: 4, bedNumber: '4床', name: '赵六', age: 55, gender: '女',
      admissionDate: '2026-03-18', hospitalId: 'H202603004',
      department: '心内科', departmentId: 1,
      diagnosis: '高血压2级', attendingDoctor: '张医生',
      nurseLevel: '二级', isCritical: false,
      allergies: [], medicalHistory: ['高血压病史5年'],
      mewsScore: 1, bradenScore: 20, fallRisk: 15,
      vitalsHistory: generateVitalsHistory(36.5),
      nursingNotes: '规律服药，监测血压',
      handoverRecords: generateHandoverRecords(4)
    },
    {
      id: 5, bedNumber: '5床', name: '钱七', age: 48, gender: '男',
      admissionDate: '2026-03-20', hospitalId: 'H202603005',
      department: '心内科', departmentId: 1,
      diagnosis: '2型糖尿病', attendingDoctor: '李主任',
      nurseLevel: '三级', isCritical: false,
      allergies: [], medicalHistory: ['糖尿病病史2年'],
      mewsScore: 0, bradenScore: 19, fallRisk: 10,
      vitalsHistory: generateVitalsHistory(36.6),
      nursingNotes: '糖尿病饮食，监测血糖',
      handoverRecords: generateHandoverRecords(5)
    },
    {
      id: 6, bedNumber: '6床', name: '孙八', age: 62, gender: '男',
      admissionDate: '2026-03-22', hospitalId: 'H202603006',
      department: '心内科', departmentId: 1,
      diagnosis: '心律失常', attendingDoctor: '王主任',
      nurseLevel: '二级', isCritical: false,
      allergies: [], medicalHistory: ['心律失常病史1年'],
      mewsScore: 1, bradenScore: 18, fallRisk: 18,
      vitalsHistory: generateVitalsHistory(36.7),
      nursingNotes: '监测心率，避免剧烈运动',
      handoverRecords: generateHandoverRecords(6)
    },
    {
      id: 7, bedNumber: '7床', name: '周九', age: 70, gender: '女',
      admissionDate: '2026-03-25', hospitalId: 'H202603007',
      department: '心内科', departmentId: 1,
      diagnosis: '心力衰竭', attendingDoctor: '李主任',
      nurseLevel: '一级', isCritical: true,
      allergies: ['青霉素'], medicalHistory: ['心力衰竭病史2年'],
      mewsScore: 2, bradenScore: 15, fallRisk: 35,
      vitalsHistory: generateVitalsHistory(37.1),
      nursingNotes: '限制液体摄入，监测体重变化',
      handoverRecords: generateHandoverRecords(7)
    },
    {
      id: 8, bedNumber: '8床', name: '吴十', age: 45, gender: '男',
      admissionDate: '2026-03-28', hospitalId: 'H202603008',
      department: '心内科', departmentId: 1,
      diagnosis: '高血压1级', attendingDoctor: '张医生',
      nurseLevel: '三级', isCritical: false,
      allergies: [], medicalHistory: ['高血压病史1年'],
      mewsScore: 0, bradenScore: 21, fallRisk: 8,
      vitalsHistory: generateVitalsHistory(36.5),
      nursingNotes: '规律服药，低盐饮食',
      handoverRecords: generateHandoverRecords(8)
    }
  ],
  2: [
    {
      id: 11, bedNumber: '1床', name: '郑十一', age: 68, gender: '男',
      admissionDate: '2026-03-08', hospitalId: 'H202603011',
      department: '神经内科', departmentId: 2,
      diagnosis: '脑梗死', attendingDoctor: '陈主任',
      nurseLevel: '一级', isCritical: true,
      allergies: ['阿司匹林'], medicalHistory: ['高血压病史15年', '脑梗死病史'],
      mewsScore: 2, bradenScore: 14, fallRisk: 45,
      vitalsHistory: generateVitalsHistory(37.2),
      nursingNotes: '卧床护理，预防压疮，注意吞咽功能',
      handoverRecords: generateHandoverRecords(11)
    },
    {
      id: 12, bedNumber: '2床', name: '王十二', age: 55, gender: '女',
      admissionDate: '2026-03-10', hospitalId: 'H202603012',
      department: '神经内科', departmentId: 2,
      diagnosis: '脑出血', attendingDoctor: '陈主任',
      nurseLevel: '特级', isCritical: true,
      allergies: [], medicalHistory: ['高血压病史10年'],
      mewsScore: 3, bradenScore: 13, fallRisk: 50,
      vitalsHistory: generateVitalsHistory(38.0),
      nursingNotes: '绝对卧床，控制血压，观察意识变化',
      handoverRecords: generateHandoverRecords(12)
    },
    {
      id: 13, bedNumber: '3床', name: '陈十三', age: 42, gender: '男',
      admissionDate: '2026-03-12', hospitalId: 'H202603013',
      department: '神经内科', departmentId: 2,
      diagnosis: '癫痫持续状态', attendingDoctor: '林医生',
      nurseLevel: '特级', isCritical: true,
      allergies: [], medicalHistory: ['癫痫病史5年'],
      mewsScore: 4, bradenScore: 18, fallRisk: 35,
      vitalsHistory: generateVitalsHistory(37.5),
      nursingNotes: '床边防护，防止癫痫发作时受伤',
      handoverRecords: generateHandoverRecords(13)
    },
    {
      id: 14, bedNumber: '4床', name: '林十四', age: 38, gender: '女',
      admissionDate: '2026-03-15', hospitalId: 'H202603014',
      department: '神经内科', departmentId: 2,
      diagnosis: '重症肌无力', attendingDoctor: '陈主任',
      nurseLevel: '一级', isCritical: true,
      allergies: [], medicalHistory: ['重症肌无力病史3年'],
      mewsScore: 2, bradenScore: 17, fallRisk: 30,
      vitalsHistory: generateVitalsHistory(36.9),
      nursingNotes: '注意呼吸功能，避免感染',
      handoverRecords: generateHandoverRecords(14)
    },
    {
      id: 15, bedNumber: '5床', name: '黄十五', age: 50, gender: '男',
      admissionDate: '2026-03-18', hospitalId: 'H202603015',
      department: '神经内科', departmentId: 2,
      diagnosis: '偏头痛', attendingDoctor: '林医生',
      nurseLevel: '三级', isCritical: false,
      allergies: [], medicalHistory: ['偏头痛病史5年'],
      mewsScore: 0, bradenScore: 20, fallRisk: 10,
      vitalsHistory: generateVitalsHistory(36.6),
      nursingNotes: '避免诱因，规律作息',
      handoverRecords: generateHandoverRecords(15)
    }
  ],
  3: [
    {
      id: 21, bedNumber: '1床', name: '孙八', age: 75, gender: '男',
      admissionDate: '2026-03-05', hospitalId: 'H202603021',
      department: '呼吸科', departmentId: 3,
      diagnosis: '慢性阻塞性肺疾病急性加重', attendingDoctor: '周主任',
      nurseLevel: '特级', isCritical: true,
      allergies: [], medicalHistory: ['COPD病史20年', '吸烟史40年'],
      mewsScore: 3, bradenScore: 14, fallRisk: 45,
      vitalsHistory: generateVitalsHistory(37.8),
      nursingNotes: '氧疗护理，监测血氧，雾化治疗',
      handoverRecords: generateHandoverRecords(21)
    },
    {
      id: 22, bedNumber: '2床', name: '周九', age: 62, gender: '女',
      admissionDate: '2026-03-10', hospitalId: 'H202603022',
      department: '呼吸科', departmentId: 3,
      diagnosis: '肺炎、呼吸衰竭', attendingDoctor: '周主任',
      nurseLevel: '一级', isCritical: true,
      allergies: ['头孢类药物'], medicalHistory: ['肺炎病史'],
      mewsScore: 2, bradenScore: 16, fallRisk: 38,
      vitalsHistory: generateVitalsHistory(38.5),
      nursingNotes: '抗感染治疗，氧疗，监测呼吸功能',
      handoverRecords: generateHandoverRecords(22)
    },
    {
      id: 23, bedNumber: '3床', name: '吴十', age: 55, gender: '男',
      admissionDate: '2026-03-15', hospitalId: 'H202603023',
      department: '呼吸科', departmentId: 3,
      diagnosis: '支气管哮喘', attendingDoctor: '吴医生',
      nurseLevel: '二级', isCritical: false,
      allergies: [], medicalHistory: ['哮喘病史10年'],
      mewsScore: 0, bradenScore: 19, fallRisk: 20,
      vitalsHistory: generateVitalsHistory(36.7),
      nursingNotes: '雾化治疗，避免接触过敏原',
      handoverRecords: generateHandoverRecords(23)
    }
  ],
  4: [
    {
      id: 31, bedNumber: '1床', name: '郑十', age: 45, gender: '女',
      admissionDate: '2026-03-12', hospitalId: 'H202603031',
      department: '内分泌科', departmentId: 4,
      diagnosis: '甲状腺功能亢进', attendingDoctor: '黄主任',
      nurseLevel: '二级', isCritical: false,
      allergies: [], medicalHistory: ['甲亢病史2年'],
      mewsScore: 1, bradenScore: 20, fallRisk: 15,
      vitalsHistory: generateVitalsHistory(36.8),
      nursingNotes: '抗甲状腺药物治疗，监测心率',
      handoverRecords: generateHandoverRecords(31)
    },
    {
      id: 32, bedNumber: '2床', name: '王十一', age: 58, gender: '男',
      admissionDate: '2026-03-08', hospitalId: 'H202603032',
      department: '内分泌科', departmentId: 4,
      diagnosis: '糖尿病肾病', attendingDoctor: '黄主任',
      nurseLevel: '一级', isCritical: true,
      allergies: ['磺胺类药物'], medicalHistory: ['糖尿病病史15年', '肾病病史3年'],
      mewsScore: 2, bradenScore: 17, fallRisk: 28,
      vitalsHistory: generateVitalsHistory(36.9),
      nursingNotes: '监测血糖、肾功能，低盐低蛋白饮食',
      handoverRecords: generateHandoverRecords(32)
    }
  ]
}

export function getPatientList(deptId: number): PatientListItem[] {
  const patients = departmentPatients[deptId] || []
  return patients.map(p => ({
    id: p.id,
    bedNumber: p.bedNumber,
    name: p.name,
    age: p.age,
    gender: p.gender,
    admissionDate: p.admissionDate,
    diagnosis: p.diagnosis,
    attendingDoctor: p.attendingDoctor,
    nurseLevel: p.nurseLevel,
    isCritical: p.isCritical,
    allergies: p.allergies,
    mewsScore: p.mewsScore,
    bradenScore: p.bradenScore,
    fallRisk: p.fallRisk,
    latestVitals: p.vitalsHistory[p.vitalsHistory.length - 1] || {
      time: '', temperature: 36.5, pulse: 70, respiration: 16,
      systolicBp: 120, diastolicBp: 80, oxygenSaturation: 98
    }
  }))
}

export function getPatientDetail(id: number): Patient | null {
  for (const patients of Object.values(departmentPatients)) {
    const patient = patients.find(p => p.id === id)
    if (patient) return patient
  }
  return null
}

export function getAllPatients(): Patient[] {
  return Object.values(departmentPatients).flat()
}

const currentConditionMap: Record<number, string> = {
  1: '患者今日血糖波动较大，最高15.2mmol/L，需注意低血糖反应',
  2: '今日胸闷症状加重，活动后明显，心电图示ST段改变',
  3: '急性心肌梗死术后第2天，生命体征平稳，切口愈合良好',
  4: '血压控制稳定，无明显不适主诉',
  5: '血糖控制良好，无明显并发症',
  6: '偶有心悸，心率波动在正常范围',
  7: '心力衰竭症状有所改善，夜间阵发性呼吸困难减轻',
  8: '血压平稳，无头晕头痛等不适',
  11: '今日肢体无力加重，左侧肢体肌力下降',
  12: '脑出血术后第1天，意识清楚，瞳孔等大等圆',
  13: '今日癫痫发作2次，已用药控制，意识逐渐恢复',
  14: '呼吸肌无力，密切监测呼吸功能，备呼吸机',
  15: '头痛症状缓解，睡眠质量改善',
  21: '呼吸困难加重，已调整氧疗方案，血氧饱和度波动',
  22: '肺炎抗感染治疗中，体温下降，咳嗽咳痰减少',
  23: '哮喘症状控制良好，无明显喘息',
  31: '甲亢症状稳定，心率控制在正常范围',
  32: '肾功能监测中，尿量正常，水肿消退'
}

const observationItemsMap: Record<number, string> = {
  1: '注意监测血糖变化，观察低血糖症状',
  2: '监测心率、血压变化，注意胸闷症状',
  3: '绝对卧床，持续心电监护，观察生命体征',
  4: '监测血压，规律服药',
  5: '监测血糖，糖尿病饮食',
  6: '监测心率，避免剧烈运动',
  7: '监测体重、尿量，限制液体摄入',
  8: '监测血压，低盐饮食',
  11: '观察意识、瞳孔变化，注意肢体活动',
  12: '监测意识、瞳孔，控制血压，预防再出血',
  13: '防止癫痫发作时受伤，床边防护',
  14: '监测呼吸功能，备呼吸机，注意吞咽功能',
  15: '避免诱因，规律作息',
  21: '监测血氧饱和度，氧疗护理',
  22: '监测体温、呼吸功能，注意痰液引流',
  23: '避免接触过敏原，雾化治疗',
  31: '监测心率，观察甲亢症状',
  32: '监测血糖、肾功能，低盐低蛋白饮食'
}

export function getHandoverPatients(deptId: number): PatientHandover[] {
  const patients = departmentPatients[deptId] || []
  return patients.map(p => {
    const latestVitals = p.vitalsHistory[p.vitalsHistory.length - 1] || {
      time: '', temperature: 36.5, pulse: 70, respiration: 16,
      systolicBp: 120, diastolicBp: 80, oxygenSaturation: 98
    }
    const vitalsText = `T: ${(latestVitals.temperature).toFixed(1)}℃  P: ${latestVitals.pulse}次/分  R: ${latestVitals.respiration}次/分  BP: ${latestVitals.systolicBp}/${latestVitals.diastolicBp}mmHg  SpO₂: ${latestVitals.oxygenSaturation}%`
    return {
      id: p.id,
      bedNumber: p.bedNumber,
      name: p.name,
      age: p.age,
      gender: p.gender,
      diagnosis: p.diagnosis,
      isCritical: p.isCritical,
      vitals: vitalsText,
      currentCondition: currentConditionMap[p.id] || '病情稳定',
      observationItems: observationItemsMap[p.id] || '常规观察'
    }
  })
}