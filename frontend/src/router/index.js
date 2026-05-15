import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import MainLayout from '@/views/layouts/MainLayout.vue'
import AdminLayout from '@/views/layouts/AdminLayout.vue'

const routes = [
  {
    path: '/',
    component: MainLayout,
    children: [
      { path: '', component: () => import('@/views/Home.vue') },
      { path: 'products', component: () => import('@/views/Products.vue') },
      { path: 'products/:id', component: () => import('@/views/ProductDetail.vue') },
      { path: 'articles', component: () => import('@/views/Articles.vue') },
      { path: 'articles/:id', component: () => import('@/views/ArticleDetail.vue') },
      { path: 'cart', component: () => import('@/views/Cart.vue'), meta: { auth: true } },
      { path: 'checkout', component: () => import('@/views/Checkout.vue'), meta: { auth: true } },
      { path: 'payment/:id', component: () => import('@/views/Payment.vue'), meta: { auth: true } },
      { path: 'orders', component: () => import('@/views/Orders.vue'), meta: { auth: true } },
      { path: 'orders/:id', component: () => import('@/views/Orders.vue'), meta: { auth: true } },
      { path: 'reviews', component: () => import('@/views/MyReviews.vue'), meta: { auth: true } },
      { path: 'favorites', component: () => import('@/views/Favorites.vue'), meta: { auth: true } },
      { path: 'profile', component: () => import('@/views/Profile.vue'), meta: { auth: true } }
    ]
  },
  { path: '/login', component: () => import('@/views/Login.vue') },
  { path: '/register', component: () => import('@/views/Register.vue') },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { auth: true, admin: true },
    redirect: '/admin/products',
    children: [
      { path: 'products', component: () => import('@/views/admin/AdminProducts.vue') },
      { path: 'categories', component: () => import('@/views/admin/AdminCategories.vue') },
      { path: 'banners', component: () => import('@/views/admin/AdminBanners.vue') },
      { path: 'orders', component: () => import('@/views/admin/AdminOrders.vue') },
      { path: 'after-sales', component: () => import('@/views/admin/AdminAfterSales.vue') },
      { path: 'users', component: () => import('@/views/admin/AdminUsers.vue') },
      { path: 'articles', component: () => import('@/views/admin/AdminArticles.vue') },
      { path: 'statistics', component: () => import('@/views/admin/AdminStatistics.vue') },
      { path: 'config', component: () => import('@/views/admin/RecommendConfig.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from) => {
  const userStore = useUserStore()
  if (to.meta?.auth && !userStore.isLogin) return '/login'
  if (to.meta?.admin && userStore.role !== 'ADMIN') return '/login?tab=admin'
  return true
})

export default router
