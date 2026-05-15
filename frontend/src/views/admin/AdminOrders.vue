<template>
  <div class="admin-page">
    <div class="page-header">
      <h3 class="page-title">订单管理</h3>
    </div>

    <div class="search-wrap">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input v-model="searchForm.userId" placeholder="请输入用户ID" clearable />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option label="待付款" :value="1" />
            <el-option label="待发货" :value="2" />
            <el-option label="待收货" :value="3" />
            <el-option label="已完成" :value="4" />
            <el-option label="已取消" :value="0" />
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
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="username" label="用户" width="140">
          <template #default="{ row }">
            <div class="user-info">
              <span class="user-name">{{ row.username || '-' }}</span>
              <span class="user-phone" v-if="row.userPhone">{{ row.userPhone }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="订单金额" width="100" align="right">
          <template #default="{ row }">
            <span class="price">¥{{ row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="订单状态" width="90" align="center">
          <template #default="{ row }">
            <span class="status-tag" :class="'status-' + row.status">
              {{ getStatusText(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="payType" label="支付方式" width="90" align="center">
          <template #default="{ row }">
            <span class="pay-type">{{ getPayTypeText(row.payType) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="paymentTime" label="支付时间" width="160">
          <template #default="{ row }">
            <span :class="{ 'no-data': !row.paymentTime }">
              {{ row.paymentTime ? formatTime(row.paymentTime) : '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <span class="action-btn" @click="viewDetail(row)">详情</span>
              <span 
                class="action-btn success" 
                @click="handleDeliver(row)" 
                v-if="row.status === 2"
              >
                发货
              </span>
              <span 
                class="action-btn warning" 
                @click="handleUpdateStatus(row)" 
                v-if="row.status !== 0 && row.status !== 4"
              >
                改状态
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
          @size-change="loadOrders"
          @current-change="loadOrders"
        />
      </div>
    </div>

    <el-dialog v-model="detailVisible" title="订单详情" width="800px">
      <div v-if="currentOrder" class="order-detail">
        <div class="detail-section">
          <div class="section-title">基本信息</div>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="label">订单号：</span>
              <span class="value">{{ currentOrder.orderNo }}</span>
            </div>
            <div class="detail-item">
              <span class="label">订单状态：</span>
              <el-tag :type="getStatusType(currentOrder.status)">
                {{ getStatusText(currentOrder.status) }}
              </el-tag>
            </div>
            <div class="detail-item">
              <span class="label">支付方式：</span>
              <span class="value">{{ getPayTypeText(currentOrder.payType) }}</span>
            </div>
            <div class="detail-item">
              <span class="label">下单时间：</span>
              <span class="value">{{ formatTime(currentOrder.createTime) }}</span>
            </div>
            <div class="detail-item">
              <span class="label">支付时间：</span>
              <span class="value">{{ currentOrder.paymentTime ? formatTime(currentOrder.paymentTime) : '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">发货时间：</span>
              <span class="value">{{ currentOrder.deliveryTime ? formatTime(currentOrder.deliveryTime) : '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">收货时间：</span>
              <span class="value">{{ currentOrder.receiveTime ? formatTime(currentOrder.receiveTime) : '-' }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">用户信息</div>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="label">用户名：</span>
              <span class="value">{{ currentOrder.username || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">手机号：</span>
              <span class="value">{{ currentOrder.userPhone || '-' }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">金额信息</div>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="label">商品总额：</span>
              <span class="value price">¥{{ currentOrder.totalAmount }}</span>
            </div>
            <div class="detail-item">
              <span class="label">运费：</span>
              <span class="value">¥{{ currentOrder.freightAmount || 0 }}</span>
            </div>
            <div class="detail-item">
              <span class="label">优惠金额：</span>
              <span class="value">¥{{ currentOrder.discountAmount || 0 }}</span>
            </div>
            <div class="detail-item">
              <span class="label">实付金额：</span>
              <span class="value price">¥{{ currentOrder.payAmount }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">收货地址</div>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="label">收货人：</span>
              <span class="value">{{ currentOrder.receiverName || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">联系电话：</span>
              <span class="value">{{ currentOrder.receiverPhone || '-' }}</span>
            </div>
            <div class="detail-item full-width">
              <span class="label">详细地址：</span>
              <span class="value">{{ currentOrder.fullAddress || '-' }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section" v-if="currentOrder.items && currentOrder.items.length > 0">
          <div class="section-title">商品信息</div>
          <el-table :data="currentOrder.items" size="small">
            <el-table-column prop="productImage" label="商品图片" width="80">
              <template #default="{ row }">
                <img :src="row.productImage" class="product-img" />
              </template>
            </el-table-column>
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column prop="price" label="单价" width="100">
              <template #default="{ row }">
                ¥{{ row.price }}
              </template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column prop="totalAmount" label="小计" width="100">
              <template #default="{ row }">
                ¥{{ row.totalAmount }}
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="detail-section" v-if="currentOrder.remark">
          <div class="section-title">订单备注</div>
          <div class="remark">{{ currentOrder.remark }}</div>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="deliverVisible" title="订单发货" width="500px">
      <el-form :model="deliverForm" label-width="100px">
        <el-form-item label="订单号">
          <el-input :value="currentOrder?.orderNo" disabled />
        </el-form-item>
        <el-form-item label="物流单号" required>
          <el-input v-model="deliverForm.logisticsNo" placeholder="请输入物流单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deliverVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmDeliver" :loading="deliverLoading">确认发货</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="statusVisible" title="修改订单状态" width="500px">
      <el-form :model="statusForm" label-width="100px">
        <el-form-item label="订单号">
          <el-input :value="currentOrder?.orderNo" disabled />
        </el-form-item>
        <el-form-item label="当前状态">
          <el-tag :type="getStatusType(currentOrder?.status)">
            {{ getStatusText(currentOrder?.status) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="修改为" required>
          <el-select v-model="statusForm.status" placeholder="请选择状态">
            <el-option label="待付款" :value="1" />
            <el-option label="待发货" :value="2" />
            <el-option label="待收货" :value="3" />
            <el-option label="已完成" :value="4" />
            <el-option label="已取消" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmUpdateStatus" :loading="statusLoading">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminOrdersApi, adminDeliverOrderApi, adminUpdateOrderStatusApi } from '@/api/admin'

const loading = ref(false)
const list = ref({})
const searchForm = reactive({
  orderNo: '',
  userId: '',
  status: null
})
const pagination = reactive({
  page: 1,
  size: 10
})

const detailVisible = ref(false)
const currentOrder = ref(null)

const deliverVisible = ref(false)
const deliverForm = reactive({
  logisticsNo: ''
})
const deliverLoading = ref(false)

const statusVisible = ref(false)
const statusForm = reactive({
  status: null
})
const statusLoading = ref(false)

const loadOrders = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    }
    if (params.orderNo) {
      delete params.orderNo
    }
    list.value = await adminOrdersApi(params)
  } catch (error) {
    ElMessage.error('加载订单列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadOrders()
}

const handleReset = () => {
  searchForm.orderNo = ''
  searchForm.userId = ''
  searchForm.status = null
  pagination.page = 1
  loadOrders()
}

const viewDetail = (row) => {
  currentOrder.value = row
  detailVisible.value = true
}

const handleDeliver = (row) => {
  currentOrder.value = row
  deliverForm.logisticsNo = ''
  deliverVisible.value = true
}

const confirmDeliver = async () => {
  if (!deliverForm.logisticsNo) {
    ElMessage.warning('请输入物流单号')
    return
  }

  try {
    deliverLoading.value = true
    await adminDeliverOrderApi(currentOrder.value.id, deliverForm.logisticsNo)
    ElMessage.success('发货成功')
    deliverVisible.value = false
    loadOrders()
  } catch (error) {
    ElMessage.error('发货失败')
  } finally {
    deliverLoading.value = false
  }
}

const handleUpdateStatus = (row) => {
  currentOrder.value = row
  statusForm.status = row.status
  statusVisible.value = true
}

const confirmUpdateStatus = async () => {
  if (statusForm.status === null) {
    ElMessage.warning('请选择订单状态')
    return
  }

  try {
    await ElMessageBox.confirm('确定要修改订单状态吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    statusLoading.value = true
    await adminUpdateOrderStatusApi(currentOrder.value.id, statusForm.status)
    ElMessage.success('修改成功')
    statusVisible.value = false
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('修改失败')
    }
  } finally {
    statusLoading.value = false
  }
}

const getStatusText = (status) => {
  const statusMap = {
    0: '已取消',
    1: '待付款',
    2: '待发货',
    3: '待收货',
    4: '已完成'
  }
  return statusMap[status] || '未知'
}

const getStatusType = (status) => {
  const typeMap = {
    0: 'info',
    1: 'warning',
    2: 'primary',
    3: '',
    4: 'success'
  }
  return typeMap[status] || ''
}

const getPayTypeText = (payType) => {
  const payTypeMap = {
    1: '支付宝',
    2: '微信支付',
    3: '余额支付'
  }
  return payTypeMap[payType] || '-'
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

onMounted(loadOrders)
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

.user-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  color: #2d3436;
  font-weight: 600;
}

.user-phone {
  font-size: 12px;
  color: #636e72;
}

.price {
  color: #ff6b6b;
  font-weight: 700;
  font-size: 15px;
}

.price::before {
  content: '¥';
  font-size: 0.85em;
}

.status-tag {
  display: inline-block;
  padding: 5px 14px;
  border-radius: 25px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.status-tag.status-0 {
  background: linear-gradient(135deg, #f5f5f5 0%, #e0e0e0 100%);
  color: #636e72;
}

.status-tag.status-1 {
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
  color: #e65100;
}

.status-tag.status-2 {
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  color: #1565c0;
}

.status-tag.status-3 {
  background: linear-gradient(135deg, #e0f2f1 0%, #b2dfdb 100%);
  color: #00695c;
}

.status-tag.status-4 {
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  color: #2e7d32;
}

.pay-type {
  color: #636e72;
  font-size: 13px;
}

.no-data {
  color: #b2bec3;
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

.order-detail {
  padding: 10px 0;
}

.detail-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 15px;
  font-weight: 700;
  color: #2d3436;
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 2px solid rgba(255, 107, 107, 0.2);
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.detail-item {
  display: flex;
  align-items: center;
}

.detail-item.full-width {
  grid-column: 1 / -1;
}

.detail-item .label {
  color: #636e72;
  min-width: 80px;
  font-size: 14px;
  font-weight: 500;
}

.detail-item .value {
  color: #2d3436;
  font-size: 14px;
}

.remark {
  padding: 14px 16px;
  background: linear-gradient(135deg, #fff9f0 0%, #fff5e6 100%);
  border-radius: 12px;
  color: #2d3436;
  line-height: 1.8;
  font-size: 14px;
  border-left: 4px solid #ff6b6b;
}

.product-img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 10px;
  border: 2px solid rgba(255, 107, 107, 0.2);
  transition: all 0.3s ease;
}

.product-img:hover {
  transform: scale(1.1);
  border-color: #ff6b6b;
}
</style>
