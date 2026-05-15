import request from '@/utils/request'

export const createOrderApi = (data) => request.post('/orders', data)
export const payOrderApi = (id) => request.post(`/orders/${id}/pay`)
export const getOrdersApi = (params) => request.get('/orders', { params })
export const getOrderDetailApi = (id) => request.get(`/orders/${id}`)
export const cancelOrderApi = (id) => request.put(`/orders/${id}/cancel`)
export const receiveOrderApi = (id) => request.put(`/orders/${id}/receive`)
