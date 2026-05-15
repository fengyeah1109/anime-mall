<template>
  <div class="admin-page">
    <div class="page-header">
      <h3 class="page-title">分类管理</h3>
      <div class="tabs-wrap">
        <span 
          class="tab-item" 
          :class="{ active: activeTab === 'category' }"
          @click="activeTab = 'category'"
        >商品分类</span>
        <span 
          class="tab-item" 
          :class="{ active: activeTab === 'animeIp' }"
          @click="activeTab = 'animeIp'"
        >动漫IP</span>
      </div>
      <el-button type="primary" @click="open()">
        <span class="btn-icon">+</span> {{ activeTab === 'category' ? '新增分类' : '新增IP' }}
      </el-button>
    </div>

    <!-- 商品分类表格 -->
    <div class="table-wrap" v-if="activeTab === 'category'">
      <el-table 
        :data="list" 
        v-loading="loading" 
        border 
        row-key="id"
        default-expand-all
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      >
        <el-table-column prop="name" label="分类名称" min-width="200" />
        <el-table-column prop="icon" label="图标" width="100" align="center">
          <template #default="{ row }">
            <img :src="row.icon" class="category-icon" v-if="row.icon" />
            <span v-else class="no-data">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="level" label="层级" width="80" align="center">
          <template #default="{ row }">
            <span class="level-tag">第{{ row.level }}级</span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <span class="status-tag" :class="row.status === 1 ? 'on' : 'off'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <span class="action-btn" @click="openCategory(row)">编辑</span>
              <span class="action-btn success" @click="openCategory(null, row.id)" v-if="row.level < 3">添加子分类</span>
              <span class="action-btn danger" @click="removeCategory(row.id)">删除</span>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 动漫IP表格 -->
    <div class="table-wrap" v-if="activeTab === 'animeIp'">
      <el-table 
        :data="animeIpList" 
        v-loading="loading" 
        border 
        row-key="id"
      >
        <el-table-column prop="name" label="IP名称" min-width="200" />
        <el-table-column prop="cover" label="封面图" width="100" align="center">
          <template #default="{ row }">
            <img :src="row.cover" class="category-icon" v-if="row.cover" />
            <span v-else class="no-data">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <span class="status-tag" :class="row.status === 1 ? 'on' : 'off'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <span class="action-btn" @click="openAnimeIp(row)">编辑</span>
              <span class="action-btn danger" @click="removeAnimeIp(row.id)">删除</span>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 商品分类弹窗 -->
    <el-dialog v-model="dialogVisible" :title="categoryForm.id ? '编辑分类' : '新增分类'" width="500px">
      <el-form :model="categoryForm" label-width="80px" :rules="categoryRules" ref="categoryFormRef">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="上级分类">
          <el-cascader
            v-model="categoryForm.parentId"
            :options="parentOptions"
            :props="{ value: 'id', label: 'name', children: 'children', checkStrictly: true, emitPath: false }"
            placeholder="请选择上级分类（不选则为一级分类）"
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="分类图标">
          <el-input v-model="categoryForm.icon" placeholder="请输入图标URL" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="categoryForm.sort" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="categoryForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCategory" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 动漫IP弹窗 -->
    <el-dialog v-model="animeIpDialogVisible" :title="animeIpForm.id ? '编辑IP' : '新增IP'" width="500px">
      <el-form :model="animeIpForm" label-width="80px" :rules="animeIpRules" ref="animeIpFormRef">
        <el-form-item label="IP名称" prop="name">
          <el-input v-model="animeIpForm.name" placeholder="请输入IP名称" />
        </el-form-item>
        <el-form-item label="封面图">
          <el-input v-model="animeIpForm.cover" placeholder="请输入封面图URL" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="animeIpForm.sort" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="animeIpForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="animeIpDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAnimeIp" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminCategoryTreeApi, getAnimeIpsApi } from '@/api/admin'
import request from '@/utils/request'

const activeTab = ref('category')
const loading = ref(false)
const list = ref([])
const animeIpList = ref([])
const dialogVisible = ref(false)
const animeIpDialogVisible = ref(false)
const submitLoading = ref(false)
const categoryFormRef = ref(null)
const animeIpFormRef = ref(null)

const defaultCategoryForm = {
  id: null,
  name: '',
  parentId: null,
  icon: '',
  sort: 0,
  status: 1
}

const defaultAnimeIpForm = {
  id: null,
  name: '',
  cover: '',
  sort: 0,
  status: 1
}

const categoryForm = ref({ ...defaultCategoryForm })
const animeIpForm = ref({ ...defaultAnimeIpForm })

const categoryRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

