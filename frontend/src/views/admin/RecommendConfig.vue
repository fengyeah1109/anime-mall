<template>
  <div class="recommend-config">
    <el-card shadow="never" class="page-card">
      <template #header>
        <div class="card-header">
          <span>推荐算法配置</span>
        </div>
      </template>
      
      <div class="config-content">
        <el-form label-width="120px">
          <el-form-item label="协同过滤权重">
            <el-slider 
              v-model="config.cfWeight" 
              :min="0" 
              :max="1" 
              :step="0.01"
              @change="updateTotal"
            />
            <span class="weight-value">{{ config.cfWeight.toFixed(2) }}</span>
          </el-form-item>
          
          <el-form-item label="基于内容权重">
            <el-slider 
              v-model="config.contentWeight" 
              :min="0" 
              :max="1" 
              :step="0.01"
              @change="updateTotal"
            />
            <span class="weight-value">{{ config.contentWeight.toFixed(2) }}</span>
          </el-form-item>
          
          <el-form-item label="热门推荐权重">
            <el-slider 
              v-model="config.hotWeight" 
              :min="0" 
              :max="1" 
              :step="0.01"
              @change="updateTotal"
            />
            <span class="weight-value">{{ config.hotWeight.toFixed(2) }}</span>
          </el-form-item>
          
          <el-form-item>
            <div class="total-weight">
              <span>权重总和：</span>
              <span :class="{ 'total-warning': Math.abs(totalWeight - 1) > 0.01 }">
                {{ totalWeight.toFixed(2) }}
              </span>
              <span v-if="Math.abs(totalWeight - 1) > 0.01" class="warning-text">
                （权重总和必须为1）
              </span>
            </div>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="saveConfig" :disabled="Math.abs(totalWeight - 1) > 0.01">
              保存配置
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getRecommendConfigApi, updateRecommendConfigApi } from '@/api/admin/config'

const router = useRouter()

const config = ref({
  cfWeight: 0.4,
  contentWeight: 0.4,
  hotWeight: 0.2
})

const totalWeight = computed(() => {
  return config.value.cfWeight + config.value.contentWeight + config.value.hotWeight
})

const updateTotal = () => {
  // 自动调整权重，确保总和为1
  if (Math.abs(totalWeight.value - 1) > 0.01) {
    const sum = totalWeight.value
    if (sum > 0) {
      config.value.cfWeight = config.value.cfWeight / sum
      config.value.contentWeight = config.value.contentWeight / sum
      config.value.hotWeight = config.value.hotWeight / sum
    }
  }
}

const loadConfig = async () => {
  try {
    const data = await getRecommendConfigApi()
    config.value = data
    // 确保权重总和为1
    updateTotal()
  } catch (error) {
    ElMessage.error('加载配置失败')
  }
}

const saveConfig = async () => {
  try {
    await updateRecommendConfigApi(config.value)
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.recommend-config {
  padding: 20px;
  background: transparent;
  min-height: calc(100vh - 60px);
}

.page-card {
  margin-bottom: 24px;
  border-radius: 20px;
  border: none;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}

.page-card:hover {
  box-shadow: 0 12px 40px rgba(255, 107, 107, 0.12);
}

:deep(.el-card__header) {
  background: linear-gradient(135deg, #ff6b6b 0%, #ffa07a 100%);
  padding: 20px 28px;
  border: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #fff;
  font-size: 18px;
  font-weight: 700;
}

.config-content {
  margin-top: 24px;
  padding: 10px 0;
}

:deep(.el-form-item) {
  margin-bottom: 28px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #2d3436;
}

:deep(.el-slider) {
  --el-slider-main-bg-color: #ff6b6b;
}

:deep(.el-slider__runway) {
  background: linear-gradient(90deg, #ff6b6b 0%, #ffa07a 100%);
  height: 8px;
  border-radius: 4px;
}

:deep(.el-slider__bar) {
  background: linear-gradient(90deg, #ff6b6b 0%, #ffa07a 100%);
  height: 8px;
  border-radius: 4px;
}

:deep(.el-slider__button) {
  border-color: #ff6b6b;
  box-shadow: 0 2px 8px rgba(255, 107, 107, 0.4);
}

.weight-value {
  margin-left: 16px;
  font-size: 15px;
  color: #ff6b6b;
  min-width: 55px;
  display: inline-block;
  font-weight: 600;
}

.total-weight {
  margin: 24px 0;
  padding: 20px 24px;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 16px;
  font-size: 15px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.8);
}

.total-weight span:first-child {
  font-weight: 500;
  color: #2d3436;
}

.total-weight span:nth-child(2) {
  color: #ff6b6b;
  font-weight: 700;
  font-size: 18px;
}

.total-warning {
  color: #c62828 !important;
  font-weight: bold;
}

.warning-text {
  color: #c62828;
  font-size: 13px;
  background: rgba(198, 40, 40, 0.1);
  padding: 4px 14px;
  border-radius: 20px;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #ff6b6b 0%, #ff8e53 100%);
  border: none;
  border-radius: 12px;
  padding: 12px 32px;
  font-weight: 600;
  transition: all 0.25s ease;
}

:deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #ff5252 0%, #e67e50 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 107, 107, 0.35);
}

:deep(.el-button--primary:disabled) {
  background: linear-gradient(135deg, #c0c0c0 0%, #a0a0a0 100%);
  border: none;
}
</style>