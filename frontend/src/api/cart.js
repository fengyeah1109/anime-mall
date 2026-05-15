import request from '@/utils/request'

export const addCartApi = (data) => request.post('/cart/add', data)
export const getCartApi = () => request.get('/cart')
export const updateCartApi = (data) => request.put('/cart/update', data)
export const removeCartApi = (id) => request.delete('/cart/remove', { params: { id } })
export const clearCartApi = () => request.delete('/cart/clear')
export const selectedCartApi = (data) => request.post('/cart/selected', data)
