import request from './request'
import { mockUsers } from '@/mock/user'

export interface Department {
  id: number
  code: string
  name: string
  isPrimary: boolean
}

export interface LoginParams {
  usercode: string
  password: string
  captcha?: string
  captchaId?: string
}

export interface LoginResponse {
  token: string
  userInfo: {
    id: number
    username: string
    name: string
    role: string
    avatar: string
    departments: Department[]
  }
}

const USE_MOCK = true

export function getCaptchaApi() {
  return request.get('/auth/captcha')
}

export async function loginApi(params: LoginParams): Promise<ApiResponse<LoginResponse>> {
  if (USE_MOCK) {
    const user = mockUsers.find(u =>
      u.usercode.toLowerCase() === params.usercode.toLowerCase() &&
      u.password === params.password
    )
    
    if (!user) {
      return Promise.resolve({
        code: 1,
        message: '用户编码或密码错误',
        data: null as unknown as LoginResponse
      })
    }
    
    return Promise.resolve({
      code: 0,
      message: '登录成功',
      data: {
        token: 'mock-token-' + user.id,
        userInfo: {
          id: user.id,
          username: user.username,
          name: user.username,
          role: user.role?.name || '',
          avatar: '',
          departments: []
        }
      }
    })
  }
  return request.post<LoginResponse>('/auth/login', params)
}

export function logoutApi() {
  return request.post('/auth/logout')
}

export function getCurrentUserApi() {
  return request.get('/auth/me')
}