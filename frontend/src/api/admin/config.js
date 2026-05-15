import request from '@/utils/request'

// 获取推荐权重配置
export const getRecommendConfigApi = () => {
  return request.get('/admin/config/recommend')
}

// 更新推荐权重配置
export const updateRecommendConfigApi = (config) => {
  return request.put('/admin/config/recommend', config)
}