<template>
  <div class="admin-page">
    <div class="page-header">
      <h3 class="page-title">用户管理</h3>
    </div>

    <div class="search-wrap">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
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
        <el-table-column prop="avatar" label="头像" width="70" align="center">
          <template #default="{ row }">
            <el-avatar :size="36" :src="row.avatar" v-if="row.avatar" />
            <el-avatar :size="36" v-else>{{ row.username?.charAt(0)?.toUpperCase() }}</el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="120">
          <template #default="{ row }">
            <div class="user-name">{{ row.username }}</div>
            <div class="user-nickname" v-if="row.nickname">{{ row.nickname }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130">
          <template #default="{ row }">
            <span :class="{ 'no-data': !row.phone }">{{ row.phone || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" width="180">
          <template #default="{ row }">
            <span :class="{ 'no-data': !row.email }">{{ row.email || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <span class="status-tag" :class="row.status === 1 ? 'normal' : 'disabled'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <span 
                class="action-btn" 
                :class="row.status === 1 ? 'warning' : 'success'" 
                @click="toggleStatus(row)"
              >
                {{ row.status === 1 ? '禁用' : '启用' }}
              </span>
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
  </div>
</template>

<script setup>
import { onMounted, ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminUsersApi } from '@/api/admin'
import request from '@/utils/request'

const loading = ref(false)
const list = ref({})
const pagination = reactive({
  page: 1,
  size: 10
})
const searchForm = reactive({
  username: '',
  phone: '',
  status: null
})

const load = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    }
    list.value = await adminUsersApi(params)
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  load()
}

const handleReset = () => {
  searchForm.username = ''
  searchForm.phone = ''
  searchForm.status = null
  pagination.page = 1
  load()
}

const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const actionText = newStatus === 0 ? '禁用' : '启用'
  
  try {
    await ElMessageBox.confirm(`确定要${actionText}该用户吗？`, '提示', {
      type: 'warning'
    })
    
    await request.put(`/admin/users/${row.id}/status`, null, { params: { status: newStatus } })
    row.status = newStatus
    ElMessage.success(`${actionText}成功`)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${actionText}失败`)
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

.search-wrap {
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  padding: 24px 28px;
  border-radius: 20px;
  margin-bottom: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
}

.search-wrap:hover {
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
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

.user-name {
  color: #2d3436;
  font-weight: 600;
}

.user-nickname {
  font-size: 12px;
  color: #636e72;
  margin-top: 4px;
}

.no-data {
  color: #b2bec3;
}

.status-tag {
  display: inline-block;
  padding: 5px 14px;
  border-radius: 25px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.status-tag.normal {
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  color: #2e7d32;
}

.status-tag.disabled {
  background: linear-gradient(135deg, #fff5f5 0%, #ffcdd2 100%);
  color: #c62828;
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
  color: #e65100;
}

.action-btn.warning:hover {
  background: rgba(230, 81, 0, 0.1);
  color: #ff8a50;
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
</style>
