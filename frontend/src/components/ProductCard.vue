<template>
  <el-card shadow="hover">
    <img :src="getImageUrl(item.mainImage)" style="width:100%;height:180px;object-fit:cover" />
    <div style="margin-top: 8px">{{ item.name }}</div>
    <el-text type="danger">¥{{ item.price }}</el-text>
    <div style="margin-top: 8px">
      <el-button type="primary" size="small" @click="$router.push(`/products/${item.id}`)">查看详情</el-button>
    </div>
  </el-card>
</template>

<script setup>
import { getImageUrl } from '@/utils/image'

defineProps({ item: { type: Object, required: true } })
</script>
