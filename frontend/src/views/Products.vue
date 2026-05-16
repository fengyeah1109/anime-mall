<template>
  <div class="products-page">
    <div class="products-wrapper">
      <div class="sidebar">
        <div class="category-card sidebar-card">
          <div class="category-header">
            <div class="category-icon">📂</div>
            <h4 class="card-title">商品分类</h4>
          </div>
          <div class="category-list">
            <div 
              class="category-item" 
              :class="{ active: !query.categoryId }"
              @click="onCategory('')"
            >
              全部分类
            </div>
            <div 
              v-for="c in categories" 
              :key="c.id" 
              class="category-item"
              :class="{ active: query.categoryId === String(c.id) }"
              @click="onCategory(String(c.id))"
            >
              {{ c.name }}
            </div>
          </div>
        </div>
        
        <div class="category-card sidebar-card">
          <div class="category-header">
            <div class="category-icon">🎭</div>
            <h4 class="card-title">动漫IP</h4>
          </div>
          <div class="category-list">
            <div 
              class="category-item" 
              :class="{ active: !query.animeIpId }"
              @click="onAnimeIp('')"
            >
              全部IP
            </div>
            <div 
              v-for="ip in animeIps" 
              :key="ip.id" 
              class="category-item"
              :class="{ active: query.animeIpId === String(ip.id) }"
              @click="onAnimeIp(String(ip.id))"
            >
              {{ ip.name }}
            </div>
          </div>
        </div>
      </div>

      <div class="main-content">
        <div class="filter-bar">
          <div class="filter-left">
            <el-input 
              v-model="query.keyword" 
              placeholder="搜索商品" 
              clearable
              class="search-input"
              @keyup.enter="load"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <div class="price-filter">
              <el-input-number 
                v-model="query.minPrice" 
                :min="0" 
                placeholder="最低价"
                controls-position="right"
                class="price-input"
              />
              <span class="price-sep">-</span>
              <el-input-number 
                v-model="query.maxPrice" 
                :min="0" 
                placeholder="最高价"
                controls-position="right"
                class="price-input"
              />
            </div>
          </div>
          <div class="filter-right">
            <el-select v-model="query.sortBy" class="sort-select" @change="load">
              <el-option label="综合排序" value="createTime" />
              <el-option label="价格排序" value="price" />
              <el-option label="销量排序" value="sales" />
            </el-select>
            <el-button type="primary" @click="load">搜索</el-button>
          </div>
        </div>

        <div class="selected-tags" v-if="query.categoryId || query.animeIpId">
          <span class="tags-label">已选筛选：</span>
          <span 
            v-if="query.categoryId" 
            class="tag-item category"
          >
            {{ getCategoryName(query.categoryId) }}
            <span class="tag-close" @click="onCategory('')">×</span>
          </span>
          <span 
            v-if="query.animeIpId" 
            class="tag-item anime-ip"
          >
            {{ getAnimeIpName(query.animeIpId) }}
            <span class="tag-close" @click="onAnimeIp('')">×</span>
          </span>
        </div>

        <div class="product-list" v-loading="loading">
          <div v-if="products.length === 0" class="empty-state">
            <el-empty description="暂无商品" />
          </div>
          <div v-else class="product-grid">
            <div 
              v-for="p in products" 
              :key="p.id" 
              class="product-card"
              @click="$router.push(`/products/${p.id}`)"
            >
              <div class="product-img-wrap">
                <img :src="p.mainImage" :alt="p.name" class="product-img" />
                <div class="product-tag hot" v-if="p.isHot">热销</div>
                <div class="product-tag new" v-if="p.isNew">新品</div>
              </div>
              <div class="product-info">
                <h4 class="product-name text-ellipsis-2">{{ p.name }}</h4>
                <div class="product-price">
                  <span class="price-current">¥{{ p.price }}</span>
                  <span class="price-original" v-if="p.originalPrice && p.originalPrice > p.price">¥{{ p.originalPrice }}</span>
                </div>
                <div class="product-meta">
                  <span>已售 {{ p.sales || 0 }}</span>
                  <span>库存 {{ p.stock || 0 }}</span>
                </div>
              </div>
            </div>
          </div>

          <el-pagination
            v-if="total > 0"
            v-model:current-page="query.page"
            v-model:page-size="query.size"
            :total="total"
            :page-sizes="[12, 24, 48]"
            layout="total, sizes, prev, pager, next"
            @size-change="load"
            @current-change="load"
            class="pagination"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getCategoriesApi, getProductsApi, getAnimeIpsApi } from '@/api/product'

