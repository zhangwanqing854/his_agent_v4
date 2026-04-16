import Mock from 'mockjs'

Mock.setup({
  timeout: '200-600'
})

Mock.mock(/\/api\/patients(\?.*)?$/, 'get', {
  code: 0,
  message: 'success',
  'data': {
    'list|10': [{
      'id|+1': 1,
      'bedNumber': '@string("number", 1)床',
      'name': '患者@id',
      'age|40-80': 1,
      'gender|1': ['男', '女'],
      'diagnosis|1': ['2 型糖尿病', '高血压 3 级', '冠心病', 'COPD'],
      'department': '心内科',
      'mewsScore|0-5': 1,
      'bradenScore|15-23': 1
    }],
    'total': 30
  }
})

export default Mock