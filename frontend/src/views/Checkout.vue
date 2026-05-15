<template>
  <div class="checkout-page">
    <div class="page-header">
      <h2 class="page-title">确认订单</h2>
    </div>

    <div class="checkout-section">
      <div class="section-title">
        <span class="title-text">收货地址</span>
        <el-button type="primary" link @click="goToAddress">管理地址</el-button>
      </div>
      <div class="address-content">
        <div v-if="!addresses.length" class="no-address">
          <p>您还没有添加收货地址</p>
          <el-button type="primary" @click="goToAddress">添加地址</el-button>
        </div>
        <div v-else class="address-list">
          <div 
            v-for="address in addresses" 
            :key="address.id" 
            class="address-item"
            :class="{ active: form.addressId === address.id }"
            @click="form.addressId = address.id"
          >
            <div class="address-info">
              <div class="address-header">
                <span class="receiver-name">{{ address.receiverName }}</span>
                <span class="receiver-phone">{{ address.receiverPhone }}</span>
                <span v-if="address.isDefault === 1" class="default-tag">默认</span>
              </div>
              <div class="address-detail">
                {{ address.province }} {{ address.city }} {{ address.district }} {{ address.detailAddress }}
              </div>
            </div>
            <div class="address-check" v-if="form.addressId === address.id">
              <el-icon color="#ff4400"><Check /></el-icon>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="checkout-section">
      <div class="section-title">
        <span class="title-text">商品清单</span>
      </div>
      <div class="products-content">
        <div class="products-header">
          <span class="col-product">商品信息</span>
          <span class="col-price">单价</span>
          <span class="col-qty">数量</span>
          <span class="col-total">小计</span>
        </div>
        <div class="products-list">
          <div v-for="item in cartItems" :key="item.id" class="product-row">
            <div class="col-product">
              <img :src="item.imageUrl" class="product-img" />
              <div class="product-info">
                <div class="product-name">{{ item.name }}</div>
                <div class="product-spec">{{ item.spec || '默认规格' }}</div>
              </div>
            </div>
            <div class="col-price">¥{{ item.price || 0 }}</div>
            <div class="col-qty">×{{ item.quantity }}</div>
            <div class="col-total">¥{{ ((item.price || 0) * item.quantity).toFixed(2) }}</div>
          </div>
        </div>
      </div>
    </div>

    <div class="checkout-section">
      <div class="section-title">
        <span class="title-text">支付方式</span>
      </div>
      <div class="payment-content">
        <div class="payment-list">
          <div 
            class="payment-item"
            :class="{ active: form.payType === 1 }"
            @click="form.payType = 1"
          >
            <span class="payment-name">支付宝</span>
            <div class="payment-check" v-if="form.payType === 1">
              <el-icon color="#ff4400"><Check /></el-icon>
            </div>
          </div>
          <div 
            class="payment-item"
            :class="{ active: form.payType === 2 }"
            @click="form.payType = 2"
          >
            <span class="payment-name">微信支付</span>
            <div class="payment-check" v-if="form.payType === 2">
              <el-icon color="#ff4400"><Check /></el-icon>
            </div>
          </div>
          <div 
            class="payment-item"
            :class="{ active: form.payType === 3 }"
            @click="form.payType = 3"
          >
            <span class="payment-name">余额支付</span>
            <div class="payment-check" v-if="form.payType === 3">
              <el-icon color="#ff4400"><Check /></el-icon>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="checkout-section">
      <div class="section-title">
        <span class="title-text">订单备注</span>
      </div>
      <div class="remark-content">
        <el-input 
          v-model="form.remark" 
          type="textarea" 
          :rows="3" 
          placeholder="选填，可输入订单备注信息"
          maxlength="200"
          show-word-limit
        />
      </div>
    </div>

    <div class="checkout-footer">
      <div class="footer-content">
        <div class="price-summary">
          <div class="summary-row">
            <span class="label">商品总额</span>
            <span class="value">¥{{ totalPrice.toFixed(2) }}</span>
          </div>
          <div class="summary-row">
            <span class="label">运费</span>
            <span class="value">¥{{ freight.toFixed(2) }}</span>
          </div>
          <div class="summary-row total">
            <span class="label">应付金额</span>
            <span class="value">¥{{ payAmount.toFixed(2) }}</span>
          </div>
        </div>
        <div class="submit-area">
          <el-button type="primary" size="large" class="submit-btn" @click="submit" :loading="submitLoading">
            提交订单
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import { createOrderApi } from '@/api/order'
import { getAddressesApi } from '@/api/user'
import { getCartApi } from '@/api/cart'

