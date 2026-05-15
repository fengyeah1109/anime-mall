import request from '@/utils/request'

export const getRecommendationsApi = () => {
  return request.get('/recommend')
}