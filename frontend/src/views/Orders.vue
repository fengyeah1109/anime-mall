<template>
  <div class="orders-page">
    <div class="page-header">
      <h2 class="page-title">我的订单</h2>
    </div>

    <div class="tabs-nav">
      <div 
        class="tab-item" 
        :class="{ active: activeStatus === '' }" 
        @click="changeStatus('')"
      >
        全部订单
      </div>
      <div 
        class="tab-item" 
        :class="{ active: activeStatus === '1' }" 
        @click="changeStatus('1')"
      >
        待付款
      </div>
      <div 
        class="tab-item" 
        :class="{ active: activeStatus === '2' }" 
        @click="changeStatus('2')"
      >
        待发货
      </div>
      <div 
        class="tab-item" 
        :class="{ active: activeStatus === '3' }" 
        @click="changeStatus('3')"
      >
        待收货
      </div>
      <div 
        class="tab-item" 
        :class="{ active: activeStatus === '4' }" 
        @click="changeStatus('4')"
      >
        已完成
      </div>
      <div 
        class="tab-item" 
        :class="{ active: activeStatus === 'after_sale' }" 
        @click="changeStatus('after_sale')"
      >
        售后
      </div>
    </div>

    <div v-if="loading" class="loading-wrap">
      <el-skeleton :rows="5" animated />
    </div>

    <div v-else-if="activeStatus === 'after_sale' && !afterSales.length" class="empty-wrap">
      <div class="empty-icon">📋</div>
      <p class="empty-text">暂无售后申请</p>
      <el-button type="primary" size="large" @click="$router.push('/products')">
        去购物
      </el-button>
    </div>

    <div v-else-if="!orders.length" class="empty-wrap">
      <div class="empty-icon">📋</div>
      <p class="empty-text">暂无相关订单</p>
      <el-button type="primary" size="large" @click="$router.push('/products')">
        去购物
      </el-button>
    </div>

    <div v-else-if="activeStatus === 'after_sale'" class="orders-list">
      <div v-for="item in afterSales" :key="item.id" class="order-card">
        <div class="order-header">
          <div class="header-left">
            <span class="order-no">售后单号: {{ item.id }}</span>
            <span class="order-time">{{ formatTime(item.applyTime) }}</span>
          </div>
          <div class="header-right">
            <el-tag :type="getAfterSaleStatusType(item.status)" size="small">
              {{ getAfterSaleStatusText(item.status) }}
            </el-tag>
          </div>
        </div>

        <div class="order-body">
          <div class="products-list">
            <div class="product-row">
              <img :src="getImageUrl(item.productImage)" class="product-img" />
              <div class="product-info">
                <div class="product-name">{{ item.productName }}</div>
                <div style="margin-top: 8px; font-size: 13px; color: #666;">
                  售后类型: {{ getAfterSaleTypeText(item.type) }}
                </div>
                <div style="font-size: 13px; color: #666;">
                  原因: {{ item.reason }}
                </div>
              </div>
              <div class="product-price">¥{{ item.price }}</div>
              <div class="product-qty">×{{ item.quantity }}</div>
            </div>
          </div>
          <div class="order-summary">
            <div v-if="item.reply" class="summary-row total">
              <span class="label">管理员回复</span>
              <span class="value">{{ item.reply }}</span>
            </div>
          </div>
        </div>

        <div class="order-footer">
          <div class="footer-left">
            <span class="pay-type">订单号: {{ item.orderNo }}</span>
          </div>
          <div class="footer-right">
            <el-button size="small" @click="handleAfterSaleDetail(item)">
              查看详情
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="orders-list">
      <div v-for="order in orders" :key="order.id" class="order-card">
        <div class="order-header">
          <div class="header-left">
            <span class="order-no">{{ order.orderNo }}</span>
            <span class="order-time">{{ formatTime(order.createTime) }}</span>
          </div>
          <div class="header-right">
            <span class="status-text" :class="'status-' + order.status">
              {{ getStatusText(order.status) }}
            </span>
          </div>
        </div>

        <div class="order-body">
          <div class="products-list">
            <div v-for="item in order.items" :key="item.id" class="product-row">
              <img :src="getImageUrl(item.productImage)" class="product-img" />
              <div class="product-info">
                <div class="product-name">{{ item.productName }}</div>
                <div class="product-spec">{{ item.spec || '默认规格' }}</div>
              </div>
              <div class="product-price">¥{{ item.price }}</div>
              <div class="product-qty">×{{ item.quantity }}</div>
            </div>
          </div>
          <div class="order-summary">
            <div class="summary-row">
              <span class="label">商品总额</span>
              <span class="value">¥{{ order.totalAmount }}</span>
            </div>
            <div class="summary-row">
              <span class="label">运费</span>
              <span class="value">¥{{ order.freightAmount }}</span>
            </div>
            <div class="summary-row total">
              <span class="label">实付金额</span>
              <span class="value">¥{{ order.payAmount }}</span>
            </div>
          </div>
        </div>

        <div class="order-footer">
          <div class="footer-left">
            <span class="pay-type" v-if="order.payType">{{ getPayTypeText(order.payType) }}</span>
          </div>
          <div class="footer-right">
            <el-button v-if="order.status === 1" type="primary" size="small" @click="handlePay(order)">
              立即支付
            </el-button>
            <el-button v-if="order.status === 1" size="small" @click="handleCancel(order)">
              取消订单
            </el-button>
            <el-button v-if="order.status === 3" type="success" size="small" @click="handleReceive(order)">
              确认收货
            </el-button>
            <el-button v-if="order.status === 4" size="small" @click="handleAfterSale(order)">
              申请售后
            </el-button>
            <el-button v-if="order.status === 4" type="primary" size="small" @click="handleReview(order)">
              商品评价
            </el-button>
            <el-button size="small" @click="handleDetail(order)">
              查看详情
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <div class="pagination-wrap" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <el-dialog v-model="detailDialogVisible" title="订单详情" width="700px" class="order-dialog">
      <div v-if="currentOrder" class="detail-content">
        <div class="detail-block">
          <div class="block-title">订单信息</div>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">订单号</span>
              <span class="value">{{ currentOrder.orderNo }}</span>
            </div>
            <div class="info-item">
              <span class="label">订单状态</span>
              <span class="value status" :class="'status-' + currentOrder.status">
                {{ getStatusText(currentOrder.status) }}
              </span>
            </div>
            <div class="info-item">
              <span class="label">下单时间</span>
              <span class="value">{{ formatTime(currentOrder.createTime) }}</span>
            </div>
            <div class="info-item" v-if="currentOrder.paymentTime">
              <span class="label">支付时间</span>
              <span class="value">{{ formatTime(currentOrder.paymentTime) }}</span>
            </div>
            <div class="info-item" v-if="currentOrder.deliveryTime">
              <span class="label">发货时间</span>
              <span class="value">{{ formatTime(currentOrder.deliveryTime) }}</span>
            </div>
            <div class="info-item" v-if="currentOrder.receiveTime">
              <span class="label">收货时间</span>
              <span class="value">{{ formatTime(currentOrder.receiveTime) }}</span>
            </div>
          </div>
        </div>

        <div class="detail-block">
          <div class="block-title">商品清单</div>
          <div class="detail-products">
            <div v-for="item in currentOrder.items" :key="item.id" class="detail-product">
              <img :src="getImageUrl(item.productImage)" class="detail-img" />
              <div class="detail-info">
                <div class="name">{{ item.productName }}</div>
                <div class="spec">{{ item.spec || '默认规格' }}</div>
              </div>
              <div class="detail-price">¥{{ item.price }}</div>
              <div class="detail-qty">×{{ item.quantity }}</div>
              <div class="detail-total">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
            </div>
          </div>
        </div>

        <div class="detail-block">
          <div class="block-title">支付信息</div>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">支付方式</span>
              <span class="value">{{ getPayTypeText(currentOrder.payType) }}</span>
            </div>
            <div class="info-item">
              <span class="label">商品总额</span>
              <span class="value">¥{{ currentOrder.totalAmount }}</span>
            </div>
            <div class="info-item">
              <span class="label">运费</span>
              <span class="value">¥{{ currentOrder.freightAmount }}</span>
            </div>
            <div class="info-item total">
              <span class="label">实付金额</span>
              <span class="value">¥{{ currentOrder.payAmount }}</span>
            </div>
          </div>
        </div>

        <div class="detail-block" v-if="currentOrder.remark">
          <div class="block-title">订单备注</div>
          <div class="remark-content">{{ currentOrder.remark }}</div>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="afterSaleDialogVisible" title="申请售后" width="550px" class="order-dialog">
      <el-form :model="afterSaleForm" :rules="afterSaleRules" ref="afterSaleFormRef" label-width="100px">
        <el-form-item label="订单号">
          <el-input v-model="afterSaleForm.orderNo" disabled />
        </el-form-item>
        <el-form-item label="商品" prop="orderItemId">
          <el-select v-model="afterSaleForm.orderItemId" placeholder="请选择商品" style="width: 100%">
            <el-option
              v-for="item in currentOrder?.items"
              :key="item.id"
              :label="item.productName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="售后类型" prop="type">
          <el-select v-model="afterSaleForm.type" placeholder="请选择售后类型" style="width: 100%">
            <el-option label="退货退款" value="1" />
            <el-option label="仅退款" value="2" />
            <el-option label="换货" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="售后原因" prop="reason">
          <el-input v-model="afterSaleForm.reason" type="textarea" :rows="3" placeholder="请输入售后原因" />
        </el-form-item>
        <el-form-item label="问题描述">
          <el-input v-model="afterSaleForm.description" type="textarea" :rows="3" placeholder="请详细描述问题" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="afterSaleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAfterSale" :loading="afterSaleLoading">提交申请</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="afterSaleDetailVisible" title="售后详情" width="600px" class="order-dialog">
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
            <el-tag :type="getAfterSaleStatusType(currentAfterSale.status)">{{ getAfterSaleStatusText(currentAfterSale.status) }}</el-tag>
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
      <template #footer>
        <el-button @click="afterSaleDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="payDialogVisible" title="订单支付" width="450px" class="order-dialog">
      <div class="pay-dialog-content">
        <div class="pay-amount">
          <span class="label">支付金额</span>
          <span class="amount">¥{{ currentOrder?.payAmount }}</span>
        </div>
        <div class="pay-methods">
          <div class="method-title">选择支付方式</div>
          <div class="method-list">
            <div 
              class="method-item" 
              :class="{ active: payForm.payType === 1 }" 
              @click="payForm.payType = 1"
            >
              <span class="method-icon alipay">支付宝</span>
            </div>
            <div 
              class="method-item" 
              :class="{ active: payForm.payType === 2 }" 
              @click="payForm.payType = 2"
            >
              <span class="method-icon wechat">微信支付</span>
            </div>
            <div 
              class="method-item" 
              :class="{ active: payForm.payType === 3 }" 
              @click="payForm.payType = 3"
            >
              <span class="method-icon balance">余额支付</span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="payDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmPay" :loading="confirmPayLoading">确认支付</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reviewDialogVisible" title="评价商品" width="500px" class="order-dialog">
      <el-form :model="reviewForm" :rules="reviewRules" ref="reviewFormRef" label-width="100px">
        <el-form-item label="选择商品" prop="orderItemId">
          <el-select v-model="reviewForm.orderItemId" placeholder="请选择要评价的商品" style="width: 100%">
            <el-option 
              v-for="item in currentOrder?.items" 
              :key="item.id" 
              :label="item.productName + (item.spec ? ' - ' + item.spec : '')" 
              :value="item.id"
              @click="selectProduct(item)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="商品信息" v-if="reviewForm.productName">
          <div class="review-product-info">
            <img :src="getImageUrl(reviewForm.productImage)" class="review-product-img" />
            <div class="review-product-details">
              <div class="review-product-name">{{ reviewForm.productName }}</div>
              <div class="review-product-price">¥{{ reviewForm.price }}</div>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="评分" prop="rating">
          <div class="rating-stars">
            <el-rate v-model="reviewForm.rating" :max="5" show-score text-color="#ff9900" />
          </div>
        </el-form-item>
        <el-form-item label="评价内容" prop="content">
          <el-input v-model="reviewForm.content" type="textarea" :rows="4" placeholder="请输入评价内容" />
        </el-form-item>
        <el-form-item label="上传图片">
          <el-upload
            class="review-uploader"
            action=""
            :auto-upload="false"
            :on-change="handleImageChange"
            :file-list="reviewForm.images"
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
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview" :loading="reviewLoading">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrdersApi, payOrderApi, cancelOrderApi, receiveOrderApi } from '@/api/order'
import { createAfterSaleApi, getAfterSalesApi } from '@/api/user'
import { createReviewApi } from '@/api/reviews'
import { Plus } from '@element-plus/icons-vue'
import { getImageUrl } from '@/utils/image'

