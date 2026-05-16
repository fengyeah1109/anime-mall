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
      return Promise.reject({ message: payload?.message || '请求失败' })
    }
    return payload.data
  },
  (err) => {
    const errorMessage = err?.response?.data?.message || err?.message || '网络错误'
    
    if (err?.response?.status === 401 || err?.response?.status === 403) {
      // 如果是登录页面的请求，不显示"未登录"提示，直接传递错误信息
      if (location.pathname === '/login') {
        return Promise.reject({ message: errorMessage })
      }
      ElMessage.warning('未登录，请先登录')
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      setTimeout(() => {
        location.href = '/login'
      }, 1500)
    } else {
      ElMessage.error(errorMessage)
    }
    return Promise.reject({ message: errorMessage })
  }
)

export default request
