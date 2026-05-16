<template>
  <div class="home-page">
    <!-- 轮播图区域 -->
    <div class="banner-section">
      <el-carousel height="560px" :interval="5000" v-if="banners.length > 0">
        <el-carousel-item v-for="banner in banners" :key="banner.id">
          <div class="banner-item" @click="handleBannerClick(banner)">
            <img :src="banner.imageUrl" :alt="banner.title" class="banner-img" />
            <div class="banner-overlay"></div>
            <div class="banner-content" v-if="banner.title">
              <h2>{{ banner.title }}</h2>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
      <el-carousel height="560px" :interval="5000" v-else>
        <el-carousel-item v-for="i in 3" :key="i">
          <div class="banner-item" :style="{ background: getBannerColor(i) }">
            <div class="banner-content">
              <h2>动漫周边特惠</h2>
              <p>精选正版周边，品质保证</p>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 热销推荐 -->
    <section class="section">
      <div class="section-header">
        <div class="section-title-wrap">
          <span class="section-icon">🔥</span>
          <h3 class="section-title">热销推荐</h3>
        </div>
        <router-link to="/products?sortBy=sales" class="section-more">
          查看更多
          <svg class="arrow-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M5 12h14M12 5l7 7-7 7"/>
          </svg>
        </router-link>
      </div>
      <div class="product-grid">
        <div v-for="p in products" :key="p.id" class="product-card" @click="$router.push(`/products/${p.id}`)">
          <div class="product-img-wrap">
            <img :src="p.mainImage" :alt="p.name" class="product-img" />
            <div class="product-tags">
              <span class="product-tag hot" v-if="p.isHot">热销</span>
              <span class="product-tag new" v-if="p.isNew">新品</span>
            </div>
            <div class="product-overlay">
              <span class="view-btn">查看详情</span>
            </div>
          </div>
          <div class="product-info">
            <h4 class="product-name">{{ p.name }}</h4>
            <div class="product-price-row">
              <span class="price-current">¥{{ p.price }}</span>
              <span class="price-original" v-if="p.originalPrice && p.originalPrice > p.price">¥{{ p.originalPrice }}</span>
            </div>
            <div class="product-sales">
              <span>已售 {{ p.sales || 0 }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 为你推荐 -->
    <section class="section" v-if="recommendations.length > 0">
      <div class="section-header">
        <div class="section-title-wrap">
          <span class="section-icon">✨</span>
          <h3 class="section-title">为你推荐</h3>
        </div>
      </div>
      <div class="product-grid">
        <div v-for="p in recommendations.slice(0, 8)" :key="p.id" class="product-card" @click="$router.push(`/products/${p.id}`)">
          <div class="product-img-wrap">
            <img :src="p.mainImage" :alt="p.name" class="product-img" />
            <div class="product-tags">
              <span class="product-tag hot" v-if="p.isHot">热销</span>
              <span class="product-tag new" v-if="p.isNew">新品</span>
            </div>
            <div class="product-overlay">
              <span class="view-btn">查看详情</span>
            </div>
          </div>
          <div class="product-info">
            <h4 class="product-name">{{ p.name }}</h4>
            <div class="product-price-row">
              <span class="price-current">¥{{ p.price }}</span>
              <span class="price-original" v-if="p.originalPrice && p.originalPrice > p.price">¥{{ p.originalPrice }}</span>
            </div>
            <div class="product-sales">
              <span>已售 {{ p.sales || 0 }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 最新资讯 -->
    <section class="section news-section">
      <div class="section-header">
        <div class="section-title-wrap">
          <span class="section-icon">📰</span>
          <h3 class="section-title">最新资讯</h3>
        </div>
        <router-link to="/articles" class="section-more">
          查看更多
          <svg class="arrow-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M5 12h14M12 5l7 7-7 7"/>
          </svg>
        </router-link>
      </div>
      <div class="news-grid">
        <div v-for="article in articles" :key="article.id"
          class="news-card"
          @click="$router.push(`/articles/${article.id}`)"
        >
          <div class="news-cover" v-if="article.coverImage">
            <img :src="article.coverImage" :alt="article.title" />
          </div>
          <div class="news-body">
            <span class="news-category">{{ article.category || '综合' }}</span>
            <h4 class="news-title">{{ article.title }}</h4>
            <p class="news-desc">{{ article.summary || '点击查看详情...' }}</p>
            <div class="news-footer">
              <span class="news-time">{{ formatTime(article.createTime) }}</span>
              <span class="news-link">查看详情 →</span>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getArticlesApi, getProductsApi } from '@/api/product'
import { getRecommendationsApi } from '@/api/recommend'
import request from '@/utils/request'

const router = useRouter()
const products = ref([])
const articles = ref([])
const banners = ref([])
const recommendations = ref([])

const getBannerColor = (i) => {
  const colors = [
    'linear-gradient(135deg, #ff6b6b 0%, #ff8e53 100%)',
    'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
  ]
  return colors[(i - 1) % colors.length]
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const handleBannerClick = (banner) => {
  if (banner.linkUrl) {
    if (banner.linkUrl.startsWith('http://') || banner.linkUrl.startsWith('https://')) {
      window.open(banner.linkUrl, '_blank')
    } else {
      router.push(banner.linkUrl)
    }
  }
}

const getBannersApi = async () => {
  try {
    const response = await request.get('/banners')
    return response
  } catch (error) {
    console.error('获取轮播图失败:', error)
    return []
  }
}

onMounted(async () => {
  products.value = (await getProductsApi({ sortBy: 'sales', order: 'desc', page: 1, size: 8 })).records || []
  articles.value = (await getArticlesApi({ page: 1, size: 4 })).records || []
  banners.value = await getBannersApi()
  try {
    recommendations.value = await getRecommendationsApi() || []
  } catch (e) {
    console.error('获取推荐失败', e)
    recommendations.value = []
  }
})
</script>

<style scoped>
/* ========== 全新视觉风格 ========== */
.home-page {
  padding-bottom: 80px;
  background: #f8fafc;
}

/* 轮播图区域 */
.banner-section {
  margin-bottom: 50px;
  width: 100%;
  position: relative;
  padding: 0;
  background: linear-gradient(180deg, #f0f2f5 0%, #f8fafc 100%);
}

.banner-section :deep(.el-carousel) {
  overflow: hidden;
  box-shadow: 0 15px 35px -12px rgba(0,0,0,0.2);
}

.banner-section :deep(.el-carousel__container) {
  border-radius: 0;
}

.banner-section :deep(.el-carousel__indicator) {
  padding: 8px 4px;
}

.banner-section :deep(.el-carousel__indicator .el-carousel__button) {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgba(255,255,255,0.6);
  transition: all 0.3s ease;
}

.banner-section :deep(.el-carousel__indicator.is-active .el-carousel__button) {
  background: #ff6b6b;
  width: 28px;
  border-radius: 5px;
}

.banner-item {
  height: 100%;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  cursor: pointer;
  overflow: hidden;
}

.banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.banner-item:hover .banner-img {
  transform: scale(1.05);
}

.banner-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, rgba(0,0,0,0.2) 0%, rgba(0,0,0,0.4) 100%);
}

.banner-content {
  text-align: center;
  color: #fff;
  position: absolute;
  bottom: 80px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 10;
  width: 90%;
  max-width: 900px;
  backdrop-filter: blur(0px);
}

.banner-content h2 {
  font-size: 48px;
  margin: 0 0 16px;
  font-weight: 800;
  letter-spacing: 4px;
  text-shadow: 0 4px 25px rgba(0,0,0,0.3);
  background: linear-gradient(135deg, #ffffff 0%, #ffe0d4 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.banner-content p {
  font-size: 18px;
  margin: 0;
  opacity: 0.95;
  text-shadow: 0 2px 10px rgba(0,0,0,0.3);
  font-weight: 500;
  letter-spacing: 1px;
}

/* 公用区块 */
.section {
  margin-bottom: 60px;
  padding: 0 5%;
  max-width: 1400px;
  margin-left: auto;
  margin-right: auto;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 32px;
  padding-bottom: 8px;
  border-bottom: 2px solid rgba(255,107,107,0.15);
}

.section-title-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
}

.section-icon {
  font-size: 28px;
  filter: drop-shadow(0 4px 6px rgba(0,0,0,0.1));
}

.section-title {
  font-size: 26px;
  font-weight: 800;
  margin: 0;
  background: linear-gradient(135deg, #2d2d2d 0%, #ff6b6b 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 1px;
}

.section-more {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #ff6b6b;
  transition: all 0.3s ease;
  text-decoration: none;
  background: rgba(255,107,107,0.08);
  padding: 8px 18px;
  border-radius: 40px;
}

.section-more:hover {
  background: #ff6b6b;
  color: white;
  gap: 10px;
  transform: translateY(-2px);
  box-shadow: 0 6px 14px rgba(255,107,107,0.3);
}

.arrow-svg {
  width: 16px;
  height: 16px;
  transition: transform 0.3s ease;
}

.section-more:hover .arrow-svg {
  transform: translateX(4px);
}

/* 商品卡片 */
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 28px;
}

.product-card {
  background: #ffffff;
  border-radius: 20px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.2, 0.9, 0.4, 1.1);
  box-shadow: 0 8px 20px rgba(0,0,0,0.04);
  border: 1px solid rgba(0,0,0,0.04);
}

