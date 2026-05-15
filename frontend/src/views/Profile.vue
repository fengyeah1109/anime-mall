<template>
  <div class="profile-page">
    <div class="page-header">
      <h2 class="page-title">个人中心</h2>
    </div>

    <div class="profile-content">
      <div class="sidebar">
        <div class="user-card">
          <div class="avatar-wrap">
            <el-avatar :size="80" :src="profile.avatar" />
            <el-upload :http-request="uploadAvatar" :show-file-list="false" class="avatar-upload">
              <span class="upload-text">更换头像</span>
            </el-upload>
          </div>
          <div class="user-info">
            <div class="nickname">{{ profile.nickname || '用户' }}</div>
            <div class="phone">{{ profile.phone || '未绑定手机' }}</div>
          </div>
        </div>
        <div class="menu-list">
          <div 
            class="menu-item" 
            :class="{ active: activeTab === 'profile' }" 
            @click="activeTab = 'profile'"
          >
            <span class="menu-text">资料修改</span>
          </div>
          <div 
            class="menu-item" 
            :class="{ active: activeTab === 'address' }" 
            @click="activeTab = 'address'"
          >
            <span class="menu-text">收货地址</span>
          </div>
        </div>
      </div>

      <div class="main-content">
        <div v-show="activeTab === 'profile'" class="profile-section">
          <div class="section-title">资料修改</div>
          <el-form :model="profile" label-width="80px" class="profile-form">
            <el-form-item label="昵称">
              <el-input v-model="profile.nickname" placeholder="请输入昵称" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="profile.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="profile.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveProfile" :loading="saveProfileLoading">保存修改</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div v-show="activeTab === 'address'" class="address-section">
          <div class="section-header">
            <div class="section-title">收货地址</div>
            <el-button type="primary" @click="showAddressDialog()">添加地址</el-button>
          </div>
          <div class="address-list" v-if="addresses.length">
            <div v-for="address in addresses" :key="address.id" class="address-item">
              <div class="address-content">
                <div class="address-header">
                  <span class="receiver-name">{{ address.receiverName }}</span>
                  <span class="receiver-phone">{{ address.receiverPhone }}</span>
                  <span v-if="address.isDefault === 1" class="default-tag">默认</span>
                </div>
                <div class="address-detail">
                  {{ address.province }} {{ address.city }} {{ address.district }} {{ address.detailAddress }}
                </div>
              </div>
              <div class="address-actions">
                <span class="action-link" @click="setDefaultAddress(address.id)" v-if="address.isDefault !== 1">设为默认</span>
                <span class="action-link" @click="showAddressDialog(address)">编辑</span>
                <span class="action-link danger" @click="deleteAddress(address.id)">删除</span>
              </div>
            </div>
          </div>
          <div v-else class="empty-address">
            <p>暂无收货地址</p>
            <el-button type="primary" @click="showAddressDialog()">添加地址</el-button>
          </div>
        </div>

        <div class="logout-section">
          <el-button type="danger" size="large" @click="handleLogout" :loading="logoutLoading">
            退出登录
          </el-button>
        </div>
      </div>
    </div>

    <el-dialog v-model="addressDialogVisible" :title="isEditAddress ? '编辑地址' : '添加地址'" width="500px" class="address-dialog">
      <el-form :model="addressForm" :rules="addressRules" ref="addressFormRef" label-width="80px">
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="receiverPhone">
          <el-input v-model="addressForm.receiverPhone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="省份" prop="province">
          <el-input v-model="addressForm.province" placeholder="请输入省份" />
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-input v-model="addressForm.city" placeholder="请输入城市" />
        </el-form-item>
        <el-form-item label="区县" prop="district">
          <el-input v-model="addressForm.district" placeholder="请输入区县" />
        </el-form-item>
        <el-form-item label="详细地址" prop="detailAddress">
          <el-input v-model="addressForm.detailAddress" type="textarea" :rows="3" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="addressForm.isDefault" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addressDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAddress" :loading="saveAddressLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  getProfileApi, 
  updateProfileApi, 
  uploadAvatarApi,
  getAddressesApi,
  addAddressApi,
  updateAddressApi,
  deleteAddressApi,
  setDefaultAddressApi
} from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('profile')
const profile = ref({})
const addresses = ref([])
const logoutLoading = ref(false)
const saveProfileLoading = ref(false)
const saveAddressLoading = ref(false)
const addressDialogVisible = ref(false)
const isEditAddress = ref(false)
const addressFormRef = ref()

const addressForm = ref({
  id: null,
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  isDefault: 0
})

