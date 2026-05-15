<template>
  <div class="favorites-page">
    <div class="page-header">
      <h2 class="page-title">我的收藏</h2>
    </div>

    <div v-if="loading" class="loading-wrap">
      <el-skeleton :rows="5" animated />
    </div>

    <div v-else-if="!favorites.length" class="empty-wrap">
      <div class="empty-icon">❤️</div>
      <p class="empty-text">暂无收藏商品</p>
      <el-button type="primary" size="large" @click="$router.push('/products')">
        去逛逛
      </el-button>
    </div>

    <div v-else class="favorites-grid">
      <div v-for="item in favorites" :key="item.id" class="favorite-card">
        <div class="card-image" @click="goToProduct(item.productId)">
          <img :src="item.productImage" :alt="item.productName" />
        </div>
        <div class="card-content">
          <div class="product-name" @click="goToProduct(item.productId)">{{ item.productName }}</div>
          <div class="product-price">¥{{ item.price }}</div>
          <div class="card-actions">
            <el-button type="primary" size="small" @click="addToCart(item)">加入购物车</el-button>
            <el-button size="small" @click="removeFavorite(item.id)">取消收藏</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getFavoritesApi, deleteFavoriteApi } from '@/api/user'
import { addCartApi } from '@/api/cart'

const router = useRouter()
const favorites = ref([])
const loading = ref(false)

const loadFavorites = async () => {
  loading.value = true
  try {
    favorites.value = await getFavoritesApi()
  } catch (error) {
    ElMessage.error('加载收藏列表失败')
  } finally {
    loading.value = false
  }
}

const goToProduct = (productId) => {
  router.push(`/products/${productId}`)
}

const removeFavorite = async (id) => {
  try {
    await ElMessageBox.confirm('确定要取消收藏该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteFavoriteApi(id)
    await loadFavorites()
    ElMessage.success('已取消收藏')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const addToCart = async (item) => {
  try {
    await addCartApi({
      productId: item.productId,
      quantity: 1
    })
    ElMessage.success('已加入购物车')
  } catch (error) {
    ElMessage.error('加入购物车失败')
  }
}

onMounted(loadFavorites)
</script>

<style scoped>
.favorites-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.loading-wrap {
  background: #fff;
  padding: 40px;
  border-radius: 8px;
}

.empty-wrap {
  background: #fff;
  padding: 80px 20px;
  text-align: center;
  border-radius: 8px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.empty-text {
  color: #999;
  font-size: 16px;
  margin-bottom: 30px;
}

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.favorite-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
}

.favorite-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-image {
  width: 100%;
  height: 220px;
  overflow: hidden;
  cursor: pointer;
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.card-image:hover img {
  transform: scale(1.05);
}

.card-content {
  padding: 15px;
}

.product-name {
  font-size: 14px;
  color: #333;
  line-height: 1.4;
  height: 40px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  cursor: pointer;
  margin-bottom: 10px;
}

.product-name:hover {
  color: #ff4400;
}

.product-price {
  font-size: 18px;
  color: #ff4400;
  font-weight: 600;
  margin-bottom: 15px;
}

.card-actions {
  display: flex;
  gap: 10px;
}

.card-actions .el-button {
  flex: 1;
}
</style>