const router = useRouter()
const route = useRoute()
const addresses = ref([])
const cartItems = ref([])
const submitLoading = ref(false)
const form = ref({
  addressId: null,
  payType: 1,
  remark: ''
})

const selectedCartIds = ref([])

const totalPrice = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + (item.price || 0) * item.quantity, 0)
})

const freight = computed(() => {
  return totalPrice.value >= 99 ? 0 : 10
})

const payAmount = computed(() => {
  return totalPrice.value + freight.value
})

const loadAddresses = async () => {
  addresses.value = await getAddressesApi()
  if (addresses.value.length > 0) {
    const defaultAddr = addresses.value.find(a => a.isDefault === 1)
    form.value.addressId = defaultAddr ? defaultAddr.id : addresses.value[0].id
  }
}

const loadCart = async () => {
  const allItems = await getCartApi()
  // 获取URL中的选中购物车ID
  const cartIds = route.query.cartIds
  if (cartIds) {
    selectedCartIds.value = cartIds.split(',').map(id => parseInt(id))
    // 只保留选中的商品
    cartItems.value = allItems.filter(item => selectedCartIds.value.includes(item.id))
  } else {
    // 如果没有传参，显示全部商品（兼容直接访问）
    cartItems.value = allItems
  }
}

const goToAddress = () => {
  router.push('/profile')
}

const submit = async () => {
  if (!form.value.addressId) {
    ElMessage.warning('请选择收货地址')
    return
  }
  
  if (cartItems.value.length === 0) {
    ElMessage.warning('购物车为空')
    return
  }

  try {
    await ElMessageBox.confirm('确认提交订单？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    submitLoading.value = true
    const order = await createOrderApi(form.value)
    ElMessage.success('下单成功')
    router.push(`/payment/${order.id}`)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('下单失败')
    }
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  loadAddresses()
  loadCart()
})
</script>

<style scoped>
.checkout-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
  padding-bottom: 120px;
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

.checkout-section {
  background: #fff;
  border-radius: 8px;
  margin-bottom: 15px;
  overflow: hidden;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.title-text {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.address-content {
  padding: 20px;
}

.no-address {
  text-align: center;
  padding: 30px 0;
  color: #999;
}

.no-address p {
  margin-bottom: 15px;
}

.address-list {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.address-item {
  width: calc(50% - 8px);
  padding: 15px;
  border: 2px solid #eee;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.address-item:hover {
  border-color: #ff4400;
}

.address-item.active {
  border-color: #ff4400;
  background: #fff9f5;
}

.address-header {
  margin-bottom: 10px;
}

.receiver-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-right: 15px;
}

.receiver-phone {
  font-size: 14px;
  color: #666;
}

.default-tag {
  display: inline-block;
  margin-left: 10px;
  padding: 2px 8px;
  background: #ff4400;
  color: #fff;
  font-size: 12px;
  border-radius: 2px;
}

.address-detail {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

.address-check {
  position: absolute;
  right: 15px;
  top: 50%;
  transform: translateY(-50%);
}

.products-content {
  padding: 0 20px 20px;
}

.products-header {
  display: flex;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
  color: #666;
}

.col-product {
  flex: 1;
}

.col-price, .col-qty, .col-total {
  width: 100px;
  text-align: center;
}

.products-list {
  padding: 15px 0;
}

.product-row {
  display: flex;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #f5f5f5;
}

.product-row:last-child {
  border-bottom: none;
}

.product-row .col-product {
  display: flex;
  align-items: center;
}

.product-img {
  width: 70px;
  height: 70px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #eee;
}

.product-info {
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

.product-row .col-price,
.product-row .col-qty {
  font-size: 14px;
  color: #666;
}

.product-row .col-total {
  font-size: 14px;
  color: #ff4400;
  font-weight: 600;
}

.payment-content {
  padding: 20px;
}

.payment-list {
  display: flex;
  gap: 15px;
}

.payment-item {
  width: 150px;
  padding: 15px;
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

.payment-name {
  font-size: 14px;
  color: #333;
}

.payment-check {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
}

.remark-content {
  padding: 20px;
}

.checkout-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  border-top: 2px solid #ff4400;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
  z-index: 100;
}

.footer-content {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 40px;
}

.price-summary {
  text-align: right;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
  min-width: 200px;
}

.summary-row .label {
  color: #666;
}

.summary-row .value {
  color: #333;
}

.summary-row.total {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #eee;
}

.summary-row.total .label {
  font-weight: 600;
  color: #333;
}

.summary-row.total .value {
  font-size: 24px;
  font-weight: 600;
  color: #ff4400;
}

.submit-btn {
  width: 160px;
  height: 48px;
  font-size: 18px;
}
</style>