const router = useRouter()
const activeStatus = ref('')
const orders = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const detailDialogVisible = ref(false)
const afterSaleDialogVisible = ref(false)
const payDialogVisible = ref(false)
const currentOrder = ref(null)
const afterSaleLoading = ref(false)
const confirmPayLoading = ref(false)
const afterSaleFormRef = ref()
const afterSales = ref([])
const afterSaleDetailVisible = ref(false)
const currentAfterSale = ref(null)

const afterSaleForm = ref({
  orderNo: '',
  orderItemId: null,
  type: '1',
  reason: '',
  description: ''
})

const afterSaleRules = {
  orderItemId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  type: [{ required: true, message: '请选择售后类型', trigger: 'change' }],
  reason: [
    { required: true, message: '请输入售后原因', trigger: 'blur' }
  ]
}

const payForm = ref({ payType: 1 })

// 评价相关
const reviewDialogVisible = ref(false)
const reviewFormRef = ref()
const reviewLoading = ref(false)
const reviewForm = ref({
  orderItemId: null,
  productName: '',
  productImage: '',
  price: '',
  rating: 5,
  content: '',
  images: []
})

const reviewRules = {
  rating: [{ required: true, message: '请选择评分', trigger: 'blur' }],
  content: [
    { required: true, message: '请输入评价内容', trigger: 'blur' },
    { min: 10, message: '评价内容至少10个字符', trigger: 'blur' }
  ]
}

