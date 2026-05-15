<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <h1 class="logo">管理后台</h1>
      </div>
      <nav class="sidebar-nav">
        <div class="nav-item home-link" @click="goToFrontend">
          <span class="nav-icon">🏠</span>
          <span class="nav-text">返回前台</span>
        </div>
        <div class="nav-divider"></div>
        <router-link to="/admin/products" class="nav-item" :class="{ active: currentPath === '/admin/products' }">
          <span class="nav-icon">📦</span>
          <span class="nav-text">商品管理</span>
        </router-link>
        <router-link to="/admin/categories" class="nav-item" :class="{ active: currentPath === '/admin/categories' }">
          <span class="nav-icon">📂</span>
          <span class="nav-text">分类/IP管理</span>
        </router-link>
        <router-link to="/admin/banners" class="nav-item" :class="{ active: currentPath === '/admin/banners' }">
          <span class="nav-icon">🖼️</span>
          <span class="nav-text">轮播图管理</span>
        </router-link>
        <router-link to="/admin/orders" class="nav-item" :class="{ active: currentPath === '/admin/orders' }">
          <span class="nav-icon">📋</span>
          <span class="nav-text">订单管理</span>
        </router-link>
        <router-link to="/admin/after-sales" class="nav-item" :class="{ active: currentPath === '/admin/after-sales' }">
          <span class="nav-icon">🔧</span>
          <span class="nav-text">售后管理</span>
        </router-link>
        <router-link to="/admin/users" class="nav-item" :class="{ active: currentPath === '/admin/users' }">
          <span class="nav-icon">👥</span>
          <span class="nav-text">用户管理</span>
        </router-link>
        <router-link to="/admin/articles" class="nav-item" :class="{ active: currentPath === '/admin/articles' }">
          <span class="nav-icon">📝</span>
          <span class="nav-text">文章管理</span>
        </router-link>
        <router-link to="/admin/config" class="nav-item" :class="{ active: currentPath === '/admin/config' }">
          <span class="nav-icon">⚙️</span>
          <span class="nav-text">推荐配置</span>
        </router-link>
        <router-link to="/admin/statistics" class="nav-item" :class="{ active: currentPath.startsWith('/admin/statistics') }">
          <span class="nav-icon">📊</span>
          <span class="nav-text">数据统计</span>
        </router-link>
        <div class="nav-divider"></div>
        <div class="nav-item logout-link" @click="logout">
          <span class="nav-icon">🚪</span>
          <span class="nav-text">退出登录</span>
        </div>
      </nav>
    </aside>
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const currentPath = computed(() => route.path)

const goToFrontend = () => {
  userStore.logout()
  router.push('/')
  ElMessage.success('已返回前台')
}

const logout = () => {
  userStore.logout()
  router.push('/login?tab=admin')
  ElMessage.success('已退出登录')
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: linear-gradient(135deg, #f8b195 0%, #f67280 35%, #c06c84 70%, #6c5b7b 100%);
  position: relative;
  overflow: hidden;
}

.admin-layout::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle at 30% 70%, rgba(255,255,255,0.3) 0%, transparent 50%),
              radial-gradient(circle at 70% 30%, rgba(255,200,150,0.2) 0%, transparent 40%);
  animation: float 20s ease-in-out infinite;
  pointer-events: none;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  50% { transform: translate(-2%, 2%) rotate(2deg); }
}

.sidebar {
  width: 280px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.08);
  border-right: 1px solid rgba(255, 255, 255, 0.6);
  position: relative;
  z-index: 10;
}

.sidebar-header {
  padding: 32px 24px;
  border-bottom: 1px solid rgba(255, 107, 107, 0.15);
}

.logo {
  color: #1a1a1a;
  font-size: 24px;
  font-weight: 700;
  margin: 0;
  text-align: center;
  background: linear-gradient(135deg, #ff6b6b 0%, #ffa07a 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 1px;
}

.sidebar-nav {
  flex: 1;
  padding: 24px 16px;
  overflow-y: auto;
}

.sidebar-nav::-webkit-scrollbar {
  width: 4px;
}

.sidebar-nav::-webkit-scrollbar-track {
  background: rgba(255, 107, 107, 0.08);
  border-radius: 2px;
}

.sidebar-nav::-webkit-scrollbar-thumb {
  background: rgba(255, 107, 107, 0.4);
  border-radius: 2px;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  color: #2d3436;
  text-decoration: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  border-radius: 16px;
  margin-bottom: 8px;
  font-weight: 500;
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(10px);
  border: 1px solid transparent;
}

.nav-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(255,107,107,0.12) 0%, rgba(255,160,122,0.08) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
  border-radius: 16px;
}

.nav-item:hover::before {
  opacity: 1;
}

.nav-item:hover {
  color: #ff6b6b;
  transform: translateX(8px);
  border-color: rgba(255, 107, 107, 0.2);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.nav-item.active {
  color: #fff;
  background: linear-gradient(135deg, #ff6b6b 0%, #ffa07a 100%);
  box-shadow: 0 6px 25px rgba(255, 107, 107, 0.4);
  border-color: rgba(255, 255, 255, 0.4);
  transform: translateX(8px);
  font-weight: 600;
}

.nav-item.active::before {
  opacity: 0;
}

.nav-icon {
  font-size: 20px;
  margin-right: 14px;
  transition: transform 0.3s ease;
}

.nav-item:hover .nav-icon {
  transform: scale(1.2) rotate(5deg);
}

.nav-text {
  font-size: 15px;
  letter-spacing: 0.3px;
}

.nav-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255, 107, 107, 0.3), transparent);
  margin: 20px 24px;
}

.home-link {
  background: rgba(255, 107, 107, 0.08);
  border: 1px solid rgba(255, 107, 107, 0.15);
  margin-bottom: 16px;
}

.logout-link {
  margin-top: 16px;
  background: rgba(255, 107, 107, 0.1);
  border: 1px solid rgba(255, 107, 107, 0.3);
}

.logout-link:hover {
  background: rgba(255, 107, 107, 0.35);
}

.main-content {
  flex: 1;
  padding: 32px;
  overflow-y: auto;
  animation: fadeIn 0.5s ease-out;
  position: relative;
  z-index: 1;
}

.main-content::-webkit-scrollbar {
  width: 8px;
}

.main-content::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 4px;
}

.main-content::-webkit-scrollbar-thumb {
  background: rgba(255, 107, 107, 0.3);
  border-radius: 4px;
}

.main-content::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 107, 107, 0.5);
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
