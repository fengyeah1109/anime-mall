<template>
  <div class="admin-banners">
    <div class="page-header">
      <h2 class="page-title">轮播图管理</h2>
    </div>

    <div class="banner-actions">
      <el-button type="primary" @click="showAddDialog">添加轮播图</el-button>
    </div>

    <el-table :data="banners" style="width: 100%" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="图片" width="150">
        <template #default="{ row }">
          <el-image :src="row.imageUrl" fit="cover" style="width: 100px; height: 50px" />
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="linkUrl" label="链接" />
      <el-table-column prop="sortOrder" label="排序" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-switch v-model="row.status" @change="handleStatusChange(row)" :active-value="1" :inactive-value="0" />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="showEditDialog(row)">编辑</el-button>
          <el-button type="danger" size="small" @click="deleteBanner(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 添加/编辑轮播图对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑轮播图' : '添加轮播图'" width="500px">
      <el-form :model="bannerForm" :rules="bannerRules" ref="bannerFormRef" label-width="100px">
        <el-form-item label="图片地址" prop="imageUrl">
          <el-upload
            class="avatar-uploader"
            action=""
            :http-request="uploadImage"
            :show-file-list="false"
            :before-upload="beforeUpload"
          >
            <el-image v-if="bannerForm.imageUrl" :src="bannerForm.imageUrl" fit="cover" style="width: 100%; height: 150px" />
            <div v-else class="upload-placeholder">
              <el-icon><Plus /></el-icon>
              <div class="upload-text">点击上传图片</div>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="bannerForm.title" placeholder="请输入轮播图标题" />
        </el-form-item>
        <el-form-item label="链接" prop="linkUrl">
          <el-input v-model="bannerForm.linkUrl" placeholder="请输入点击跳转链接" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="bannerForm.sortOrder" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="bannerForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="loading">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getBannersApi, createBannerApi, updateBannerApi, deleteBannerApi } from '@/api/admin/banners'
import { uploadFileApi } from '@/api/common'

const banners = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const isEdit = ref(false)
const loading = ref(false)
const bannerForm = ref({
  imageUrl: '',
  linkUrl: '',
  title: '',
  sortOrder: 0,
  status: '1'
})
const bannerRules = {
  imageUrl: [
    { required: true, message: '请上传轮播图', trigger: 'blur' }
  ]
}
const bannerFormRef = ref(null)

// 加载轮播图列表
const loadBanners = async () => {
  loading.value = true
  try {
    const response = await getBannersApi(currentPage.value, pageSize.value)
    banners.value = response.records
    total.value = response.total
  } catch (error) {
    ElMessage.error('获取轮播图列表失败')
  } finally {
    loading.value = false
  }
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  loadBanners()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadBanners()
}

// 显示添加对话框
const showAddDialog = () => {
  isEdit.value = false
  bannerForm.value = {
    imageUrl: '',
    linkUrl: '',
    title: '',
    sortOrder: 0,
    status: 1
  }
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (row) => {
  isEdit.value = true
  bannerForm.value = { ...row }
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  if (!bannerFormRef.value) return
  await bannerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 转换 status 为整数
        const formData = {
          ...bannerForm.value,
          status: parseInt(bannerForm.value.status)
        }
        if (isEdit.value) {
          await updateBannerApi(bannerForm.value.id, formData)
          ElMessage.success('轮播图更新成功')
        } else {
          await createBannerApi(formData)
          ElMessage.success('轮播图添加成功')
        }
        dialogVisible.value = false
        loadBanners()
      } catch (error) {
        ElMessage.error(isEdit.value ? '轮播图更新失败' : '轮播图添加失败')
      } finally {
        loading.value = false
      }
    }
  })
}

// 删除轮播图
const deleteBanner = async (id) => {
  await ElMessageBox.confirm('确定要删除这个轮播图吗？', '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  loading.value = true
  try {
    await deleteBannerApi(id)
    ElMessage.success('轮播图删除成功')
    loadBanners()
  } catch (error) {
    ElMessage.error('轮播图删除失败')
  } finally {
    loading.value = false
  }
}

// 状态更新
const handleStatusChange = async (row) => {
  loading.value = true
  try {
    // 传递完整的轮播图数据，确保所有字段都被正确更新
    const updateData = {
      ...row,
      status: parseInt(row.status)
    }
    await updateBannerApi(row.id, updateData)
    ElMessage.success('状态更新成功')
  } catch (error) {
    ElMessage.error('状态更新失败')
    // 恢复原状态
    row.status = row.status === '1' ? '0' : '1'
  } finally {
    loading.value = false
  }
}

// 上传图片
const uploadImage = async (options) => {
  const formData = new FormData()
  formData.append('file', options.file)
  try {
    const response = await uploadFileApi(formData)
    handleImageSuccess(response)
  } catch (error) {
    ElMessage.error('图片上传失败')
  }
}

// 图片上传成功
const handleImageSuccess = (response) => {
  bannerForm.value.imageUrl = response
  ElMessage.success('图片上传成功')
  // 触发表单验证
  if (bannerFormRef.value) {
    bannerFormRef.value.validateField('imageUrl')
  }
}

// 图片上传前
const beforeUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/gif'
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isJPG) {
    ElMessage.error('只能上传 JPG、PNG 或 GIF 格式的图片')
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
  }
  return isJPG && isLt2M
}

