import request from '@/utils/request'

export const getCategoriesApi = () => request.get('/categories/tree')
export const getProductsApi = (params) => request.get('/products', { params })
export const getProductDetailApi = (id) => request.get(`/products/${id}`)
export const getAnimeIpsApi = () => request.get('/products/anime-ips')
export const getArticlesApi = (params) => request.get('/articles', { params })
export const getArticleDetailApi = (id) => request.get(`/articles/${id}`)
