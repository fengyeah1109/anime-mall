<template>
  <div class="payment-page">
    <div class="payment-card">
      <div class="payment-header">
        <h2 class="payment-title">订单支付</h2>
      </div>

      <div class="order-info">
        <div class="info-row">
          <span class="label">订单编号</span>
          <span class="value">{{ orderId }}</span>
        </div>
        <div class="info-row">
          <span class="label">下单时间</span>
          <span class="value">{{ orderTime }}</span>
        </div>
        <div class="info-row total">
          <span class="label">订单金额</span>
          <span class="value">¥{{ payAmount.toFixed(2) }}</span>
        </div>
      </div>

      <div class="payment-section">
        <div class="section-title">选择支付方式</div>
        <div class="payment-list">
          <div 
            class="payment-item" 
            :class="{ active: payType === 1 }" 
            @click="payType = 1"
          >
            <div class="payment-icon alipay">支付宝</div>
            <div class="payment-check" v-if="payType === 1">
              <el-icon color="#ff4400"><Check /></el-icon>
            </div>
          </div>
          <div 
            class="payment-item" 
            :class="{ active: payType === 2 }" 
            @click="payType = 2"
          >
            <div class="payment-icon wechat">微信支付</div>
            <div class="payment-check" v-if="payType === 2">
              <el-icon color="#ff4400"><Check /></el-icon>
            </div>
          </div>
          <div 
            class="payment-item" 
            :class="{ active: payType === 3 }" 
            @click="payType = 3"
          >
            <div class="payment-icon balance">余额支付</div>
            <div class="payment-check" v-if="payType === 3">
              <el-icon color="#ff4400"><Check /></el-icon>
            </div>
          </div>
        </div>
      </div>

      <div class="payment-footer">
        <div class="countdown-wrap" v-if="countdown > 0">
          <span class="countdown-label">支付剩余时间</span>
          <span class="countdown-time">{{ formatCountdown(countdown) }}</span>
        </div>
        <div class="action-area">
          <el-button size="large" @click="cancelOrder" :loading="cancelLoading">
            取消订单
          </el-button>
          <el-button 
            type="primary" 
            size="large" 
            @click="pay" 
            :loading="payLoading"
            class="pay-btn"
          >
            立即支付 ¥{{ payAmount.toFixed(2) }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import { payOrderApi, getOrderDetailApi } from '@/api/order'

const router = useRouter()
const route = useRoute()

const orderId = ref(route.params.id)
const payAmount = ref(0)
const orderTime = ref('')
const payType = ref(1)
const payLoading = ref(false)
const cancelLoading = ref(false)
const countdown = ref(1800)
let countdownTimer = null

const loadOrderDetail = async () => {
  try {
    const order = await getOrderDetailApi(orderId.value)
    payAmount.value = order.totalAmount || order.payAmount || 0
    orderTime.value = order.createTime
    payType.value = order.payType || 1
  } catch (error) {
    ElMessage.error('加载订单信息失败')
  }
}

const pay = async () => {
  try {
    await ElMessageBox.confirm('确认支付该订单？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    payLoading.value = true
    await payOrderApi(orderId.value)
    ElMessage.success('支付成功')
    router.push('/orders')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('支付失败')
    }
  } finally {
    payLoading.value = false
  }
}

const cancelOrder = async () => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    cancelLoading.value = true
    ElMessage.success('订单已取消')
    router.push('/orders')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消订单失败')
    }
  } finally {
    cancelLoading.value = false
  }
}

const formatCountdown = (seconds) => {
  const minutes = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

const startCountdown = () => {
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(countdownTimer)
      ElMessage.warning('订单已超时，请重新下单')
      router.push('/orders')
    }
  }, 1000)
}

onMounted(() => {
  loadOrderDetail()
  startCountdown()
})

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
</script>

<style scoped>
.payment-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.payment-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.payment-header {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.payment-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.order-info {
  padding: 25px 20px;
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 14px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-row .label {
  color: #999;
}

.info-row .value {
  color: #333;
}

.info-row.total {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.info-row.total .label {
  font-weight: 600;
  color: #333;
}

.info-row.total .value {
  font-size: 24px;
  font-weight: 600;
  color: #ff4400;
}

.payment-section {
  padding: 25px 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
}

.payment-list {
  display: flex;
  gap: 15px;
}

.payment-item {
  flex: 1;
  padding: 20px;
  border: 2px solid #eee;
  border-radius: 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.payment-item:hover {
  border-color: #ff4400;
}

.payment-item.active {
  border-color: #ff4400;
  background: #fff9f5;
}

.payment-icon {
  font-size: 15px;
  color: #333;
  font-weight: 500;
}

.payment-check {
  position: absolute;
  right: 15px;
  top: 50%;
  transform: translateY(-50%);
}

.payment-footer {
  padding: 20px;
  background: #fafafa;
  border-top: 1px solid #f0f0f0;
}

.countdown-wrap {
  text-align: center;
  margin-bottom: 20px;
}

.countdown-label {
  font-size: 14px;
  color: #666;
  margin-right: 10px;
}

.countdown-time {
  font-size: 20px;
  font-weight: 600;
  color: #ff4400;
}

.action-area {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.pay-btn {
  width: 200px;
}
</style>
