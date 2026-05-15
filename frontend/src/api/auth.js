import request from '@/utils/request'

export const sendCodeApi = (identifier) => request.post('/auth/send-code', { identifier })
export const registerApi = (data) => request.post('/auth/register', data)
export const loginApi = (data) => request.post('/auth/login', data)
