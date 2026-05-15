import request from '@/utils/request'

/**
 * 上传文件
 * @param {FormData} formData - 包含文件的FormData
 * @returns {Promise}
 */
export const uploadFileApi = (formData) => {
  return request.post('/upload', formData)
}