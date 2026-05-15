<template>
  <div class="cart-page">
    <div class="page-header">
      <h2 class="page-title">我的购物车</h2>
    </div>
    
    <div v-if="loading" class="loading-wrap">
      <el-skeleton :rows="5" animated />
    </div>
    
    <div v-else-if="!list.length" class="empty-wrap">
      <div class="empty-icon">🛒</div>
      <p class="empty-text">购物车还是空的，快去挑选心仪的商品吧</p>
      <el-button type="primary" size="large" @click="$router.push('/products')">
        去购物
      </el-button>
    </div>
    
    <div v-else class="cart-content">
      <div class="cart-header">
        <div class="col-check">
          <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
        </div>
        <div class="col-product">商品信息</div>
        <div class="col-price">单价</div>
        <div class="col-quantity">数量</div>
        <div class="col-total">小计</div>
        <div class="col-action">操作</div>
      </div>
      
      <div class="cart-list">
        <div v-for="item in list" :key="item.id" class="cart-item">
          <div class="col-check">
            <el-checkbox v-model="item.selected" @change="handleSelectedChange(item)" />
          </div>
          <div class="col-product">
            <div class="product-info" @click="$router.push(`/products/${item.productId}`)">
              <img :src="item.imageUrl" class="product-img" />
              <div class="product-name">{{ item.name }}</div>
            </div>
          </div>
          <div class="col-price">
            <span class="price">¥{{ item.price || 0 }}</span>
          </div>
          <div class="col-quantity">
            <div class="quantity-box">
              <button class="qty-btn" @click="item.quantity > 1 && updateQty(item, -1)">-</button>
              <input type="text" class="qty-input" :value="item.quantity" @change="updateQtyInput(item, $event)" />
              <button class="qty-btn" @click="updateQty(item, 1)">+</button>
            </div>
          </div>
          <div class="col-total">
            <span class="total-price">¥{{ item.totalPrice || 0 }}</span>
          </div>
          <div class="col-action">
            <span class="action-btn" @click="remove(item.id)">删除</span>
          </div>
        </div>
      </div>
      
      <div class="cart-footer">
        <div class="footer-left">
          <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
          <span class="action-link" @click="clear">清空购物车</span>
        </div>
        <div class="footer-right">
          <div class="summary">
            <span class="selected-count">已选择 <em>{{ selectedCount }}</em> 件商品</span>
            <div class="total-box">
              <span class="total-label">合计：</span>
              <span class="total-amount">¥{{ totalPrice.toFixed(2) }}</span>
            </div>
          </div>
          <el-button 
            type="primary" 
            size="large" 
            class="checkout-btn"
            @click="goToCheckout"
            :disabled="selectedCount === 0"
          >
            去结算
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { clearCartApi, getCartApi, removeCartApi, updateCartApi, selectedCartApi } from '@/api/cart'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const list = ref([])
const loading = ref(false)

const load = async () => {
  loading.value = true
  try {
    list.value = await getCartApi()
  } catch (error) {
    ElMessage.error('加载购物车失败')
  } finally {
    loading.value = false
  }
}

const updateQty = async (item, delta) => {
  const newQty = item.quantity + delta
  if (newQty < 1) return
  try {
    await updateCartApi({ id: item.id, quantity: newQty, selected: item.selected })
    item.quantity = newQty
    item.totalPrice = (item.price || 0) * newQty
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

const updateQtyInput = async (item, event) => {
  const newQty = parseInt(event.target.value) || 1
  if (newQty < 1) return
  try {
    await updateCartApi({ id: item.id, quantity: newQty, selected: item.selected })
    item.quantity = newQty
    item.totalPrice = (item.price || 0) * newQty
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

const remove = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await removeCartApi(id)
    await load()
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const clear = async () => {
  try {
    await ElMessageBox.confirm('确定要清空购物车吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await clearCartApi()
    await load()
    ElMessage.success('清空成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清空失败')
    }
  }
}

const handleSelectedChange = async (item) => {
  try {
    await selectedCartApi({ id: item.id, selected: item.selected ? 1 : 0 })
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const goToCheckout = () => {
  const selectedIds = list.value
    .filter(item => item.selected)
    .map(item => item.id)
    .join(',')
  router.push(`/checkout?cartIds=${selectedIds}`)
}

const selectAll = computed({
  get: () => list.value.length > 0 && list.value.every(item => item.selected),
  set: (value) => {
    list.value.forEach(item => {
      item.selected = value
      selectedCartApi({ id: item.id, selected: value ? 1 : 0 })
    })
  }
})

const selectedCount = computed(() => list.value.filter(item => item.selected).length)

const totalPrice = computed(() => {
  return list.value
    .filter(item => item.selected)
    .reduce((sum, item) => sum + (item.totalPrice || 0), 0)
})

onMounted(load)
</script>

<style scoped>
.cart-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  padding-bottom: 100px;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-color);
  margin: 0;
  padding-left: 12px;
  border-left: 4px solid var(--primary-color);
}

.loading-wrap {
  background: #fff;
  padding: 40px;
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-card);
}

.empty-wrap {
  background: #fff;
  padding: 100px 20px;
  text-align: center;
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-card);
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 24px;
  opacity: 0.8;
}

.empty-text {
  color: var(--text-secondary);
  font-size: 16px;
  margin-bottom: 30px;
}

.cart-content {
  background: #fff;
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: var(--shadow-card);
}

.cart-header {
  display: flex;
  align-items: center;
  padding: 16px 24px;
  background: linear-gradient(135deg, #fafbfc 0%, #f5f7fa 100%);
  border-bottom: 1px solid var(--border-color);
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 500;
}

.cart-header :deep(.el-checkbox__label) {
  color: var(--text-secondary);
}

.col-check {
  width: 60px;
}

.col-product {
  flex: 1;
  min-width: 300px;
}

.col-price {
  width: 120px;
  text-align: center;
}

.col-quantity {
  width: 140px;
  text-align: center;
}

.col-total {
  width: 120px;
  text-align: center;
}

.col-action {
  width: 80px;
  text-align: center;
}

.cart-list {
  padding: 0;
}

.cart-item {
  display: flex;
  align-items: center;
  padding: 24px;
  border-bottom: 1px solid var(--border-color);
  transition: all var(--transition-base);
}

.cart-item:last-child {
  border-bottom: none;
}

.cart-item:hover {
  background: #fafbfc;
}

.product-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 16px;
}

.product-img {
  width: 90px;
  height: 90px;
  object-fit: cover;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  transition: all var(--transition-base);
}

.product-info:hover .product-img {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-md);
}

.product-name {
  font-size: 15px;
  color: var(--text-color);
  line-height: 1.5;
  max-width: 280px;
  transition: color var(--transition-base);
}

.product-info:hover .product-name {
  color: var(--primary-color);
}

.price {
  color: var(--text-secondary);
  font-size: 15px;
  font-weight: 500;
}

.quantity-box {
  display: inline-flex;
  align-items: center;
  border: 2px solid var(--border-color);
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: all var(--transition-base);
}

.quantity-box:hover {
  border-color: var(--primary-light);
}

.qty-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: #fafbfc;
  cursor: pointer;
  font-size: 18px;
  color: var(--text-secondary);
  transition: all var(--transition-base);
  display: flex;
  align-items: center;
  justify-content: center;
}

