import { defineStore } from 'pinia'
import { getCartApi, addCartApi } from '@/api/cart'

export const useCartStore = defineStore('cart', {
  state: () => ({ list: [] }),
  getters: {
    totalCount: (s) => s.list.reduce((sum, it) => sum + it.quantity, 0)
  },
  actions: {
    async loadCart() {
      this.list = await getCartApi()
    },
    async addItem(productId, quantity) {
      await addCartApi({ productId, quantity })
      await this.loadCart()
    }
  }
})