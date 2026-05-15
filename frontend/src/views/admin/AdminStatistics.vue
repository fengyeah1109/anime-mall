<template>
  <el-card><div ref="salesRef" style="height:320px" /></el-card>
  <el-card style="margin-top:12px"><div ref="hotRef" style="height:320px" /></el-card>
</template>

<script setup>
import * as echarts from 'echarts'
import { onMounted, ref } from 'vue'
import { adminHotProductsApi, adminSalesApi } from '@/api/admin'

const salesRef = ref(null)
const hotRef = ref(null)

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  // 处理各种日期格式
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) {
    // 如果解析失败，尝试截取前10位
    return dateStr.slice(0, 10)
  }
  // 格式化为 YYYY-MM-DD
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

onMounted(async () => {
  let sales = await adminSalesApi()
  const hot = await adminHotProductsApi()

  // 对销售数据按时间排序
  sales = sales.sort((a, b) => {
    const dateA = new Date(a.createTime)
    const dateB = new Date(b.createTime)
    return dateA.getTime() - dateB.getTime()
  })

  echarts.init(salesRef.value).setOption({
    title: { text: '销售趋势' },
    xAxis: { type: 'category', data: sales.map((x) => formatDate(x.createTime)) },
    yAxis: { type: 'value' },
    series: [{ type: 'line', data: sales.map((x) => Number(x.payAmount || 0)) }]
  })
  
  echarts.init(hotRef.value).setOption({
    title: { text: '商品热度' },
    xAxis: { type: 'category', data: hot.map((x) => x.name) },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: hot.map((x) => x.sales || 0) }]
  })
})
</script>

<style scoped>
.admin-statistics {
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
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.page-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #2d3436;
  letter-spacing: 0.5px;
}

:deep(.el-card) {
  border-radius: 20px;
  border: none;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}

:deep(.el-card:hover) {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(255, 107, 107, 0.15);
}

:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-card:first-child) {
  margin-bottom: 24px;
}

.chart-container {
  background: linear-gradient(135deg, #ff6b6b 0%, #ffa07a 100%);
  border-radius: 16px;
  padding: 20px;
  height: 320px;
  box-shadow: inset 0 2px 10px rgba(0, 0, 0, 0.1);
}

:deep(.el-card [style*="height:320px"]) {
  border-radius: 16px;
  overflow: hidden;
}
</style>
