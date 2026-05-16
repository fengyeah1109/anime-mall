<template>
  <div class="main-layout">
    <header class="header">
      <div class="header-inner">
        <div class="logo" @click="$router.push('/')">
          <span class="logo-text">动漫商城</span>
        </div>
        <nav class="nav-menu">
          <router-link to="/" class="nav-item" :class="{ active: $route.path === '/' }">首页</router-link>
          <router-link to="/products" class="nav-item" :class="{ active: $route.path.startsWith('/products') }">商品</router-link>
          <router-link to="/articles" class="nav-item" :class="{ active: $route.path.startsWith('/articles') }">资讯</router-link>
        </nav>
        <div class="header-right">
          <router-link to="/cart" class="header-icon">
            <el-badge :value="userStore.isLogin ? cartCount : 0" :hidden="!userStore.isLogin || !cartCount" :max="99">
              <el-icon size="20"><ShoppingCart /></el-icon>
            </el-badge>
          </router-link>
          <router-link to="/orders" class="header-link">我的订单</router-link>
          <router-link to="/reviews" class="header-link">我的评价</router-link>
          <router-link to="/favorites" class="header-link">收藏</router-link>
          <router-link to="/profile" class="header-link">个人中心</router-link>
          <router-link to="/admin" class="header-link admin-link">管理后台</router-link>
        </div>
      </div>
    </header>
    <main class="main-content">
      <router-view />
    </main>
    <footer class="footer">
      <div class="footer-inner">
        <p>&copy; 2024 动漫商城 - 动漫周边正品购物平台</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { ShoppingCart } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { storeToRefs } from 'pinia'

const userStore = useUserStore()
const cartStore = useCartStore()
const { totalCount: cartCount } = storeToRefs(cartStore)

onMounted(() => {
  if (userStore.isLogin) {
    cartStore.loadCart()
  }
})
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 60px;
  display: flex;
  align-items: center;
}

.logo {
  cursor: pointer;
  margin-right: 40px;
}

.logo-text {
  font-size: 22px;
  font-weight: 600;
  color: var(--primary-color);
}

.nav-menu {
  display: flex;
  gap: 8px;
}

.nav-item {
  padding: 8px 16px;
  color: var(--text-color);
  font-size: 15px;
  border-radius: 4px;
  transition: all 0.2s;
}

.nav-item:hover {
  color: var(--primary-color);
  background: #fff5f0;
}

.nav-item.active {
  color: var(--primary-color);
  font-weight: 500;
}

.header-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-icon {
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  cursor: pointer;
}

.header-icon:hover {
  color: var(--primary-color);
}

.header-link {
  color: var(--text-secondary);
  font-size: 14px;
}

.header-link:hover {
  color: var(--primary-color);
}

.admin-link {
  color: var(--primary-color);
}

.main-content {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 20px;
}

.footer {
  background: #fff;
  border-top: 1px solid var(--border-color);
  padding: 20px 0;
  margin-top: auto;
}

.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  text-align: center;
  color: var(--text-light);
  font-size: 13px;
}
</style>