.product-card:hover {
  transform: translateY(-12px);
  box-shadow: 0 25px 40px -12px rgba(255,107,107,0.35), 0 10px 20px -8px rgba(0,0,0,0.1);
  border-color: rgba(255,107,107,0.2);
}

.product-img-wrap {
  position: relative;
  padding-top: 100%;
  overflow: hidden;
  background: linear-gradient(145deg, #f0f2f5 0%, #e2e6ea 100%);
}

.product-img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.product-card:hover .product-img {
  transform: scale(1.08);
}

.product-tags {
  position: absolute;
  top: 14px;
  left: 14px;
  display: flex;
  gap: 8px;
  z-index: 2;
}

.product-tag {
  padding: 4px 12px;
  font-size: 11px;
  border-radius: 30px;
  color: #fff;
  font-weight: 700;
  backdrop-filter: blur(4px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.product-tag.hot {
  background: linear-gradient(135deg, #ff6b6b, #ee5a5a);
}

.product-tag.new {
  background: linear-gradient(135deg, #20c997, #1aa179);
}

.product-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.25);
  backdrop-filter: blur(3px);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.product-card:hover .product-overlay {
  opacity: 1;
}

.view-btn {
  background: rgba(255,255,255,0.95);
  color: #ff6b6b;
  padding: 10px 28px;
  border-radius: 40px;
  font-size: 14px;
  font-weight: 700;
  transform: translateY(15px);
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.product-card:hover .view-btn {
  transform: translateY(0);
}

.product-info {
  padding: 20px 18px 22px;
}

.product-name {
  font-size: 15px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 12px;
  line-height: 1.45;
  height: 44px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.2s;
}

.product-card:hover .product-name {
  color: #ff6b6b;
}

.product-price-row {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 10px;
}

.price-current {
  font-size: 24px;
  color: #ff6b6b;
  font-weight: 800;
  letter-spacing: -0.5px;
}

.price-original {
  font-size: 13px;
  color: #94a3b8;
  text-decoration: line-through;
}

.product-sales {
  font-size: 12px;
  color: #94a3b8;
  font-weight: 500;
  background: #f1f5f9;
  display: inline-block;
  padding: 4px 10px;
  border-radius: 20px;
}

/* 资讯卡片 */
.news-section {
  margin-bottom: 60px;
}

.news-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 28px;
}

.news-card {
  display: flex;
  background: #fff;
  border-radius: 24px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.35s ease;
  box-shadow: 0 8px 20px rgba(0,0,0,0.04);
  border: 1px solid #eef2f6;
}

.news-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 20px 30px -12px rgba(0,0,0,0.12);
  border-color: #ffe0d4;
}

.news-cover {
  width: 180px;
  flex-shrink: 0;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #f1f3f5 0%, #e9ecef 100%);
}

.news-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.news-card:hover .news-cover img {
  transform: scale(1.05);
}

.news-body {
  flex: 1;
  padding: 22px 24px;
  display: flex;
  flex-direction: column;
}

.news-category {
  display: inline-block;
  background: linear-gradient(135deg, #20c997, #1aa179);
  color: #fff;
  padding: 4px 14px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 700;
  margin-bottom: 12px;
  align-self: flex-start;
  letter-spacing: 0.5px;
}

.news-title {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 10px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.2s;
}

.news-card:hover .news-title {
  color: #ff6b6b;
}

.news-desc {
  font-size: 13px;
  line-height: 1.6;
  color: #64748b;
  margin: 0 0 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.news-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.news-time {
  font-size: 12px;
  color: #94a3b8;
  font-weight: 500;
}

.news-link {
  font-size: 13px;
  font-weight: 700;
  color: #20c997;
  opacity: 0;
  transform: translateX(-8px);
  transition: all 0.25s ease;
}

.news-card:hover .news-link {
  opacity: 1;
  transform: translateX(0);
}

/* 响应式优化 */
@media (max-width: 1300px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 24px;
  }
}

@media (max-width: 1000px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .news-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .section-title {
    font-size: 22px;
  }
  
  .banner-content h2 {
    font-size: 36px;
  }
}

@media (max-width: 768px) {
  .banner-section :deep(.el-carousel) {
    height: 360px !important;
  }
  
  .banner-content h2 {
    font-size: 26px;
  }
  
  .banner-content p {
    font-size: 14px;
  }
  
  .section {
    padding: 0 4%;
  }
  
  .product-grid {
    gap: 16px;
  }
  
  .product-info {
    padding: 14px;
  }
  
  .product-name {
    font-size: 13px;
    height: 38px;
  }
  
  .price-current {
    font-size: 20px;
  }
  
  .news-card {
    flex-direction: column;
  }
  
  .news-cover {
    width: 100%;
    height: 150px;
  }
  
  .news-body {
    padding: 18px;
  }
  
  .section-more {
    padding: 6px 14px;
  }
}
</style>