// 初始加载
onMounted(() => {
  loadBanners()
})
</script>

<style scoped>
.admin-banners {
  padding: 20px;
  background: transparent;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 24px 28px;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.8);
}

.page-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  letter-spacing: 0.5px;
}

.banner-actions :deep(.el-button) {
  border-radius: 12px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ff8e53 100%);
  border: none;
  transition: all 0.25s ease;
}

.banner-actions :deep(.el-button:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 107, 107, 0.35);
}

.table-wrap {
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  padding: 24px;
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.8);
}

.table-wrap :deep(.el-table) {
  border-radius: 16px;
  overflow: hidden;
}

.table-wrap :deep(.el-table__header th) {
  background: linear-gradient(135deg, #ff6b6b 0%, #ffa07a 100%);
  color: #fff;
  font-weight: 700;
  padding: 16px 0;
  border: none;
  font-size: 14px;
  letter-spacing: 0.5px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.15);
}

.table-wrap :deep(.el-table__row) {
  transition: all 0.2s ease;
}

.table-wrap :deep(.el-table__row:hover) {
  background: rgba(255, 107, 107, 0.08);
}

.table-wrap :deep(.el-table__row:hover td) {
  background: rgba(255, 107, 107, 0.08) !important;
}

.table-wrap :deep(.el-table td) {
  border-bottom: 1px solid rgba(255, 107, 107, 0.12);
  padding: 16px 0;
  color: #1a1a1a;
  font-weight: 500;
}

.table-wrap :deep(.el-image) {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

.pagination :deep(.el-pagination) {
  --el-pagination-hover-color: #ff6b6b;
}

.pagination :deep(.el-pagination button) {
  border-radius: 10px;
}

.pagination :deep(.el-pager li) {
  border-radius: 10px;
  transition: all 0.2s ease;
}

.pagination :deep(.el-pager li:hover) {
  color: #ff6b6b;
}

.pagination :deep(.el-pager li.is-active) {
  background: linear-gradient(135deg, #ff6b6b 0%, #ffa07a 100%);
  color: #fff;
}

.avatar-uploader {
  border: 2px dashed rgba(255, 107, 107, 0.4);
  border-radius: 16px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  width: 100%;
  height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 107, 107, 0.03);
}

.avatar-uploader:hover {
  border-color: #ff6b6b;
  box-shadow: 0 4px 15px rgba(255, 107, 107, 0.2);
  background: rgba(255, 107, 107, 0.05);
}

.upload-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #636e72;
  transition: all 0.3s ease;
}

.upload-placeholder .el-icon {
  font-size: 48px;
  margin-bottom: 12px;
  color: rgba(255, 107, 107, 0.5);
  transition: all 0.3s ease;
}

.avatar-uploader:hover .upload-placeholder .el-icon {
  color: #ff6b6b;
  transform: scale(1.1);
}

.upload-text {
  margin-top: 8px;
  font-size: 14px;
  color: #636e72;
}

:deep(.el-dialog) {
  border-radius: 24px;
  overflow: hidden;
  backdrop-filter: blur(20px);
}

:deep(.el-dialog__header) {
  background: linear-gradient(135deg, #ff6b6b 0%, #ffa07a 100%);
  padding: 24px 28px;
  margin: 0;
}

:deep(.el-dialog__title) {
  color: #fff;
  font-size: 20px;
  font-weight: 700;
}

:deep(.el-dialog__close) {
  color: #fff;
  font-size: 20px;
}

:deep(.el-dialog__body) {
  padding: 28px;
  background: rgba(255, 255, 255, 0.95);
}

:deep(.el-dialog__footer) {
  padding: 20px 28px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  background: rgba(255, 255, 255, 0.95);
}

:deep(.el-button--small) {
  border-radius: 10px;
}
</style>
