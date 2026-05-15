<template>
  <div class="admin-page">
    <div class="page-header">
      <h3 class="page-title">售后管理</h3>
    </div>

    <div class="search-wrap">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="售后单号">
          <el-input v-model="searchForm.id" placeholder="请输入售后单号" clearable />
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option label="待审核" :value="0" />
            <el-option label="审核通过" :value="1" />
            <el-option label="审核拒绝" :value="2" />
            <el-option label="已完成" :value="3" />
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
        <el-table-column prop="id" label="售后单号" width="100" align="center" />
        <el-table-column prop="orderNo" label="订单号" width="120" align="center" />
        <el-table-column label="商品" min-width="150">
          <template #default="{ row }">
            {{ row.productName }}
          </template>
        </el-table-column>
        <el-table-column prop="type" label="售后类型" width="100" align="center">
          <template #default="{ row }">
            <span>{{ getAfterSaleTypeText(row.type) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="售后原因" min-width="150">
          <template #default="{ row }">
            <span class="reason-text">{{ row.reason }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="处理状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.applyTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <span class="action-btn" @click="viewDetail(row)">详情</span>
              <span
                v-if="row.status === 0"
                class="action-btn success"
                @click="handleApprove(row)"
              >
                同意
              </span>
              <span
                v-if="row.status === 0"
                class="action-btn danger"
                @click="handleReject(row)"
              >
                拒绝
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
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadAfterSales"
          @current-change="loadAfterSales"
        />
      </div>
    </div>

    <el-dialog v-model="detailVisible" title="售后详情" width="600px">
      <div v-if="currentAfterSale" class="after-sale-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="售后单号">{{ currentAfterSale.id }}</el-descriptions-item>
          <el-descriptions-item label="订单号">{{ currentAfterSale.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="商品">
            {{ currentAfterSale.productName }}
          </el-descriptions-item>
          <el-descriptions-item label="售后类型">{{ getAfterSaleTypeText(currentAfterSale.type) }}</el-descriptions-item>
          <el-descriptions-item label="售后原因">{{ currentAfterSale.reason }}</el-descriptions-item>
          <el-descriptions-item v-if="currentAfterSale.description" label="问题描述">
            {{ currentAfterSale.description }}
          </el-descriptions-item>
          <el-descriptions-item label="处理状态">
            <el-tag :type="getStatusType(currentAfterSale.status)">{{ getStatusText(currentAfterSale.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ formatTime(currentAfterSale.applyTime) }}</el-descriptions-item>
          <el-descriptions-item v-if="currentAfterSale.handleTime" label="处理时间">
            {{ formatTime(currentAfterSale.handleTime) }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentAfterSale.reply" label="管理员回复">
            {{ currentAfterSale.reply }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <el-dialog v-model="approveVisible" title="同意售后" width="500px">
      <el-form :model="approveForm" label-width="100px">
        <el-form-item label="售后单号">
          <el-input :value="currentAfterSale?.id" disabled />
        </el-form-item>
        <el-form-item label="回复">
          <el-input v-model="approveForm.reply" type="textarea" :rows="3" placeholder="请输入回复" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmApprove" :loading="approveLoading">确认同意</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rejectVisible" title="拒绝售后" width="500px">
      <el-form :model="rejectForm" label-width="100px">
        <el-form-item label="售后单号">
          <el-input :value="currentAfterSale?.id" disabled />
        </el-form-item>
        <el-form-item label="拒绝原因" required>
          <el-input v-model="rejectForm.reply" type="textarea" :rows="3" placeholder="请输入拒绝原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject" :loading="rejectLoading">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminAfterSalesApi, adminUpdateAfterSaleStatusApi } from '@/api/admin'

const loading = ref(false)
const list = ref({})
const searchForm = reactive({
  id: '',
  status: null
})
const pagination = reactive({
  page: 1,
  size: 10
})

const detailVisible = ref(false)
const currentAfterSale = ref(null)

const approveVisible = ref(false)
const approveForm = reactive({
  reply: ''
})
const approveLoading = ref(false)

const rejectVisible = ref(false)
const rejectForm = reactive({
  reply: ''
})
const rejectLoading = ref(false)

const loadAfterSales = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size
    }
    if (searchForm.status !== null) {
      params.status = searchForm.status
    }
    if (searchForm.id) {
      params.id = searchForm.id
    }
    list.value = await adminAfterSalesApi(params)
  } catch (error) {
    ElMessage.error('加载售后列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadAfterSales()
}

const handleReset = () => {
  searchForm.id = ''
  searchForm.status = null
  pagination.page = 1
  loadAfterSales()
}

const viewDetail = (row) => {
  currentAfterSale.value = row
  detailVisible.value = true
}

const handleApprove = (row) => {
  currentAfterSale.value = row
  approveForm.reply = ''
  approveVisible.value = true
}

const confirmApprove = async () => {
  try {
    approveLoading.value = true
    await adminUpdateAfterSaleStatusApi(currentAfterSale.value.id, {
      status: 1,
      reply: approveForm.reply
    })
    ElMessage.success('已同意售后申请')
    approveVisible.value = false
    loadAfterSales()
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    approveLoading.value = false
  }
}

const handleReject = (row) => {
  currentAfterSale.value = row
  rejectForm.reply = ''
  rejectVisible.value = true
}

const confirmReject = async () => {
  if (!rejectForm.reply) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  try {
    rejectLoading.value = true
    await adminUpdateAfterSaleStatusApi(currentAfterSale.value.id, {
      status: 2,
      reply: rejectForm.reply
    })
    ElMessage.success('已拒绝售后申请')
    rejectVisible.value = false
    loadAfterSales()
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    rejectLoading.value = false
  }
}

const getStatusText = (status) => {
  const statusMap = {
    0: '待审核',
    1: '审核通过',
    2: '审核拒绝',
    3: '已完成'
  }
  return statusMap[status] || '未知'
}

const getStatusType = (status) => {
  const typeMap = {
    0: 'warning',
    1: 'primary',
    2: 'danger',
    3: 'success'
  }
  return typeMap[status] || ''
}

const getAfterSaleTypeText = (type) => {
  const typeMap = {
    1: '退货退款',
    2: '仅退款',
    3: '换货'
  }
  return typeMap[type] || '未知'
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

onMounted(loadAfterSales)
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

.reason-text {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  display: box;
  line-clamp: 2;
  box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #2d3436;
  font-weight: 500;
}

.action-btns {
  display: flex;
  justify-content: center;
  gap: 14px;
}

.action-btn {
  color: #ff5252;
  cursor: pointer;
  font-size: 14px;
  padding: 6px 16px;
  border-radius: 10px;
  transition: all 0.2s ease;
  font-weight: 600;
}

.action-btn:hover {
  background: rgba(255, 82, 82, 0.12);
  color: #ff3d3d;
}

.action-btn.success {
  color: #2e7d32;
}

.action-btn.success:hover {
  background: rgba(46, 125, 50, 0.1);
  color: #4caf50;
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

.after-sale-detail {
  padding: 10px 0;
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

:deep(.el-tag) {
  border-radius: 20px;
  padding: 0 12px;
}

:deep(.el-descriptions) {
  font-size: 14px;
}

:deep(.el-descriptions__label) {
  background: rgba(255, 107, 107, 0.05);
  color: #636e72;
  font-weight: 500;
}

:deep(.el-descriptions__content) {
  color: #2d3436;
}
</style>