const categories = ref([])
const animeIps = ref([])
const products = ref([])
const total = ref(0)
const loading = ref(false)
const query = ref({ 
  categoryId: '', 
  animeIpId: '', 
  keyword: '', 
  minPrice: null, 
  maxPrice: null, 
  sortBy: 'createTime', 
  order: 'desc', 
  page: 1, 
  size: 12 
})

const load = async () => {
  loading.value = true
  try {
    const params = { ...query.value }
    // 处理空字符串参数
    if (!params.animeIpId) delete params.animeIpId
    if (!params.categoryId) delete params.categoryId
    const res = await getProductsApi(params)
    products.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const onCategory = (id) => {
  query.value.categoryId = id || ''
  query.value.page = 1
  load()
}

const onAnimeIp = (id) => {
  query.value.animeIpId = id || ''
  query.value.page = 1
  load()
}

const getCategoryName = (id) => {
  const c = categories.value.find(c => String(c.id) === id)
  return c ? c.name : ''
}

const getAnimeIpName = (id) => {
  const ip = animeIps.value.find(ip => String(ip.id) === id)
  return ip ? ip.name : ''
}

onMounted(async () => {
  categories.value = await getCategoriesApi()
  animeIps.value = await getAnimeIpsApi()
  load()
})
</script>

<style scoped>
.products-page {
  min-height: 100vh;
  background: #f5f6f8;
  padding: 0;
}

.products-wrapper {
  display: flex;
  max-width: 1360px;
  margin: 0 auto;
  padding: 36px 32px 60px;
  gap: 28px;
  align-items: flex-start;
}

.sidebar {
  width: 240px;
  flex-shrink: 0;
  position: sticky;
  top: 88px;
  align-self: flex-start;
}

.category-card {
  background: #fff;
  border-radius: 16px;
  padding: 0;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04), 0 4px 12px rgba(0,0,0,0.03);
  overflow: hidden;
}

.category-header {
  padding: 18px 20px 14px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.category-icon {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  background: linear-gradient(135deg, #e8f5f3, #d4edea);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
  letter-spacing: 0.3px;
}

.category-list {
  display: flex;
  flex-direction: column;
  padding: 10px 0 14px;
}

.category-item {
  padding: 10px 20px;
  cursor: pointer;
  color: #555;
  font-size: 14px;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 10px;
}

.category-item::before {
  content: '';
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #ddd;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.category-item:hover {
  background: #f8faf9;
  color: #2a9d8f;
}

.category-item:hover::before {
  background: #2a9d8f;
  transform: scale(1.3);
}

.category-item.active {
  background: linear-gradient(90deg, #f0faf8, #e8f5f3);
  color: #2a9d8f;
  font-weight: 600;
}

.category-item.active::before {
  background: #2a9d8f;
  transform: scale(1.4);
}

.sidebar-card + .sidebar-card {
  margin-top: 16px;
}

.main-content {
  flex: 1;
  min-width: 0;
}

.filter-bar {
  background: #fff;
  border-radius: 16px;
  padding: 20px 24px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04), 0 4px 12px rgba(0,0,0,0.03);
}

.filter-left {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
}

.search-input {
  width: 200px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 10px !important;
  box-shadow: none !important;
  border: 1px solid #e8e8e8 !important;
  padding: 0 14px !important;
  transition: all 0.2s ease;
  background: #fafbfc;
}

.search-input :deep(.el-input__wrapper:hover) {
  border-color: #c0c0c0 !important;
}

.search-input :deep(.el-input__wrapper.is-focus) {
  border-color: #2a9d8f !important;
  box-shadow: 0 0 0 3px rgba(42,157,143,0.1) !important;
  background: #fff;
}

.price-filter {
  display: flex;
  align-items: center;
  gap: 10px;
}

.price-input {
  width: 108px;
}

.price-input :deep(.el-input) {
  width: 100%;
}

.price-input :deep(.el-input__wrapper) {
  border-radius: 10px !important;
  box-shadow: none !important;
  border: 1px solid #e8e8e8 !important;
  padding: 0 8px 0 12px !important;
  background: #fafbfc;
  transition: all 0.2s ease;
}

.price-input :deep(.el-input__wrapper:hover) {
  border-color: #c0c0c0 !important;
}

.price-input :deep(.el-input__wrapper.is-focus) {
  border-color: #2a9d8f !important;
  box-shadow: 0 0 0 3px rgba(42,157,143,0.1) !important;
  background: #fff;
}

.price-input :deep(.el-input__inner) {
  font-size: 13px;
  color: #333;
  text-align: center;
}

.price-input :deep(.el-input__suffix) {
  right: 6px;
}

.price-sep {
  color: #bbb;
  font-size: 14px;
  font-weight: 300;
}

.filter-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.sort-select {
  width: 130px;
}

.sort-select :deep(.el-input__wrapper) {
  border-radius: 10px !important;
  box-shadow: none !important;
  border: 1px solid #e8e8e8 !important;
  background: #fafbfc;
  transition: all 0.2s ease;
}

.sort-select :deep(.el-input__wrapper:hover) {
  border-color: #c0c0c0 !important;
}

.sort-select :deep(.el-select__wrapper) {
  box-shadow: none !important;
}

.filter-right :deep(.el-button--primary) {
  border-radius: 10px !important;
  padding: 10px 24px;
  background: linear-gradient(135deg, #2a9d8f, #249473) !important;
  border: none !important;
  font-weight: 500;
  letter-spacing: 0.5px;
  transition: all 0.25s ease;
}

.filter-right :deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #248c77, #1e8565) !important;
  transform: translateY(-1px);
  box-shadow: 0 4px 14px rgba(42,157,143,0.3);
}

.selected-tags {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  padding: 14px 0;
  margin-bottom: 4px;
}

.tags-label {
  font-size: 13px;
  color: #888;
  font-weight: 500;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.tag-item.category {
  background: linear-gradient(135deg, #e8f5f3, #d4edea);
  color: #2a9d8f;
  border: 1px solid rgba(42,157,143,0.2);
}

.tag-item.anime-ip {
  background: linear-gradient(135deg, #fef6e8, #fdedd3);
  color: #d4920a;
  border: 1px solid rgba(212,146,10,0.2);
}

.tag-close {
  cursor: pointer;
  font-size: 16px;
  line-height: 1;
  opacity: 0.6;
  transition: opacity 0.2s ease;
  margin-left: 2px;
}

.tag-close:hover {
  opacity: 1;
}

.product-list {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  min-height: 400px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04), 0 4px 12px rgba(0,0,0,0.03);
}

.empty-state {
  padding: 80px 0;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.product-card {
  position: relative;
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  border: 1px solid #f0f0f0;
  animation: fadeInUp 0.4s ease-out backwards;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(14px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.product-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(42,157,143,0.12), 0 4px 12px rgba(0,0,0,0.06);
  border-color: rgba(42,157,143,0.2);
}

.product-img-wrap {
  position: relative;
  padding-top: 100%;
  overflow: hidden;
  background: linear-gradient(135deg, #f8faf9 0%, #f0f4f3 100%);
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

.product-tag {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 5px 10px;
  font-size: 11px;
  border-radius: 6px;
  color: #fff;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 2;
}

.product-tag.hot {
  background: linear-gradient(135deg, #2a9d8f, #249473);
}

.product-tag.new {
  background: linear-gradient(135deg, #e9a820, #d4920a);
}

.product-info {
  padding: 14px 16px 16px;
}

.product-name {
  font-size: 14px;
  color: #2c2c2c;
  margin: 0 0 10px;
  line-height: 1.5;
  height: 42px;
  transition: color 0.2s ease;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-card:hover .product-name {
  color: #2a9d8f;
}

.product-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 6px;
}

.price-current {
  font-size: 18px;
  color: #2a9d8f;
  font-weight: 700;
}

.price-original {
  font-size: 12px;
  color: #bbb;
  text-decoration: line-through;
}

.product-meta {
  font-size: 12px;
  color: #999;
  display: flex;
  gap: 12px;
}

.pagination {
  margin-top: 28px;
  display: flex;
  justify-content: center;
}

.pagination :deep(.el-pagination) {
  background: #f8faf9;
  padding: 12px 20px;
  border-radius: 12px;
}

.pagination :deep(.el-pager li) {
  border-radius: 8px;
  margin: 0 3px;
  font-weight: 400;
  transition: all 0.2s ease;
  min-width: 32px;
  height: 32px;
  line-height: 32px;
}

.pagination :deep(.el-pager li:hover) {
  color: #2a9d8f;
}

.pagination :deep(.el-pager li.is-active) {
  background: linear-gradient(135deg, #2a9d8f, #249473);
  color: #fff;
  font-weight: 600;
}

@media (max-width: 1280px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 1100px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 992px) {
  .sidebar {
    display: none;
  }
  
  .products-wrapper {
    padding: 24px 24px 50px;
  }
}

@media (max-width: 768px) {
  .products-wrapper {
    padding: 16px 16px 40px;
  }
  
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .filter-bar {
    flex-direction: column;
    align-items: stretch;
    padding: 16px;
    border-radius: 12px;
  }
  
  .filter-left, .filter-right {
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .search-input {
    width: 100%;
  }
  
  .product-info {
    padding: 12px;
  }
}
</style>
