<template>
  <div class="article-detail" v-if="article">
    <div class="article-header">
      <div class="header-content">
        <router-link to="/articles" class="back-link">
          <span class="back-icon">←</span> 返回资讯
        </router-link>
        <span class="article-category" :class="getCategoryClass(article.category)">
          <span class="cat-icon">{{ getCategoryIcon(article.category) }}</span>
          {{ article.category || '综合' }}
        </span>
        <h1 class="article-title">{{ article.title }}</h1>
        <div class="article-meta">
          <span class="meta-item">{{ formatDate(article.publishTime) }}</span>
        </div>
      </div>
    </div>

    <div class="article-container">
      <div class="article-cover" v-if="article.coverImage">
        <img :src="article.coverImage" :alt="article.title" />
      </div>

      <div class="article-card">
        <div class="article-summary" v-if="article.summary">
          <p>{{ article.summary }}</p>
        </div>
        <div class="article-body" v-html="article.content"></div>
      </div>

      <div class="article-footer">
        <router-link to="/articles" class="back-btn">
          <span class="btn-icon">←</span>
          返回资讯列表
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getArticleDetailApi } from '@/api/product'

const route = useRoute()
const article = ref(null)

const categoryIcons = {
  '番剧': '🎬',
  '游戏': '🎮',
  '周边': '🎁',
  '活动': '🎉',
  '音乐': '🎵',
  '漫画': '📚',
  '综合': '🌟'
}

const categoryColors = {
  '番剧': 'tag-primary',
  '游戏': 'tag-primary',
  '周边': 'tag-primary',
  '活动': 'tag-primary',
  '音乐': 'tag-primary',
  '漫画': 'tag-primary',
  '综合': 'tag-primary'
}

onMounted(async () => { 
  article.value = await getArticleDetailApi(route.params.id) 
})

const getCategoryIcon = (category) => {
  return categoryIcons[category] || categoryIcons['综合']
}

const getCategoryClass = (category) => {
  return categoryColors[category] || categoryColors['综合']
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}
</script>

<style scoped>
.article-detail {
  min-height: 100vh;
  background: #FAFAFA;
  padding-bottom: 80px;
}

.article-header {
  position: relative;
  overflow: hidden;
  padding: 60px 40px 50px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  margin-bottom: 50px;
}

.article-header::before,
.article-header::after {
  content: '';
  position: absolute;
  border-radius: 50%;
  background: rgba(255,255,255,0.06);
}

.article-header::before {
  width: 180px;
  height: 180px;
  top: -70px;
  left: -50px;
  animation: float 8s ease-in-out infinite;
}

.article-header::after {
  width: 120px;
  height: 120px;
  top: 10px;
  right: -30px;
  animation: float 6s ease-in-out infinite reverse;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-15px) rotate(3deg); }
}

.header-content {
  position: relative;
  z-index: 2;
  max-width: 900px;
  margin: 0 auto;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: rgba(255,255,255,0.85);
  text-decoration: none;
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 20px;
  transition: all 0.25s ease;
  letter-spacing: 0.5px;
}

.back-link:hover {
  color: #fff;
  transform: translateX(-4px);
}

.back-icon {
  font-size: 16px;
}

.article-category {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 5px 12px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  background: rgba(255,255,255,0.15);
  color: #fff;
  margin-bottom: 16px;
  backdrop-filter: blur(4px);
}

.cat-icon {
  font-size: 11px;
}

.article-title {
  font-size: 36px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 16px;
  line-height: 1.3;
  letter-spacing: 0.5px;
  animation: fadeInUp 0.6s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.article-meta {
  color: rgba(255,255,255,0.75);
  font-size: 14px;
  letter-spacing: 0.3px;
}

.article-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 40px;
}

.article-cover {
  position: relative;
  width: 100%;
  max-height: 450px;
  overflow: hidden;
  border-radius: 12px;
  margin-bottom: 32px;
  box-shadow: 0 8px 30px rgba(102, 126, 234, 0.15), 0 2px 8px rgba(0,0,0,0.06);
}

.article-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s ease;
}

.article-cover:hover img {
  transform: scale(1.02);
}

.article-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04), 0 8px 24px rgba(0,0,0,0.04);
  animation: fadeInUp 0.6s ease-out 0.1s backwards;
}

.article-summary {
  padding: 24px 28px;
  background: linear-gradient(135deg, #f8f7ff, #f3f4f6);
  border-radius: 8px;
  margin-bottom: 32px;
  font-size: 15px;
  line-height: 1.8;
  color: #555;
  border-left: 3px solid #667eea;
}

.article-body {
  font-size: 16px;
  line-height: 1.9;
  color: #333;
}

.article-body :deep(p) {
  margin-bottom: 20px;
}

.article-body :deep(img) {
  max-width: 100%;
  height: auto;
  margin: 28px 0;
  border-radius: 10px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.06);
}

.article-body :deep(h2),
.article-body :deep(h3) {
  color: #1a1a2e;
  margin: 36px 0 18px;
  font-weight: 600;
}

.article-body :deep(h2) {
  font-size: 22px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}

.article-body :deep(h3) {
  font-size: 18px;
}

.article-body :deep(blockquote) {
  margin: 28px 0;
  padding: 20px 24px;
  background: linear-gradient(135deg, #f8f7ff, #f3f4f6);
  border-left: 3px solid #667eea;
  border-radius: 0 8px 8px 0;
  font-style: italic;
  color: #555;
}

.article-footer {
  text-align: center;
  margin-top: 50px;
}

.back-btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 12px 28px;
  background: transparent;
  color: #667eea;
  text-decoration: none;
  border-radius: 8px;
  font-weight: 500;
  font-size: 14px;
  letter-spacing: 0.5px;
  transition: all 0.3s ease;
  border: 1px solid #667eea;
  overflow: hidden;
}

.back-btn::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #667eea, #764ba2);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.35s ease;
  z-index: -1;
}

.back-btn:hover {
  color: #fff;
  border-color: transparent;
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
}

.back-btn:hover::before {
  transform: scaleX(1);
}

.back-btn:hover .btn-icon {
  transform: translateX(-3px);
}

.btn-icon {
  font-size: 16px;
  transition: transform 0.3s ease;
}

@media (max-width: 768px) {
  .article-header {
    padding: 40px 24px 36px;
  }
  
  .article-title {
    font-size: 26px;
  }
  
  .article-container {
    padding: 0 20px;
  }
  
  .article-cover {
    border-radius: 10px;
    margin-bottom: 24px;
  }
  
  .article-card {
    padding: 28px 20px;
    border-radius: 10px;
  }
  
  .article-body {
    font-size: 15px;
  }
  
  .article-summary {
    padding: 18px 20px;
  }
}
</style>
