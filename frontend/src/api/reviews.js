import request from '@/utils/request'

export const createReviewApi = (data) => {
  const formData = new FormData()
  formData.append('orderItemId', data.orderItemId)
  formData.append('rating', data.rating)
  if (data.content) {
    formData.append('content', data.content)
  }
  if (data.images && data.images.length) {
    data.images.forEach(image => {
      formData.append('images', image)
    })
  }
  return request.post('/reviews', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const getReviewsApi = () => {
  return request.get('/reviews')
}

export const updateReviewApi = (data) => {
  const formData = new FormData()
  formData.append('rating', data.rating)
  if (data.content) {
    formData.append('content', data.content)
  }
  if (data.images && data.images.length) {
    data.images.forEach(image => {
      formData.append('images', image)
    })
  }
  return request.put(`/reviews/${data.id}`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const deleteReviewApi = (id) => {
  return request.delete(`/reviews/${id}`)
}
