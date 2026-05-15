<template>
  <div class="admin-page">
    <div class="page-header">
      <h3 class="page-title">商品管理</h3>
      <el-button type="primary" @click="open()">
        <span class="btn-icon">+</span> 新增商品
      </el-button>
    </div>

    <div class="table-wrap">
      <el-table :data="list.records || []" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="mainImage" label="图片" width="80" align="center">
          <template #default="{ row }">
            <img :src="row.mainImage" class="product-img" v-if="row.mainImage" />
            <span v-else class="no-img">暂无</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="180">
          <template #default="{ row }">
            <div class="product-name">{{ row.name }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="100" align="right">
          <template #default="{ row }">
            <span class="price">{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" align="center">
          <template #default="{ row }">
            <span :class="{ 'low-stock': row.stock < 10 }">{{ row.stock }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sales" label="销量" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <span class="status-tag" :class="row.status === 1 ? 'on' : 'off'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <span class="action-btn" @click="open(row)">编辑</span>
              <span 
                class="action-btn" 
                :class="row.status === 1 ? 'warning' : 'success'" 
                @click="toggleStatus(row)"
              >
                {{ row.status === 1 ? '下架' : '上架' }}
              </span>
              <span class="action-btn danger" @click="remove(row.id)">删除</span>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="list.total || 0"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="load"
          @current-change="load"
        />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑商品' : '新增商品'" width="700px">
      <el-form :model="form" label-width="100px" :rules="rules" ref="formRef">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入商品描述" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="售价" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="原价">
              <el-input-number v-model="form.originalPrice" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="库存" prop="stock">
              <el-input-number v-model="form.stock" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类">
              <el-cascader
                v-model="form.categoryId"
                :options="categoryTree"
                :props="{ value: 'id', label: 'name', children: 'children', checkStrictly: true, emitPath: false }"
                placeholder="请选择分类"
                clearable
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="动漫IP">
          <el-select v-model="form.animeIpId" placeholder="请选择动漫IP" clearable style="width: 100%">
            <el-option label="请选择" :value="null" />
            <el-option 
              v-for="ip in animeIps" 
              :key="ip.id" 
              :label="ip.name" 
              :value="ip.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="商品主图" prop="mainImage">
          <div class="image-upload-container">
            <el-tabs v-model="imageUploadType" type="border-card">
              <el-tab-pane label="图片链接" name="url">
                <el-input 
                  v-model="form.mainImage" 
                  placeholder="请输入图片URL" 
                  clearable
                />
                <div class="image-preview" v-if="form.mainImage">
                  <img :src="form.mainImage" alt="预览图" />
                </div>
              </el-tab-pane>
              <el-tab-pane label="上传图片" name="upload">
                <el-upload
                  class="image-uploader"
                  :action="uploadUrl"
                  :headers="uploadHeaders"
                  :show-file-list="false"
                  :on-success="handleUploadSuccess"
                  :before-upload="beforeUpload"
                  drag
                >
                  <div class="upload-content" v-if="!form.mainImage">
                    <el-icon class="upload-icon"><Upload /></el-icon>
                    <div class="upload-text">拖拽图片到此处或<em>点击上传</em></div>
                    <div class="upload-tip">支持 JPG、PNG 格式，大小不超过 2MB</div>
                  </div>
                  <img v-else :src="form.mainImage" class="uploaded-image" />
                </el-upload>
              </el-tab-pane>
            </el-tabs>
          </div>
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="form.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="是否热销">
              <el-switch v-model="form.isHot" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="是否新品">
              <el-switch v-model="form.isNew" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
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
import { onMounted, ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { 
  adminProductsApi, 
  adminCreateProductApi, 
  adminUpdateProductApi, 
  adminDeleteProductApi,
  getAnimeIpsApi
} from '@/api/admin'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const list = ref({})
const categoryTree = ref([])
const animeIps = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const imageUploadType = ref('url')

const uploadUrl = import.meta.env.VITE_API_URL + '/upload'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

const defaultForm = {
  id: null,
  name: '',
  description: '',
  price: 0,
  originalPrice: 0,
  stock: 0,
  mainImage: '',
  categoryId: null,
  animeIpId: null,
  tags: '',
  isHot: 0,
  isNew: 0,
  status: 1
}

const form = ref({ ...defaultForm })

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
  mainImage: [
    { required: true, message: '请输入图片URL', trigger: 'blur' },
    { max: 1000, message: '图片URL长度不能超过1000个字符', trigger: 'blur' }
  ]
}

const load = async () => {
  loading.value = true
  try {
    list.value = await adminProductsApi({ page: page.value, size: size.value })
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    categoryTree.value = await request.get('/admin/categories/tree')
  } catch (error) {
    console.error('加载分类失败')
  }
}

const loadAnimeIps = async () => {
  try {
    animeIps.value = await getAnimeIpsApi()
  } catch (error) {
    console.error('加载IP失败')
  }
}

const open = (row) => {
  if (row) {
    form.value = { ...row }
  } else {
    form.value = { ...defaultForm }
  }
  dialogVisible.value = true
}

const beforeUpload = (file) => {
  const isImage = ['image/jpeg', 'image/png', 'image/jpg'].includes(file.type)
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传 JPG/PNG 格式的图片!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handleUploadSuccess = (response) => {
  if (response && response.data) {
    form.value.mainImage = response.data
    ElMessage.success('上传成功')
  }
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
      await adminUpdateProductApi(form.value)
      ElMessage.success('更新成功')
    } else {
      await adminCreateProductApi(form.value)
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
  try {
    await request.put(`/admin/products/${row.id}/status`, null, { params: { status: newStatus } })
    row.status = newStatus
    ElMessage.success(newStatus === 1 ? '已上架' : '已下架')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const remove = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      type: 'warning'
    })
    await adminDeleteProductApi(id)
    ElMessage.success('删除成功')
    load()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  load()
  loadCategories()
  loadAnimeIps()
})
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

.table-wrap :deep(.el-table td) {
  border-bottom: 1px solid rgba(255, 107, 107, 0.12);
  padding: 16px 0;
  color: #1a1a1a;
  font-weight: 500;
}

.product-img {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #eee;
}

.no-img {
  color: #999;
  font-size: 12px;
}

.product-name {
  color: #333;
  line-height: 1.4;
}

.price {
  color: #ff6b6b;
  font-weight: 600;
}

.price::before {
  content: '¥';
  font-size: 0.85em;
}

.low-stock {
  color: #ff4d4f;
  font-weight: 600;
}

.status-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-tag.on {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  color: #fff;
}

.status-tag.off {
  background: #f5f5f5;
  color: #999;
}

.action-btns {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.action-btn {
  color: #667eea;
  cursor: pointer;
  font-size: 13px;
  padding: 4px 12px;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.action-btn:hover {
  color: #fff;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.action-btn.warning {
  color: #faad14;
}

.action-btn.warning:hover {
  color: #fff;
  background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%);
}

.action-btn.success {
  color: #52c41a;
}

.action-btn.success:hover {
  color: #fff;
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
}

.action-btn.danger {
  color: #ff4d4f;
}

.action-btn.danger:hover {
  color: #fff;
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
}

.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.image-upload-container {
  width: 100%;
}

.image-upload-container :deep(.el-tabs__content) {
  padding: 15px;
}

.image-preview {
  margin-top: 15px;
  text-align: center;
}

.image-preview img {
  max-width: 200px;
  max-height: 200px;
  border-radius: 8px;
  border: 1px solid #eee;
}

.image-uploader {
  width: 100%;
}

.image-uploader :deep(.el-upload) {
  width: 100%;
}

.image-uploader :deep(.el-upload-dragger) {
  width: 100%;
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed #ddd;
  border-radius: 12px;
  background: #fafafa;
  transition: all 0.25s ease;
}

.image-uploader :deep(.el-upload-dragger:hover) {
  border-color: #667eea;
  background: rgba(102, 126, 234, 0.05);
}

.upload-content {
  text-align: center;
  color: #999;
}

.upload-icon {
  font-size: 40px;
  color: #ddd;
  margin-bottom: 10px;
}

.upload-text {
  font-size: 14px;
  color: #666;
}

.upload-text em {
  color: #ff6b6b;
  font-style: normal;
}

.upload-tip {
  font-size: 12px;
  color: #bbb;
  margin-top: 8px;
}

.uploaded-image {
  max-width: 100%;
  max-height: 160px;
  object-fit: contain;
}

:deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid #f0f0f0;
  padding: 18px 24px;
  margin: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

:deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}

:deep(.el-dialog__body) {
  padding: 24px;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid #f0f0f0;
  padding: 16px 24px;
}

:deep(.el-dialog__footer .el-button) {
  border-radius: 10px;
}

:deep(.el-dialog__footer .el-button--primary) {
  background: linear-gradient(135deg, #ff6b6b 0%, #ff8e53 100%);
  border: none;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-input__wrapper) {
  border-radius: 10px;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(102, 126, 234, 0.3);
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2);
}
</style>
