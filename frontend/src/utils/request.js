import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  // 登录、注册、发送验证码、轮播图接口不需要 token
  if (config.url.includes('/auth/') || config.url === '/banners') {
    // 确保删除可能存在的 Authorization 头
    delete config.headers.Authorization
    return config
  }
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (res) => {
    const payload = res.data
    if (payload?.code !== 200) {
      ElMessage.error(payload?.message || '请求失败')
      return Promise.reject(payload)
    }
    return payload.data
  },
  (err) => {
    if (err?.response?.status === 401 || err?.response?.status === 403) {
      ElMessage.warning('未登录，请先登录')
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      if (location.pathname !== '/login') {
        setTimeout(() => {
          location.href = '/login'
        }, 1500)
      }
    } else {
      ElMessage.error(err?.message || '网络错误')
    }
    return Promise.reject(err)
  }
)

export default request