const getStatusText = (status) => {
  const map = { 0: '已取消', 1: '待付款', 2: '待发货', 3: '待收货', 4: '已完成' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'primary', 3: 'success', 4: 'success' }
  return map[status] || 'info'
}

const getPayTypeText = (payType) => {
  const map = { 1: '支付宝', 2: '微信支付', 3: '余额支付' }
  return map[payType] || '未选择'
}

const getAfterSaleStatusText = (status) => {
  const map = { 0: '待审核', 1: '审核通过', 2: '审核拒绝', 3: '已完成' }
  return map[status] || '未知'
}

const getAfterSaleTypeText = (type) => {
  const map = { 1: '退货退款', 2: '仅退款', 3: '换货' }
  return map[type] || '未知'
}

const getAfterSaleStatusType = (status) => {
  const map = { 0: 'warning', 1: 'primary', 2: 'danger', 3: 'success' }
  return map[status] || 'info'
}

const handleAfterSaleDetail = (item) => {
  currentAfterSale.value = item
  afterSaleDetailVisible.value = true
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const loadOrders = async () => {
  loading.value = true
  try {
    const result = await getOrdersApi({
      status: activeStatus.value === 'after_sale' ? undefined : activeStatus.value || undefined,
      page: currentPage.value,
      size: pageSize.value
    })
    orders.value = result.records || []
    total.value = result.total || 0
  } catch (error) {
    ElMessage.error('加载订单失败')
  } finally {
    loading.value = false
  }
}

const loadAfterSales = async () => {
  loading.value = true
  try {
    const result = await getAfterSalesApi()
    afterSales.value = result || []
    total.value = afterSales.value.length
  } catch (error) {
    ElMessage.error('加载售后申请失败')
  } finally {
    loading.value = false
  }
}

const changeStatus = (status) => {
  activeStatus.value = status
  currentPage.value = 1
  if (status === 'after_sale') {
    loadAfterSales()
  } else {
    loadOrders()
  }
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadOrders()
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadOrders()
}

const handlePay = (order) => {
  currentOrder.value = order
  payForm.value.payType = order.payType || 1
  payDialogVisible.value = true
}

const confirmPay = async () => {
  try {
    await ElMessageBox.confirm('确认支付该订单？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    confirmPayLoading.value = true
    await payOrderApi(currentOrder.value.id)
    ElMessage.success('支付成功')
    payDialogVisible.value = false
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('支付失败')
    }
  } finally {
    confirmPayLoading.value = false
  }
}

const handleCancel = async (order) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cancelOrderApi(order.id)
    ElMessage.success('订单已取消')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败')
    }
  }
}

