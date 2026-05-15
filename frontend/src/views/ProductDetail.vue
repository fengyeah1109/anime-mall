<template>
  <div class="product-detail" v-if="detail.product">
    <div class="detail-main">
      <div class="gallery-section">
        <div class="main-image">
          <img :src="getImageUrl(currentImage)" :alt="detail.product.name" />
        </div>
        <div class="thumbnail-list" v-if="detail.images && detail.images.length > 1">
          <div 
            v-for="(img, index) in detail.images" 
            :key="img.id"
            class="thumbnail-item"
            :class="{ active: currentImage === img.imageUrl }"
            @click="currentImage = img.imageUrl"
          >
            <img :src="getImageUrl(img.imageUrl)" />
          </div>
        </div>
      </div>

      <div class="info-section">
        <h1 class="product-title">{{ detail.product.name }}</h1>
        
        <div class="price-box">
          <div class="price-row">
            <span class="price-label">价格</span>
            <span class="price-value">¥{{ detail.product.price }}</span>
            <span class="original-price" v-if="detail.product.originalPrice && detail.product.originalPrice > detail.product.price">
              ¥{{ detail.product.originalPrice }}
            </span>
          </div>
        </div>

        <div class="info-row">
          <span class="info-label">销量</span>
          <span class="info-value">{{ detail.product.sales || 0 }} 件</span>
        </div>

        <div class="info-row">
          <span class="info-label">库存</span>
          <span class="info-value">{{ detail.product.stock }} 件</span>
        </div>

        <div class="info-row" v-if="detail.product.tags">
          <span class="info-label">标签</span>
          <div class="tags">
            <el-tag size="small" v-for="tag in detail.product.tags.split(',')" :key="tag" class="tag-item">
              {{ tag }}
            </el-tag>
          </div>
        </div>

        <div class="quantity-row">
          <span class="info-label">数量</span>
          <el-input-number v-model="qty" :min="1" :max="detail.product.stock" size="large" />
          <span class="stock-tip">件（库存{{ detail.product.stock }}件）</span>
        </div>

        <div class="action-row">
          <el-button type="primary" size="large" class="buy-btn" @click="addCart">
            <el-icon><ShoppingCart /></el-icon>
            加入购物车
          </el-button>
          <el-button size="large" class="favorite-btn" @click="favorite">
            <el-icon><Star /></el-icon>
            收藏
          </el-button>
        </div>
      </div>
    </div>

    <div class="detail-tabs">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="商品详情" name="detail">
          <div class="detail-content">
            <p v-if="detail.product.description">{{ detail.product.description }}</p>
            <p v-else class="no-content">暂无详情</p>
          </div>
        </el-tab-pane>
        <el-tab-pane label="用户评价" name="reviews">
          <div class="reviews-content">
            <div v-if="detail.latestReviews && detail.latestReviews.length > 0" class="reviews-list">
              <div v-for="review in detail.latestReviews" :key="review.id" class="review-item">
                <div class="review-header">
                  <div class="review-rating">
                    <el-rate v-model="review.rating" :max="5" disabled show-score text-color="#ff9900" />
                  </div>
                  <div class="review-time">{{ formatTime(review.createTime) }}</div>
                </div>
                <div class="review-content" v-if="review.content">
                  {{ review.content }}
                </div>
                <div class="review-images" v-if="review.images">
                  <img v-for="(img, index) in parseReviewImages(review.images)" 
                       :key="index" 
                       :src="img" 
                       class="review-image" />
                </div>
              </div>
            </div>
            <div v-else class="no-reviews">
              <div class="no-reviews-icon">⭐</div>
              <p class="no-reviews-text">暂无评价</p>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart, Star } from '@element-plus/icons-vue'
import { getProductDetailApi } from '@/api/product'
import { addCartApi } from '@/api/cart'
import { addFavoriteApi } from '@/api/user'
import { getImageUrl, parseReviewImages } from '@/utils/image'

const route = useRoute()
const detail = ref({ product: null, images: [] })
const qty = ref(1)
const currentImage = ref('')
const activeTab = ref('detail')

onMounted(async () => {
  detail.value = await getProductDetailApi(route.params.id)
  
  // 过滤掉随机生成的占位图
  if (detail.value.images) {
    detail.value.images = detail.value.images.filter(img => 
      img.imageUrl && !img.imageUrl.includes('picsum.photos')
    )
  }
  
  // 确保主图作为第一张图片
  if (detail.value.product.mainImage) {
    const mainImage = {
      id: 0,
      productId: detail.value.product.id,
      imageUrl: detail.value.product.mainImage,
      sortOrder: 0,
      type: 0
    }
    
    // 如果没有详情图片，只使用主图
    if (!detail.value.images || detail.value.images.length === 0) {
      detail.value.images = [mainImage]
    } else {
      // 确保主图在第一位
      const hasMainImage = detail.value.images.some(img => img.imageUrl === detail.value.product.mainImage)
      if (!hasMainImage) {
        detail.value.images.unshift(mainImage)
      }
    }
  }
  
  // 确保当前图片有值
  if (detail.value.images && detail.value.images.length > 0) {
    currentImage.value = detail.value.images[0].imageUrl
  }
})

const addCart = async () => {
  try {
    await addCartApi({ productId: detail.value.product.id, quantity: qty.value })
    ElMessage.success('已加入购物车')
  } catch (error) {
    // 401/403 错误已在拦截器中处理，这里不重复提示
  }
}

const favorite = async () => {
  try {
    await addFavoriteApi(detail.value.product.id)
    ElMessage.success('收藏成功')
  } catch (error) {
    // 401/403 错误已在拦截器中处理，这里不重复提示
  }
}

