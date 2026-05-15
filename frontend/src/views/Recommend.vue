<template>
  <div class="recommend-page">
    <div class="page-header">
      <h2>为你推荐</h2>
      <p>基于你的浏览和购买历史，我们为你精选了以下商品</p>
    </div>
    
    <div class="recommend-content">
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="10" animated />
      </div>
      
      <div v-else-if="recommendations.length === 0" class="empty-container">
        <el-empty description="暂无推荐商品" />
      </div>
      
      <div v-else class="product-list">
        <div v-for="product in recommendations" :key="product.id" class="product-card">
          <div class="product-image">
            <img :src="getImageUrl(product.mainImage)" :alt="product.name" />
          </div>
          <div class="product-info">
            <h3 class="product-name">{{ product.name }}</h3>
            <div class="product-price">
              <span class="price">¥{{ product.price }}</span>
              <span v-if="product.originalPrice" class="original-price">¥{{ product.originalPrice }}</span>
            </div>
            <div class="product-meta">
              <span class="sales">销量 {{ product.sales }}</span>
              <span class="stock" :class="{ 'low-stock': product.stock < 10 }">
                库存 {{ product.stock }}
              </span>
            </div>
            <div class="product-actions">
              <el-button type="primary" size="small" @click="addToCart(product.id)">
                加入购物车
              </el-button>
              <el-button type="default" size="small" @click="viewDetail(product.id)">
                查看详情
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getRecommendationsApi } from '@/api/recommend'
import { addCartApi } from '@/api/cart'
import { getImageUrl } from '@/utils/image'

const router = useRouter()
const loading = ref(true)
const recommendations = ref([])

const loadRecommendations = async () => {
  try {
    loading.value = true
    const response = await getRecommendationsApi()
    if (response.code === 0) {
      recommendations.value = response.data
    } else {
      ElMessage.error(response.msg || '获取推荐失败')
    }
  } catch (error) {
    ElMessage.error('获取推荐失败')
  } finally {
    loading.value = false
  }
}

const addToCart = async (productId) => {
  try {
    const response = await addCartApi({ productId, quantity: 1 })
    if (response.code === 0) {
      ElMessage.success('已加入购物车')
    } else {
      ElMessage.error(response.msg || '加入购物车失败')
    }
  } catch (error) {
    ElMessage.error('加入购物车失败')
  }
}

const viewDetail = (productId) => {
  router.push(`/products/${productId}`)
}

onMounted(() => {
  loadRecommendations()
})
</script>

<style scoped>
.recommend-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 10px;
  color: #333;
}

.page-header p {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.recommend-content {
  min-height: 400px;
}

.loading-container {
  padding: 20px;
}

.empty-container {
  padding: 60px 0;
  text-align: center;
}

.product-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.product-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.15);
}

.product-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.product-card:hover .product-image img {
  transform: scale(1.05);
}

.product-info {
  padding: 15px;
}

.product-name {
  font-size: 16px;
  font-weight: 500;
  margin: 0 0 10px 0;
  color: #333;
  line-height: 1.4;
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-price {
  margin-bottom: 10px;
}

.price {
  font-size: 18px;
  font-weight: 600;
  color: #ff4400;
  margin-right: 10px;
}

.original-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
}

.product-meta {
  font-size: 12px;
  color: #666;
  margin-bottom: 15px;
  display: flex;
  justify-content: space-between;
}

.low-stock {
  color: #f56c6c;
}

.product-actions {
  display: flex;
  gap: 10px;
}

.product-actions .el-button {
  flex: 1;
}

@media (max-width: 768px) {
  .recommend-page {
    padding: 10px;
  }
  
  .product-list {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 15px;
  }
  
  .product-image {
    height: 150px;
  }
  
  .product-name {
    font-size: 14px;
  }
  
  .price {
    font-size: 16px;
  }
}
</style>