const animeIpRules = {
  name: [{ required: true, message: '请输入IP名称', trigger: 'blur' }]
}

const parentOptions = computed(() => {
  const options = JSON.parse(JSON.stringify(list.value))
  const filterLevel = (items, maxLevel = 2) => {
    return items.filter(item => {
      if (item.level >= maxLevel) return false
      if (item.children && item.children.length > 0) {
        item.children = filterLevel(item.children, maxLevel)
      }
      return true
    })
  }
  return filterLevel(options)
})

const loadCategory = async () => {
  loading.value = true
  try {
    list.value = await adminCategoryTreeApi()
  } catch (error) {
    ElMessage.error('加载分类列表失败')
  } finally {
    loading.value = false
  }
}

const loadAnimeIp = async () => {
  loading.value = true
  try {
    animeIpList.value = await getAnimeIpsApi()
  } catch (error) {
    ElMessage.error('加载IP列表失败')
  } finally {
    loading.value = false
  }
}

const load = () => {
  if (activeTab.value === 'category') {
    loadCategory()
  } else {
    loadAnimeIp()
  }
}

watch(activeTab, (newTab) => {
  if (newTab === 'animeIp') {
    loadAnimeIp()
  } else {
    loadCategory()
  }
})

const open = () => {
  if (activeTab.value === 'category') {
    openCategory()
  } else {
    openAnimeIp()
  }
}

const openCategory = (row, parentId = null) => {
  if (row) {
    categoryForm.value = { ...row }
  } else {
    categoryForm.value = { ...defaultCategoryForm, parentId }
  }
  dialogVisible.value = true
}

const openAnimeIp = (row) => {
  if (row) {
    animeIpForm.value = { ...row }
  } else {
    animeIpForm.value = { ...defaultAnimeIpForm }
  }
  animeIpDialogVisible.value = true
}

const submitCategory = async () => {
  try {
    await categoryFormRef.value.validate()
  } catch {
    return
  }

  submitLoading.value = true
  try {
    if (categoryForm.value.id) {
      await request.put(`/admin/categories/${categoryForm.value.id}`, categoryForm.value)
      ElMessage.success('更新成功')
    } else {
      await request.post('/admin/categories', categoryForm.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadCategory()
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

const submitAnimeIp = async () => {
  try {
    await animeIpFormRef.value.validate()
  } catch {
    return
  }

  submitLoading.value = true
  try {
    if (animeIpForm.value.id) {
      await request.put(`/admin/anime-ips/${animeIpForm.value.id}`, animeIpForm.value)
      ElMessage.success('更新成功')
    } else {
      await request.post('/admin/anime-ips', animeIpForm.value)
      ElMessage.success('创建成功')
    }
    animeIpDialogVisible.value = false
    loadAnimeIp()
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

const removeCategory = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该分类吗？删除后不可恢复', '提示', {
      type: 'warning'
    })
    await request.delete(`/admin/categories/${id}`)
    ElMessage.success('删除成功')
    loadCategory()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const removeAnimeIp = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该IP吗？删除后不可恢复', '提示', {
      type: 'warning'
    })
    await request.delete(`/admin/anime-ips/${id}`)
    ElMessage.success('删除成功')
    loadAnimeIp()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
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

.tabs-wrap {
  display: flex;
  gap: 10px;
}

.tab-item {
  padding: 10px 22px;
  font-size: 14px;
  color: #636e72;
  cursor: pointer;
  border-radius: 25px;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.5);
  font-weight: 500;
}

.tab-item:hover {
  color: #ff6b6b;
  background: rgba(255, 107, 107, 0.1);
}

.tab-item.active {
  background: linear-gradient(135deg, #ff6b6b 0%, #ffa07a 100%);
  color: #fff;
  box-shadow: 0 4px 15px rgba(255, 107, 107, 0.35);
}

.btn-icon {
  margin-right: 5px;
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

.category-icon {
  width: 45px;
  height: 45px;
  object-fit: cover;
  border-radius: 12px;
  border: 2px solid rgba(255, 107, 107, 0.2);
  transition: all 0.3s ease;
}

.category-icon:hover {
  transform: scale(1.15);
  border-color: #ff6b6b;
}

.no-data {
  color: #b2bec3;
}

.level-tag {
  display: inline-block;
  padding: 5px 14px;
  background: linear-gradient(135deg, #f5f5f5 0%, #e8e8e8 100%);
  border-radius: 25px;
  font-size: 12px;
  color: #636e72;
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

.status-tag.on {
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  color: #2e7d32;
}

.status-tag.off {
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

.action-btn.danger {
  color: #c62828;
}

.action-btn.danger:hover {
  background: rgba(198, 40, 40, 0.1);
  color: #ef5350;
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