const formatTime = (time) => {
  if (!time) return '-' 
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.product-detail {
  background: #fff;
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: var(--shadow-card);
}

.detail-main {
  display: flex;
  gap: 40px;
  padding: 30px;
}

.gallery-section {
  width: 480px;
  flex-shrink: 0;
}

.main-image {
  width: 100%;
  height: 480px;
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: linear-gradient(135deg, #f5f7fa 0%, #e9ecef 100%);
  box-shadow: var(--shadow-card);
  transition: all var(--transition-base);
}

.main-image:hover {
  box-shadow: var(--shadow-lg);
}

.main-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.main-image:hover img {
  transform: scale(1.03);
}

.thumbnail-list {
  display: flex;
  gap: 12px;
  margin-top: 16px;
  justify-content: center;
}

.thumbnail-item {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-md);
  overflow: hidden;
  cursor: pointer;
  border: 3px solid transparent;
  transition: all var(--transition-base);
  box-shadow: var(--shadow-sm);
}

.thumbnail-item:hover {
  border-color: var(--primary-light);
  transform: translateY(-2px);
}

.thumbnail-item.active {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(255, 107, 107, 0.2);
}

.thumbnail-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.info-section {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.product-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-color);
  margin: 0 0 24px;
  line-height: 1.4;
}

.price-box {
  background: linear-gradient(135deg, #fff5f0 0%, #fff0e8 100%);
  padding: 20px 24px;
  border-radius: var(--radius-lg);
  margin-bottom: 24px;
  border: 1px solid rgba(255, 107, 107, 0.1);
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 16px;
}

.price-label {
  font-size: 14px;
  color: var(--text-secondary);
}

.price-value {
  font-size: 32px;
  color: var(--price-color);
  font-weight: 700;
}

.original-price {
  font-size: 16px;
  color: var(--text-light);
  text-decoration: line-through;
}

.info-row {
  display: flex;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid var(--border-color);
}

.info-label {
  width: 70px;
  color: var(--text-light);
  font-size: 14px;
}

.info-value {
  color: var(--text-color);
  font-weight: 500;
}

.tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tag-item {
  margin: 0;
}

.quantity-row {
  display: flex;
  align-items: center;
  padding: 24px 0;
  gap: 16px;
}

.quantity-row :deep(.el-input-number) {
  border-radius: var(--radius-md);
}

.quantity-row :deep(.el-input-number__decrease),
.quantity-row :deep(.el-input-number__increase) {
  border-radius: 0 var(--radius-md) var(--radius-md) 0;
}

.stock-tip {
  color: var(--text-light);
  font-size: 13px;
}

.action-row {
  display: flex;
  gap: 16px;
  margin-top: auto;
  padding-top: 24px;
}

.buy-btn {
  min-width: 200px;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--radius-xl) !important;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.favorite-btn {
  border-color: var(--primary-color);
  color: var(--primary-color);
  border-radius: var(--radius-xl) !important;
  height: 52px;
  width: 140px;
  transition: all var(--transition-base);
}

.favorite-btn:hover {
  background: var(--primary-light);
  border-color: var(--primary-hover);
  color: var(--primary-hover);
}

.detail-tabs {
  border-top: 1px solid var(--border-color);
  padding: 30px;
}

.detail-tabs :deep(.el-tabs__header) {
  margin-bottom: 24px;
}

.detail-tabs :deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 500;
  padding: 0 24px;
}

.detail-tabs :deep(.el-tabs__item.is-active) {
  color: var(--primary-color);
}

.detail-tabs :deep(.el-tabs__active-bar) {
  background: var(--gradient-primary);
  height: 3px;
  border-radius: 3px;
}

.detail-content {
  padding: 16px 0;
  color: var(--text-secondary);
  line-height: 1.8;
  font-size: 15px;
}

.no-content {
  color: var(--text-light);
  text-align: center;
  padding: 60px 0;
}

.reviews-content {
  padding: 20px 0;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.review-item {
  padding: 24px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  background: #fafbfc;
  transition: all var(--transition-base);
}

.review-item:hover {
  box-shadow: var(--shadow-sm);
  border-color: var(--primary-light);
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.review-user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.review-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--gradient-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 600;
  font-size: 16px;
}

.review-username {
  font-weight: 500;
  color: var(--text-color);
}

.review-rating {
  display: flex;
  align-items: center;
  gap: 4px;
}

.review-rating :deep(.el-rate__icon) {
  font-size: 18px;
}

.review-time {
  font-size: 13px;
  color: var(--text-light);
}

.review-content {
  font-size: 15px;
  color: var(--text-color);
  line-height: 1.7;
  margin-bottom: 16px;
}

.review-images {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.review-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  transition: all var(--transition-base);
  cursor: pointer;
}

.review-image:hover {
  transform: scale(1.05);
  box-shadow: var(--shadow-md);
}

.no-reviews {
  text-align: center;
  padding: 80px 0;
  color: var(--text-light);
}

.no-reviews-icon {
  font-size: 64px;
  margin-bottom: 20px;
  opacity: 0.6;
}

.no-reviews-text {
  font-size: 18px;
  margin: 0;
}

@media (max-width: 992px) {
  .detail-main {
    flex-direction: column;
    gap: 24px;
  }
  
  .gallery-section {
    width: 100%;
  }
  
  .main-image {
    height: 400px;
  }
  
  .product-title {
    font-size: 20px;
  }
  
  .price-value {
    font-size: 28px;
  }
}

@media (max-width: 768px) {
  .detail-main {
    padding: 20px;
  }
  
  .main-image {
    height: 300px;
  }
  
  .thumbnail-item {
    width: 60px;
    height: 60px;
  }
  
  .action-row {
    flex-direction: column;
  }
  
  .buy-btn,
  .favorite-btn {
    width: 100%;
  }
}
</style>
