<template>
  <div class="articles-page">
    <div class="page-header">
      <div class="header-bg">
        <div class="bg-circle circle-1"></div>
        <div class="bg-circle circle-2"></div>
        <div class="bg-circle circle-3"></div>
        <div class="bg-circle circle-4"></div>
        <div class="bg-dots"></div>
      </div>
      <div class="header-content">
        <span class="header-badge">✨ 精彩内容</span>
        <h1 class="page-title">二次元资讯站</h1>
        <p class="page-subtitle">发现动漫世界的每一个精彩瞬间</p>
      </div>
      <div class="header-line"></div>
    </div>

    <div class="articles-container">
      <div 
        v-for="(article, index) in articles.records" 
        :key="article.id" 
        class="article-card"
        :class="{ 'featured': index === 0 }"
        :style="{ animationDelay: `${index * 0.06}s` }"
        @click="$router.push(`/articles/${article.id}`)"
      >
        <div class="card-shine"></div>
        <div class="article-cover" v-if="article.coverImage">
          <img :src="article.coverImage" :alt="article.title" />
          <div class="cover-overlay"></div>
        </div>
        <div class="article-category-fixed" :class="getCategoryClass(article.category)">
          <span class="cat-icon">{{ getCategoryIcon(article.category) }}</span>
          {{ article.category || '综合' }}
        </div>
        <div class="article-body">
          <h2 class="article-title">{{ article.title }}</h2>
          <p class="article-summary">{{ article.summary || '暂无摘要' }}</p>
          <div class="article-footer">
            <span class="article-time">
              <svg class="time-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <path d="M12 6v6l4 2"/>
              </svg>
              {{ formatDate(article.publishTime) }}
            </span>
            <span class="read-more">
              阅读全文
              <svg class="arrow-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M5 12h14M12 5l7 7-7 7"/>
              </svg>
            </span>
          </div>
        </div>
      </div>
    </div>

    <div class="empty-state" v-if="!articles.records || articles.records.length === 0">
      <div class="empty-icon">📭</div>
      <p class="empty-text">暂无资讯内容</p>
      <p class="empty-hint">敬请期待更多精彩内容</p>
    </div>

    <div class="pagination" v-if="articles.total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[5, 10, 15, 20]"
        layout="total, sizes, prev, pager, next"
        :total="articles.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getArticlesApi } from '@/api/product'

const articles = ref({})
const currentPage = ref(1)
const pageSize = ref(5)

const categoryIcons = {
  '番剧': '🎬',
  '游戏': '🎮',
  '周边': '🎁',
  '活动': '🎉',
  '音乐': '🎵',
  '漫画': '📚',
  '综合': '🌟'
}

const getCategoryIcon = (category) => {
  return categoryIcons[category] || categoryIcons['综合']
}

const getCategoryClass = (category) => {
  const colorMap = {
    '番剧': 'tag-movie',
    '游戏': 'tag-game',
    '周边': 'tag-goods',
    '活动': 'tag-event',
    '音乐': 'tag-music',
    '漫画': 'tag-comic',
    '综合': 'tag-general'
  }
  return colorMap[category] || colorMap['综合']
}

onMounted(async () => {
  await loadArticles()
})

const loadArticles = async () => {
  articles.value = await getArticlesApi({ 
    page: currentPage.value, 
    size: pageSize.value 
  })
}

const handleSizeChange = (size) => {
  pageSize.value = size
  loadArticles()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadArticles()
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}
</script>

<style scoped>
.articles-page {
  min-height: 100vh;
  background: #fafbfc;
  padding: 0 0 80px;
  overflow-x: hidden;
}

.page-header {
  position: relative;
  padding: 80px 40px 100px;
  text-align: center;
  overflow: hidden;
  background: linear-gradient(165deg, #fff5f5 0%, #fff9f0 50%, #f0fff4 100%);
}

.header-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.5;
}

.circle-1 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(255,107,107,0.15) 0%, transparent 70%);
  top: -100px;
  left: -80px;
  animation: float 8s ease-in-out infinite;
}

.circle-2 {
  width: 250px;
  height: 250px;
  background: radial-gradient(circle, rgba(42,157,143,0.12) 0%, transparent 70%);
  top: -60px;
  right: -60px;
  animation: float 10s ease-in-out infinite reverse;
}

.circle-3 {
  width: 180px;
  height: 180px;
  background: radial-gradient(circle, rgba(255,183,77,0.18) 0%, transparent 70%);
  bottom: -40px;
  left: 10%;
  animation: float 7s ease-in-out infinite 1s;
}

.circle-4 {
  width: 120px;
  height: 120px;
  background: radial-gradient(circle, rgba(106,166,221,0.15) 0%, transparent 70%);
  bottom: 20px;
  right: 15%;
  animation: float 9s ease-in-out infinite 2s;
}

.bg-dots {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(rgba(42,157,143,0.06) 1px, transparent 1px);
  background-size: 28px 28px;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(10px, -15px) scale(1.02); }
  66% { transform: translate(-5px, 8px) scale(0.98); }
}

.header-content {
  position: relative;
  z-index: 2;
}

.header-badge {
  display: inline-block;
  padding: 8px 20px;
  background: linear-gradient(135deg, rgba(255,107,107,0.1), rgba(255,183,77,0.1));
  border: 1px solid rgba(255,107,107,0.2);
  border-radius: 30px;
  font-size: 13px;
  color: #e07878;
  margin-bottom: 20px;
  animation: fadeInDown 0.6s ease-out;
}

