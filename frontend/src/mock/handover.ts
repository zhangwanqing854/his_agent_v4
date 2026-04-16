import { MockMethod } from 'vite-plugin-mock'

const handovers = [
  {
    id: 1,
    patientId: 1,
    patientName: '患者 1',
    bedNumber: '1 床',
    fromDoctorId: 1,
    fromDoctorName: '张医生',
    toDoctorId: 2,
    toDoctorName: '李医生',
    status: 'COMPLETED',
    handoverDate: '2026-03-20',
    reportContent: '<p>患者一般情况良好，生命体征平稳。</p>',
    createdAt: '2026-03-20 08:00:00'
  },
  {
    id: 2,
    patientId: 2,
    patientName: '患者 2',
    bedNumber: '2 床',
    fromDoctorId: 1,
    fromDoctorName: '张医生',
    toDoctorId: 2,
    toDoctorName: '李医生',
    status: 'PENDING',
    handoverDate: '2026-03-20',
    reportContent: '',
    createdAt: '2026-03-20 08:00:00'
  }
]

export default [
  {
    url: '/api/handovers',
    method: 'get',
    response: ({ query }) => {
      const page = parseInt(query.page) || 1
      const pageSize = parseInt(query.pageSize) || 10
      const start = (page - 1) * pageSize
      const end = start + pageSize
      
      return {
        code: 0,
        message: 'success',
        data: {
          list: handovers.slice(start, end),
          total: handovers.length
        }
      }
    }
  },
  {
    url: '/api/handovers/:id',
    method: 'get',
    response: ({ params }) => {
      const handover = handovers.find(h => h.id === parseInt(params.id))
      return {
        code: 0,
        message: 'success',
        data: handover || null
      }
    }
  }
] as MockMethod[]
