import request from '@/utils/request'

/**
 * 获取轮播图列表
 * @param {number} page - 页码
 * @param {number} size - 每页数量
 * @returns {Promise}
 */
export const getBannersApi = (page, size) => {
  return request.get('/admin/banners', {
    params: { page, size }
  })
}

/**
 * 创建轮播图
 * @param {Object} data - 轮播图数据
 * @returns {Promise}
 */
export const createBannerApi = (data) => {
  return request.post('/admin/banners', data)
}

/**
 * 更新轮播图
 * @param {number} id - 轮播图ID
 * @param {Object} data - 轮播图数据
 * @returns {Promise}
 */
export const updateBannerApi = (id, data) => {
  return request.put(`/admin/banners/${id}`, data)
}

/**
 * 删除轮播图
 * @param {number} id - 轮播图ID
 * @returns {Promise}
 */
export const deleteBannerApi = (id) => {
  return request.delete(`/admin/banners/${id}`)
}

/**
 * 获取轮播图详情
 * @param {number} id - 轮播图ID
 * @returns {Promise}
 */
export const getBannerApi = (id) => {
  return request.get(`/admin/banners/${id}`)
}