.page-title {
  font-size: 42px;
  font-weight: 700;
  margin: 0 0 14px;
  background: linear-gradient(135deg, #ff6b6b, #ffa07a, #ffd93d);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 2px;
  animation: fadeInUp 0.8s ease-out 0.1s backwards;
}

.page-subtitle {
  font-size: 16px;
  margin: 0;
  color: #888;
  font-weight: 400;
  letter-spacing: 0.5px;
  animation: fadeInUp 0.8s ease-out 0.2s backwards;
}

.header-line {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 120px;
  height: 3px;
  background: linear-gradient(90deg, transparent, #ff6b6b, #ffa07a, #ffd93d, transparent);
  border-radius: 2px;
}

@keyframes fadeInDown {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.articles-container {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 28px;
  max-width: 1280px;
  margin: -40px auto 0;
  padding: 0 40px;
  position: relative;
  z-index: 3;
}

.article-card {
  position: relative;
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  box-shadow: 0 4px 20px rgba(0,0,0,0.06), 0 1px 3px rgba(0,0,0,0.04);
  border: 1px solid rgba(0,0,0,0.04);
  animation: cardFadeIn 0.5s ease-out backwards;
}

@keyframes cardFadeIn {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.article-card.featured {
  grid-column: span 2;
  grid-row: span 1;
}

.article-card.featured .article-cover {
  height: 280px;
}

.article-card.featured .article-title {
  font-size: 22px;
  display: block !important;
  -webkit-line-clamp: unset !important;
  line-clamp: unset !important;
  overflow: visible !important;
  text-overflow: unset !important;
  white-space: normal !important;
}

.article-card.featured .article-summary {
  display: block !important;
  -webkit-line-clamp: unset !important;
  line-clamp: unset !important;
  -webkit-box-orient: unset !important;
  overflow: visible !important;
  text-overflow: unset !important;
  white-space: normal !important;
  line-height: 1.8 !important;
}

.article-card.featured .article-body {
  padding: 24px 28px;
}

.article-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 50px rgba(255,107,107,0.15), 0 8px 20px rgba(0,0,0,0.08);
}

.card-shine {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.4), transparent);
  transition: left 0.6s ease;
  pointer-events: none;
  z-index: 10;
}

.article-card:hover .card-shine {
  left: 100%;
}

.article-cover {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.article-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.article-card:hover .article-cover img {
  transform: scale(1.1);
}

.cover-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to bottom, transparent 40%, rgba(0,0,0,0.4));
  opacity: 0;
  transition: opacity 0.3s ease;
}

.article-card:hover .cover-overlay {
  opacity: 1;
}

.article-category-fixed {
  position: absolute;
  top: 14px;
  left: 14px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 25px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  box-shadow: 0 4px 15px rgba(0,0,0,0.2);
  z-index: 5;
  backdrop-filter: blur(10px);
}

.tag-movie { background: linear-gradient(135deg, #ff6b6b, #ee5a5a); }
.tag-game { background: linear-gradient(135deg, #a855f7, #9333ea); }
.tag-goods { background: linear-gradient(135deg, #22c997, #1db386); }
.tag-event { background: linear-gradient(135deg, #fbbf24, #f59e0b); }
.tag-music { background: linear-gradient(135deg, #60a5fa, #3b82f6); }
.tag-comic { background: linear-gradient(135deg, #fb923c, #f97316); }
.tag-general { background: linear-gradient(135deg, #22c997, #1db386); }

.article-body {
  padding: 20px 22px 22px;
}

.article-title {
  font-size: 17px;
  font-weight: 600;
  margin: 0 0 12px;
  color: #2d2d2d;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.3s ease;
}

.article-card:hover .article-title {
  color: #ff6b6b;
}

.article-summary {
  font-size: 14px;
  line-height: 1.7;
  color: #888;
  margin: 0 0 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 14px;
  border-top: 1px solid #f5f5f5;
}

.article-time {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #aaa;
}

.time-icon {
  width: 14px;
  height: 14px;
}

.read-more {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 500;
  color: #ff6b6b;
  transition: gap 0.3s ease;
}

.article-card:hover .read-more {
  gap: 10px;
}

.arrow-icon {
  width: 16px;
  height: 16px;
  transition: transform 0.3s ease;
}

.article-card:hover .arrow-icon {
  transform: translateX(4px);
}

.empty-state {
  text-align: center;
  padding: 100px 20px;
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
  opacity: 0.5;
}

.empty-text {
  font-size: 18px;
  font-weight: 500;
  color: #666;
  margin: 0 0 8px;
}

.empty-hint {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 50px;
}

.pagination :deep(.el-pagination) {
  background: #fff;
  padding: 14px 28px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
}

.pagination :deep(.el-pager li) {
  border-radius: 10px;
  margin: 0 4px;
  transition: all 0.25s ease;
  min-width: 36px;
  height: 36px;
  line-height: 36px;
  font-size: 14px;
}

.pagination :deep(.el-pager li:hover) {
  color: #ff6b6b;
}

.pagination :deep(.el-pager li.is-active) {
  background: linear-gradient(135deg, #ff6b6b, #ffa07a);
  color: #fff;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(255,107,107,0.3);
}

@media (max-width: 1200px) {
  .articles-container {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .article-card.featured {
    grid-column: span 2;
    grid-row: span 1;
  }
  
  .article-card.featured .article-cover {
    height: 240px;
  }
}

@media (max-width: 768px) {
  .page-header {
    padding: 60px 24px 70px;
  }
  
  .page-title {
    font-size: 30px;
  }
  
  .articles-container {
    grid-template-columns: 1fr;
    padding: 0 20px;
    gap: 20px;
    margin-top: -30px;
  }
  
  .article-card.featured {
    grid-column: span 1;
  }
  
  .article-card.featured .article-cover {
    height: 200px;
  }
  
  .article-card.featured .article-title {
    font-size: 18px;
  }
}
</style>
