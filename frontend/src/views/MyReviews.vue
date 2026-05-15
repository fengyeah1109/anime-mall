<template>
  <div class="my-reviews">
    <div class="page-header">
      <h1>我的评价</h1>
    </div>
    
    <div class="reviews-list" v-if="reviews.length > 0">
      <div v-for="review in reviews" :key="review.id" class="review-item">
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
               class="review-image" 
               @click="previewImage(img)" />
        </div>
        <div class="review-product">
          <img :src="getImageUrl(review.productImage)" class="product-img" />
          <div class="product-info">
            <div class="product-name">{{ review.productName }}</div>
            <div class="product-price">¥{{ review.price }}</div>
          </div>
        </div>
        <div class="review-actions">
          <el-button type="primary" size="small" @click="editReview(review)">编辑评价</el-button>
          <el-button type="danger" size="small" @click="deleteReview(review.id)">删除评价</el-button>
        </div>
      </div>
    </div>
    
    <div v-else class="no-reviews">
      <div class="no-reviews-icon">⭐</div>
      <p class="no-reviews-text">暂无评价</p>
    </div>
    
    <!-- 图片预览对话框 -->
    <el-dialog v-model="previewVisible" title="图片预览" width="80%">
      <img :src="previewImageUrl" class="preview-image" />
    </el-dialog>
    
    <!-- 编辑评价对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑评价" width="600px">
      <el-form :model="editForm" ref="editFormRef" label-width="80px">
        <el-form-item label="商品信息" v-if="editForm.productName">
          <div class="review-product-info">
            <img :src="getImageUrl(editForm.productImage)" class="review-product-img" />
            <div class="review-product-details">
              <div class="review-product-name">{{ editForm.productName }}</div>
              <div class="review-product-price">¥{{ editForm.price }}</div>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="评分" prop="rating">
          <el-rate v-model="editForm.rating" :max="5" show-score text-color="#ff9900" />
        </el-form-item>
        <el-form-item label="评价内容" prop="content">
          <el-input v-model="editForm.content" type="textarea" :rows="4" placeholder="请输入评价内容" />
        </el-form-item>
        <el-form-item label="上传图片">
          <el-upload
            class="review-uploader"
            action=""
            :auto-upload="false"
            :on-change="handleEditImageChange"
            :file-list="editForm.images"
            list-type="picture-card"
            :limit="5"
          >
            <template #trigger>
              <el-icon><Plus /></el-icon>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="editLoading">保存修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getReviewsApi, updateReviewApi, deleteReviewApi } from '@/api/reviews'
import { getImageUrl, parseReviewImages } from '@/utils/image'

const reviews = ref([])
const loading = ref(false)
const previewVisible = ref(false)
const previewImageUrl = ref('')
const editDialogVisible = ref(false)
const editFormRef = ref()
const editLoading = ref(false)
const editForm = ref({
  id: null,
  productName: '',
  productImage: '',
  price: '',
  rating: 5,
  content: '',
  images: []
})

onMounted(async () => {
  await loadReviews()
})

const loadReviews = async () => {
  loading.value = true
  try {
    reviews.value = await getReviewsApi()
  } catch (error) {
    ElMessage.error('获取评价失败')
  } finally {
    loading.value = false
  }
}

const formatTime = (time) => {
  if (!time) return '-' 
  return new Date(time).toLocaleString('zh-CN')
}

const previewImage = (imageUrl) => {
  previewImageUrl.value = imageUrl
  previewVisible.value = true
}

const editReview = (review) => {
  // 复制评价数据到编辑表单
  editForm.value = {
    id: review.id,
    productName: review.productName,
    productImage: review.productImage,
    price: review.price,
    rating: review.rating,
    content: review.content,
    images: []
  }
  
  // 处理已有的评价图片
  if (review.images) {
    const imageUrls = parseReviewImages(review.images)
    editForm.value.images = imageUrls.map(url => ({
      url: url,
      name: url.split('/').pop(),
      status: 'success'
    }))
  }
  
  editDialogVisible.value = true
}

const handleEditImageChange = (file, fileList) => {
  // 确保文件列表中的每个文件都有 url 属性
  const processedFileList = fileList.map(f => ({
    ...f,
    url: f.url || f.raw?.preview || ''
  }))
  editForm.value.images = processedFileList
}

const submitEdit = async () => {
  try {
    await editFormRef.value.validate()
    
    // 准备图片数据
    const images = editForm.value.images.map(file => file.raw).filter(Boolean)
    
    editLoading.value = true
    await updateReviewApi({
      id: editForm.value.id,
      rating: editForm.value.rating,
      content: editForm.value.content,
      images: images
    })
    ElMessage.success('评价更新成功')
    editDialogVisible.value = false
    await loadReviews()
  } catch (error) {
    if (error !== false) {
      ElMessage.error('更新失败')
    }
  } finally {
    editLoading.value = false
  }
}

const deleteReview = async (reviewId) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评价吗？', '删除评价', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteReviewApi(reviewId)
    ElMessage.success('评价删除成功')
    await loadReviews()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}
</script>

<style scoped>
.my-reviews {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
  border-bottom: 1px solid #e0e0e0;
  padding-bottom: 15px;
}

.page-header h1 {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.review-item {
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.review-rating {
  flex: 1;
}

.review-time {
  font-size: 12px;
  color: var(--text-light);
}

.review-content {
  font-size: 14px;
  color: var(--text-color);
  line-height: 1.6;
  margin-bottom: 15px;
  padding: 10px;
  background: #f9f9f9;
  border-radius: 4px;
}

.review-images {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 15px;
}

.review-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #eee;
  cursor: pointer;
  transition: transform 0.2s;
}

.review-image:hover {
  transform: scale(1.05);
}

.review-product {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: #f5f5f5;
  border-radius: 4px;
  margin-bottom: 15px;
}

.product-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 5px;
}

.product-price {
  font-size: 14px;
  color: #ff4d4f;
  font-weight: 500;
}

.review-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.no-reviews {
  text-align: center;
  padding: 60px 0;
  color: var(--text-light);
}

.no-reviews-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.no-reviews-text {
  font-size: 16px;
  margin: 0;
}

.preview-image {
  width: 100%;
  max-height: 600px;
  object-fit: contain;
}

.review-product-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.review-product-img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.review-product-details {
  flex: 1;
}

.review-product-name {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 5px;
}

.review-product-price {
  font-size: 14px;
  color: #ff4d4f;
  font-weight: 500;
}

.review-uploader {
  margin-top: 10px;
}

.uploaded-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
