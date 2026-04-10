import request from './request'
import { users as mockUsers } from '@/mock/user'

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

const USE_MOCK = false

export function getCaptchaApi() {
  return request.get('/auth/captcha')
}

export async function loginApi(params: LoginParams): Promise<{ code: number; message: string; data: LoginResponse | null }> {
  if (USE_MOCK) {
    const user = mockUsers.find(u =>
      u.usercode.toLowerCase() === params.usercode.toLowerCase() &&
      u.password === params.password
    )
    
    if (!user) {
      return {
        code: 1,
        message: '用户编码或密码错误',
        data: null
      }
    }
    
    return {
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
    }
  }
  return request.post('/api/auth/login', params)
}

export function logoutApi() {
  return request.post('/auth/logout')
}

export function getCurrentUserApi() {
  return request.get('/auth/me')
}