const addressRules = {
  receiverName: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  receiverPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  district: [{ required: true, message: '请输入区县', trigger: 'blur' }],
  detailAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

const loadProfile = async () => { 
  profile.value = await getProfileApi() 
}

const loadAddresses = async () => {
  addresses.value = await getAddressesApi()
}

const saveProfile = async () => { 
  saveProfileLoading.value = true
  try {
    await updateProfileApi(profile.value)
    ElMessage.success('保存成功')
  } finally {
    saveProfileLoading.value = false
  }
}

const uploadAvatar = async ({ file }) => { 
  await uploadAvatarApi(file)
  ElMessage.success('上传成功')
  loadProfile()
}

const showAddressDialog = (address = null) => {
  if (address) {
    isEditAddress.value = true
    addressForm.value = { ...address }
  } else {
    isEditAddress.value = false
    addressForm.value = {
      id: null,
      receiverName: '',
      receiverPhone: '',
      province: '',
      city: '',
      district: '',
      detailAddress: '',
      isDefault: 0
    }
  }
  addressDialogVisible.value = true
}

const saveAddress = async () => {
  try {
    await addressFormRef.value.validate()
    saveAddressLoading.value = true
    
    if (isEditAddress.value) {
      await updateAddressApi(addressForm.value.id, addressForm.value)
      ElMessage.success('修改成功')
    } else {
      await addAddressApi(addressForm.value)
      ElMessage.success('添加成功')
    }
    
    addressDialogVisible.value = false
    loadAddresses()
  } catch (error) {
    if (error !== false) {
      ElMessage.error('保存失败')
    }
  } finally {
    saveAddressLoading.value = false
  }
}

const deleteAddress = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该地址吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteAddressApi(id)
    ElMessage.success('删除成功')
    loadAddresses()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const setDefaultAddress = async (id) => {
  try {
    await setDefaultAddressApi(id)
    ElMessage.success('设置成功')
    loadAddresses()
  } catch (error) {
    ElMessage.error('设置失败')
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    logoutLoading.value = true
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('退出登录失败')
    }
  } finally {
    logoutLoading.value = false
  }
}

onMounted(() => {
  loadProfile()
  loadAddresses()
})
</script>

<style scoped>
.profile-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 24px;
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

.profile-content {
  display: flex;
  gap: 24px;
}

.sidebar {
  width: 260px;
  flex-shrink: 0;
}

.user-card {
  background: #fff;
  border-radius: var(--radius-xl);
  padding: 30px 25px;
  text-align: center;
  margin-bottom: 16px;
  box-shadow: var(--shadow-card);
  transition: all var(--transition-base);
}

.user-card:hover {
  box-shadow: var(--shadow-card-hover);
}

.avatar-wrap {
  position: relative;
  display: inline-block;
}

.avatar-wrap :deep(.el-avatar) {
  border: 4px solid var(--gradient-primary);
  box-shadow: var(--shadow-md);
}

.avatar-upload {
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
}

.upload-text {
  display: inline-block;
  padding: 4px 12px;
  background: var(--gradient-primary);
  color: #fff;
  font-size: 12px;
  border-radius: var(--radius-full);
  cursor: pointer;
  white-space: nowrap;
  transition: all var(--transition-base);
}

.upload-text:hover {
  transform: scale(1.05);
  box-shadow: var(--shadow-md);
}

.user-info {
  margin-top: 20px;
}

.nickname {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-color);
  margin-bottom: 6px;
}

.phone {
  font-size: 14px;
  color: var(--text-light);
}

.menu-list {
  background: #fff;
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: var(--shadow-card);
}

.menu-item {
  padding: 16px 24px;
  cursor: pointer;
  transition: all var(--transition-base);
  border-left: 4px solid transparent;
  position: relative;
}

.menu-item::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 24px;
  right: 24px;
  height: 1px;
  background: var(--border-color);
}

.menu-item:last-child::after {
  display: none;
}

.menu-item:hover {
  background: var(--primary-light);
}

.menu-item.active {
  background: var(--primary-light);
  border-left-color: var(--primary-color);
}

.menu-item.active .menu-text {
  color: var(--primary-color);
  font-weight: 600;
}

.menu-text {
  font-size: 15px;
  color: var(--text-color);
}

.main-content {
  flex: 1;
  background: #fff;
  border-radius: var(--radius-xl);
  padding: 30px;
  box-shadow: var(--shadow-card);
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-color);
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid var(--border-color);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header .section-title {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.profile-form {
  max-width: 450px;
}

.profile-form :deep(.el-form-item__label) {
  font-weight: 500;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.address-item {
  padding: 24px;
  border: 2px solid var(--border-color);
  border-radius: var(--radius-lg);
  transition: all var(--transition-base);
}

.address-item:hover {
  border-color: var(--primary-light);
  box-shadow: var(--shadow-sm);
}

.address-content {
  margin-bottom: 16px;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}

.receiver-name {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-color);
}

.receiver-phone {
  font-size: 14px;
  color: var(--text-secondary);
}

.default-tag {
  padding: 4px 12px;
  background: var(--gradient-primary);
  color: #fff;
  font-size: 12px;
  border-radius: var(--radius-full);
  font-weight: 500;
}

.address-detail {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.7;
}

.address-actions {
  display: flex;
  gap: 24px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
}

.action-link {
  font-size: 14px;
  color: var(--primary-color);
  cursor: pointer;
  transition: all var(--transition-base);
  padding: 4px 8px;
  border-radius: var(--radius-sm);
}

.action-link:hover {
  background: var(--primary-light);
  color: var(--primary-hover);
}

.action-link.danger {
  color: var(--danger-color);
}

.action-link.danger:hover {
  background: rgba(255, 118, 117, 0.1);
  color: var(--danger-color);
}

.empty-address {
  text-align: center;
  padding: 60px 0;
  color: var(--text-light);
}

.empty-address p {
  margin-bottom: 20px;
  font-size: 16px;
}

.logout-section {
  margin-top: 40px;
  padding-top: 30px;
  border-top: 2px solid var(--border-color);
  text-align: center;
}

.logout-section :deep(.el-button--danger) {
  border-radius: var(--radius-xl);
  padding: 14px 40px;
  font-size: 16px;
}

@media (max-width: 768px) {
  .profile-content {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
  }
  
  .main-content {
    padding: 20px;
  }
}
</style>