const handleReceive = async (order) => {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await receiveOrderApi(order.id)
    ElMessage.success('确认收货成功')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleDetail = (order) => {
  currentOrder.value = order
  detailDialogVisible.value = true
}

const handleAfterSale = (order) => {
  currentOrder.value = order
  afterSaleForm.value = {
    orderNo: order.orderNo,
    orderItemId: null,
    type: '1',
    reason: '',
    description: ''
  }
  afterSaleDialogVisible.value = true
}

const submitAfterSale = async () => {
  try {
    await afterSaleFormRef.value.validate()
    afterSaleLoading.value = true
    const data = {
      orderItemId: afterSaleForm.value.orderItemId,
      type: parseInt(afterSaleForm.value.type),
      reason: afterSaleForm.value.reason,
      description: afterSaleForm.value.description
    }
    await createAfterSaleApi(data)
    ElMessage.success('售后申请已提交')
    afterSaleDialogVisible.value = false
    // 刷新数据
    if (activeStatus.value === 'after_sale') {
      loadAfterSales()
    } else {
      loadOrders()
    }
  } catch (error) {
    if (error !== false) {
      ElMessage.error('提交失败')
    }
  } finally {
    afterSaleLoading.value = false
  }
}

// 评价相关方法
const handleImageChange = (file, fileList) => {
  // 确保文件列表中的每个文件都有 url 属性
  const processedFileList = fileList.map(f => ({
    ...f,
    url: f.url || f.raw?.preview || ''
  }))
  reviewForm.value.images = processedFileList
}

const handleReview = (order) => {
  currentOrder.value = order
  // 显示评价对话框，让用户选择要评价的商品
  reviewDialogVisible.value = true
  // 初始化评价表单
  reviewForm.value = {
    orderItemId: null,
    productName: '',
    productImage: '',
    price: '',
    rating: 5,
    content: '',
    images: []
  }
}

const selectProduct = (item) => {
  reviewForm.value = {
    ...reviewForm.value,
    productName: item.productName,
    productImage: item.productImage,
    price: item.price
  }
}

const submitReview = async () => {
  try {
    await reviewFormRef.value.validate()
    
    // 准备图片数据
    const images = reviewForm.value.images.map(file => file.raw).filter(Boolean)
    
    reviewLoading.value = true
    await createReviewApi({
      orderItemId: reviewForm.value.orderItemId,
      rating: reviewForm.value.rating,
      content: reviewForm.value.content,
      images: images
    })
    ElMessage.success('评价成功')
    reviewDialogVisible.value = false
    loadOrders()
  } catch (error) {
    if (error !== false) {
      ElMessage.error('评价失败')
    }
  } finally {
    reviewLoading.value = false
  }
}

onMounted(loadOrders)
</script>

<style scoped>
.orders-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.tabs-nav {
  display: flex;
  background: #fff;
  border-radius: 8px;
  padding: 0 20px;
  margin-bottom: 20px;
  border-bottom: 2px solid #f0f0f0;
}

.tab-item {
  padding: 15px 25px;
  font-size: 15px;
  color: #666;
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
}

.tab-item:hover {
  color: #ff4400;
}

.tab-item.active {
  color: #ff4400;
  font-weight: 600;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  right: 0;
  height: 2px;
  background: #ff4400;
}

.loading-wrap {
  background: #fff;
  padding: 40px;
  border-radius: 8px;
}

.empty-wrap {
  background: #fff;
  padding: 80px 20px;
  text-align: center;
  border-radius: 8px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.empty-text {
  color: #999;
  font-size: 16px;
  margin-bottom: 30px;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.order-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #f5f5f5;
  border-bottom: 1px solid #eee;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.order-no {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.order-time {
  font-size: 13px;
  color: #999;
}

.status-text {
  font-size: 14px;
  font-weight: 600;
}

.status-text.status-0 { color: #999; }
.status-text.status-1 { color: #ff4400; }
.status-text.status-2 { color: #409eff; }
.status-text.status-3 { color: #67c23a; }
.status-text.status-4 { color: #67c23a; }

.order-body {
  display: flex;
  padding: 20px;
}

.products-list {
  flex: 1;
}

.product-row {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
}

.product-row:last-child {
  border-bottom: none;
}

.product-img {
  width: 70px;
  height: 70px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #eee;
}

.product-info {
  flex: 1;
  margin-left: 15px;
}

.product-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 5px;
  line-height: 1.4;
}

.product-spec {
  font-size: 12px;
  color: #999;
}

.product-price {
  width: 80px;
  text-align: center;
  font-size: 14px;
  color: #666;
}

.product-qty {
  width: 50px;
  text-align: center;
  font-size: 14px;
  color: #999;
}

.order-summary {
  width: 180px;
  padding-left: 20px;
  border-left: 1px solid #f0f0f0;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 13px;
}

.summary-row .label {
  color: #999;
}

.summary-row .value {
  color: #333;
}

.summary-row.total {
  margin-top: 15px;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
}

.summary-row.total .label {
  color: #333;
  font-weight: 500;
}

.summary-row.total .value {
  color: #ff4400;
  font-size: 18px;
  font-weight: 600;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #fafafa;
  border-top: 1px solid #eee;
}

.footer-left {
  font-size: 13px;
  color: #999;
}

.footer-right {
  display: flex;
  gap: 10px;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
}

.detail-content {
  padding: 10px 0;
}

.detail-block {
  margin-bottom: 25px;
}

.detail-block:last-child {
  margin-bottom: 0;
}

.block-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px 30px;
}

.info-item {
  display: flex;
  font-size: 14px;
}

.info-item .label {
  color: #999;
  width: 80px;
  flex-shrink: 0;
}

.info-item .value {
  color: #333;
}

.info-item .value.status {
  font-weight: 600;
}

.info-item.total .value {
  color: #ff4400;
  font-size: 18px;
  font-weight: 600;
}

.detail-products {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
}

.detail-product {
  display: flex;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-product:last-child {
  border-bottom: none;
}

.detail-img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.detail-info {
  flex: 1;
  margin-left: 15px;
}

.detail-info .name {
  font-size: 14px;
  color: #333;
  margin-bottom: 5px;
}

.detail-info .spec {
  font-size: 12px;
  color: #999;
}

.detail-price {
  width: 80px;
  text-align: center;
  font-size: 14px;
  color: #666;
}

.detail-qty {
  width: 50px;
  text-align: center;
  font-size: 14px;
  color: #999;
}

.detail-total {
  width: 100px;
  text-align: right;
  font-size: 14px;
  color: #ff4400;
  font-weight: 600;
}

.remark-content {
  padding: 15px;
  background: #f5f5f5;
  border-radius: 4px;
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

.pay-dialog-content {
  padding: 10px 0;
}

.pay-amount {
  text-align: center;
  padding: 20px 0;
  border-bottom: 1px solid #eee;
  margin-bottom: 20px;
}

.pay-amount .label {
  font-size: 14px;
  color: #666;
  display: block;
  margin-bottom: 10px;
}

.pay-amount .amount {
  font-size: 32px;
  color: #ff4400;
  font-weight: 600;
}

.pay-methods {
  padding: 0 10px;
}

.method-title {
  font-size: 14px;
  color: #333;
  margin-bottom: 15px;
}

.method-list {
  display: flex;
  gap: 15px;
}

.method-item {
  flex: 1;
  padding: 15px;
  border: 2px solid #eee;
  border-radius: 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
}

.method-item:hover {
  border-color: #ff4400;
}

.method-item.active {
  border-color: #ff4400;
  background: #fff5f0;
}

.method-icon {
  font-size: 14px;
  color: #333;
}

/* 评价相关样式 */
.review-product-info {
  display: flex;
  align-items: center;
  padding: 10px;
  background: #f5f5f5;
  border-radius: 8px;
}

.review-product-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  margin-right: 15px;
}

.review-product-details {
  flex: 1;
}

.review-product-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
  line-height: 1.4;
}

.review-product-price {
  font-size: 16px;
  color: #ff4400;
  font-weight: 600;
}

.rating-stars {
  padding: 10px 0;
}

.review-uploader {
  margin-top: 10px;
}

.uploaded-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-actions {
  margin-left: 20px;
}
</style>
