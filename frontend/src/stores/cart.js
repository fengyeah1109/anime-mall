import { defineStore } from 'pinia'
import { getCartApi } from '@/api/cart'

export const useCartStore = defineStore('cart', {
  state: () => ({ list: [] }),
  getters: {
    totalCount: (s) => s.list.reduce((sum, it) => sum + it.quantity, 0)
  },
  actions: {
    async loadCart() {
      this.list = await getCartApi()
    }
  }
})
