import request from '@/utils/request'

export const getProfileApi = () => request.get('/user/profile')
export const updateProfileApi = (data) => request.put('/user/profile', data)
export const getAddressesApi = () => request.get('/addresses')
export const addAddressApi = (data) => request.post('/addresses', data)
export const updateAddressApi = (id, data) => request.put(`/addresses/${id}`, data)
export const deleteAddressApi = (id) => request.delete(`/addresses/${id}`)
export const setDefaultAddressApi = (id) => request.put(`/addresses/${id}/default`)
export const getFavoritesApi = () => request.get('/favorites')
export const addFavoriteApi = (productId) => request.post('/favorites', null, { params: { productId } })
export const deleteFavoriteApi = (id) => request.delete(`/favorites/${id}`)
export const getAfterSalesApi = () => request.get('/after-sale/list')
export const createAfterSaleApi = (data) => request.post('/after-sale', data)

export const uploadAvatarApi = (file) => {
  const fd = new FormData()
  fd.append('file', file)
  return request.post('/user/avatar', fd, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