.qty-btn:hover {
  background: var(--gradient-primary);
  color: #fff;
}

.qty-btn:active {
  transform: scale(0.95);
}

.qty-input {
  width: 50px;
  height: 36px;
  border: none;
  border-left: 1px solid var(--border-color);
  border-right: 1px solid var(--border-color);
  text-align: center;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-color);
  outline: none;
  background: #fff;
}

.total-price {
  color: var(--price-color);
  font-size: 18px;
  font-weight: 700;
}

.action-btn {
  color: var(--text-light);
  cursor: pointer;
  font-size: 14px;
  transition: color var(--transition-base);
  padding: 6px 12px;
  border-radius: var(--radius-md);
}

.action-btn:hover {
  color: var(--danger-color);
  background: rgba(255, 118, 117, 0.1);
}

.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: #fff;
  border-top: 2px solid var(--gradient-primary);
  position: sticky;
  bottom: 0;
  z-index: 100;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.08);
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.action-link {
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 14px;
  transition: color var(--transition-base);
  padding: 6px 12px;
  border-radius: var(--radius-md);
}

.action-link:hover {
  color: var(--danger-color);
  background: rgba(255, 118, 117, 0.1);
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 32px;
}

.summary {
  text-align: right;
}

.selected-count {
  color: var(--text-secondary);
  font-size: 14px;
}

.selected-count em {
  color: var(--primary-color);
  font-style: normal;
  font-weight: 600;
}

.total-box {
  margin-top: 8px;
}

.total-label {
  color: var(--text-secondary);
  font-size: 14px;
}

.total-amount {
  color: var(--price-color);
  font-size: 28px;
  font-weight: 700;
}

.checkout-btn {
  width: 160px;
  height: 52px;
  font-size: 18px;
  font-weight: 600;
  border-radius: var(--radius-xl) !important;
}

@media (max-width: 768px) {
  .cart-page {
    padding: 16px;
    padding-bottom: 120px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .cart-header {
    display: none;
  }
  
  .cart-item {
    padding: 16px;
    flex-wrap: wrap;
  }
  
  .col-check {
    position: absolute;
    top: 16px;
    left: 16px;
  }
  
  .cart-item {
    position: relative;
    padding-left: 50px;
  }
  
  .col-product {
    width: 100%;
    min-width: 0;
    margin-bottom: 12px;
  }
  
  .product-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .product-img {
    width: 100%;
    height: auto;
    aspect-ratio: 1;
  }
  
  .product-name {
    max-width: 100%;
  }
  
  .col-price,
  .col-quantity,
  .col-total,
  .col-action {
    width: auto;
    flex: 1;
    text-align: left;
  }
  
  .col-price::before {
    content: '单价：';
    color: var(--text-light);
    font-size: 12px;
  }
  
  .col-total::before {
    content: '小计：';
    color: var(--text-light);
    font-size: 12px;
  }
  
  .cart-footer {
    flex-direction: column;
    gap: 16px;
    padding: 16px;
  }
  
  .footer-left {
    width: 100%;
    justify-content: space-between;
  }
  
  .footer-right {
    width: 100%;
    justify-content: space-between;
  }
  
  .checkout-btn {
    width: auto;
    flex: 1;
  }
}
</style>
