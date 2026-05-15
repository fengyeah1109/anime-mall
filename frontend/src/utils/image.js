/**
 * 处理图片URL，确保路径一致性
 * @param {string} url - 原始图片URL
 * @returns {string} - 处理后的完整URL
 */
export function getImageUrl(url) {
  if (!url) return ''
  
  // 如果已经是完整URL（以http或https开头），直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  
  // 如果已经以/uploads/开头，直接返回
  if (url.startsWith('/uploads/')) {
    return url
  }
  
  // 如果以/开头，说明是相对路径，直接返回
  if (url.startsWith('/')) {
    return url
  }
  
  // 否则，添加/uploads/前缀
  return `/uploads/${url}`
}

/**
 * 处理评价图片，将逗号分隔的字符串转换为数组
 * @param {string} images - 逗号分隔的图片字符串
 * @returns {string[]} - 图片URL数组
 */
export function parseReviewImages(images) {
  if (!images) return []
  
  // 如果已经是数组，直接处理每个元素
  if (Array.isArray(images)) {
    return images.map(img => getImageUrl(img))
  }
  
  // 如果是字符串，按逗号分割并处理每个元素
  if (typeof images === 'string') {
    return images.split(',').map(img => getImageUrl(img.trim())).filter(Boolean)
  }
  
  return []
}
