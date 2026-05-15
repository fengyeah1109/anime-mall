<template>
  <div class="admin-page">
    <div class="page-header">
      <h3 class="page-title">文章管理</h3>
      <el-button type="primary" @click="open()">
        <span class="btn-icon">+</span> 新增文章
      </el-button>
    </div>

    <div class="search-wrap">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="文章标题">
          <el-input v-model="searchForm.title" placeholder="请输入文章标题" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.category" placeholder="全部分类" clearable>
            <el-option label="资讯" value="资讯" />
            <el-option label="活动" value="活动" />
            <el-option label="公告" value="公告" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option label="已发布" :value="1" />
            <el-option label="草稿" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-wrap">
      <el-table :data="list.records || []" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="coverImage" label="封面" width="80" align="center">
          <template #default="{ row }">
            <img :src="row.coverImage" class="cover-img" v-if="row.coverImage" />
            <span v-else class="no-data">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <div class="article-title">{{ row.title }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="100" align="center">
          <template #default="{ row }">
            <span class="category-tag">{{ row.category || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览量" width="80" align="center">
          <template #default="{ row }">
            {{ row.viewCount || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <span class="status-tag" :class="row.status === 1 ? 'published' : 'draft'">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <span class="action-btn" @click="open(row)">编辑</span>
              <span 
                class="action-btn" 
                :class="row.status === 1 ? 'warning' : 'success'" 
                @click="toggleStatus(row)"
              >
                {{ row.status === 1 ? '下架' : '发布' }}
              </span>
              <span class="action-btn danger" @click="remove(row.id)">删除</span>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="list.total || 0"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="load"
          @current-change="load"
        />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑文章' : '新增文章'" width="700px">
      <el-form :model="form" label-width="80px" :rules="rules" ref="formRef">
        <el-form-item label="文章标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入文章标题" />
        </el-form-item>
        <el-form-item label="文章分类">
          <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="资讯" value="资讯" />
            <el-option label="活动" value="活动" />
            <el-option label="公告" value="公告" />
          </el-select>
        </el-form-item>
        <el-form-item label="封面图片">
          <el-input v-model="form.coverImage" placeholder="请输入封面图片URL" />
        </el-form-item>
        <el-form-item label="文章摘要">
          <el-input v-model="form.summary" type="textarea" :rows="2" placeholder="请输入文章摘要" />
        </el-form-item>
        <el-form-item label="文章内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="8" placeholder="请输入文章内容" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="状态">
              <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
              <span class="status-text">{{ form.status === 1 ? '发布' : '草稿' }}</span>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminArticlesApi } from '@/api/admin'
import request from '@/utils/request'

const loading = ref(false)
const list = ref({})
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const pagination = reactive({
  page: 1,
  size: 10
})

const searchForm = reactive({
  title: '',
  category: '',
  status: null
})

const defaultForm = {
  id: null,
  title: '',
  category: '',
  coverImage: '',
  summary: '',
  content: '',
  status: 0
}

const form = ref({ ...defaultForm })

const rules = {
  title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入文章内容', trigger: 'blur' }]
}

const load = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    }
    list.value = await adminArticlesApi(params)
  } catch (error) {
    ElMessage.error('加载文章列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  load()
}

const handleReset = () => {
  searchForm.title = ''
  searchForm.category = ''
  searchForm.status = null
  pagination.page = 1
  load()
}

const open = (row) => {
  if (row) {
    form.value = { ...row }
  } else {
    form.value = { ...defaultForm }
  }
  dialogVisible.value = true
}

const submit = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitLoading.value = true
  try {
    if (form.value.id) {
      await request.put(`/admin/articles/${form.value.id}`, form.value)
      ElMessage.success('更新成功')
    } else {
      await request.post('/admin/articles', form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    load()
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '发布' : '下架'
  
  try {
    await request.put(`/admin/articles/${row.id}/status`, null, { params: { status: newStatus } })
    row.status = newStatus
    ElMessage.success(`${actionText}成功`)
  } catch (error) {
    ElMessage.error(`${actionText}失败`)
  }
}

const remove = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该文章吗？', '提示', {
      type: 'warning'
    })
    await request.delete(`/admin/articles/${id}`)
    ElMessage.success('删除成功')
    load()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(load)
</script>

<style scoped>
.admin-page {
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
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.page-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #2d3436;
  letter-spacing: 0.5px;
}

.page-header :deep(.el-button) {
  border-radius: 12px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ff8e53 100%);
  border: none;
  transition: all 0.25s ease;
}

.page-header :deep(.el-button:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 107, 107, 0.35);
}

.btn-icon {
  margin-right: 5px;
}

.search-wrap {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  padding: 24px 28px;
  border-radius: 20px;
  margin-bottom: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.5);
  transition: all 0.3s ease;
}

.search-wrap:hover {
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.search-form :deep(.el-input) {
  --el-input-border-radius: 12px;
}

.search-form :deep(.el-select) {
  --el-select-border-color-hover: #ff6b6b;
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

.cover-img {
  width: 60px;
  height: 45px;
  object-fit: cover;
  border-radius: 10px;
  border: 2px solid rgba(255, 107, 107, 0.2);
  transition: all 0.3s ease;
}

.cover-img:hover {
  transform: scale(1.1);
  border-color: #ff6b6b;
}

.no-data {
  color: #b2bec3;
}

.article-title {
  color: #2d3436;
  font-weight: 500;
  line-height: 1.4;
}

.category-tag {
  display: inline-block;
  padding: 5px 14px;
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  border-radius: 25px;
  font-size: 12px;
  color: #1565c0;
  font-weight: 500;
}

.status-tag {
  display: inline-block;
  padding: 5px 14px;
  border-radius: 25px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.status-tag.published {
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  color: #2e7d32;
}

.status-tag.draft {
  background: linear-gradient(135deg, #f5f5f5 0%, #e8e8e8 100%);
  color: #636e72;
}

.action-btns {
  display: flex;
  justify-content: center;
  gap: 14px;
}

.action-btn {
  color: #ff6b6b;
  cursor: pointer;
  font-size: 14px;
  padding: 6px 16px;
  border-radius: 10px;
  transition: all 0.2s ease;
  font-weight: 500;
}

.action-btn:hover {
  background: rgba(255, 107, 107, 0.1);
  color: #ff5252;
}

.action-btn.success {
  color: #2e7d32;
}

.action-btn.success:hover {
  background: rgba(46, 125, 50, 0.1);
  color: #4caf50;
}

.action-btn.warning {
  color: #c62828;
}

.action-btn.warning:hover {
  background: rgba(198, 40, 40, 0.1);
  color: #ef5350;
}

.action-btn.danger {
  color: #c62828;
}

.action-btn.danger:hover {
  background: rgba(198, 40, 40, 0.1);
  color: #ef5350;
}

.pagination-wrap {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
  padding: 16px 0 0;
}

.pagination-wrap :deep(.el-pagination) {
  --el-pagination-hover-color: #ff6b6b;
}

.pagination-wrap :deep(.el-pagination button) {
  border-radius: 10px;
}

.pagination-wrap :deep(.el-pager li) {
  border-radius: 10px;
  transition: all 0.2s ease;
}

.pagination-wrap :deep(.el-pager li:hover) {
  color: #ff6b6b;
}

.pagination-wrap :deep(.el-pager li.is-active) {
  background: linear-gradient(135deg, #ff6b6b 0%, #ffa07a 100%);
  color: #fff;
}

.status-text {
  margin-left: 10px;
  color: #636e72;
  font-size: 13px;
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
</style>
