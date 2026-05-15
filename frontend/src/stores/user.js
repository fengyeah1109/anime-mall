import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || '{}')
  }),
  getters: {
    isLogin: (state) => !!state.token,
    role: (state) => state.user?.role || 'USER'
  },
  actions: {
    setAuth(token, user = {}) {
      this.token = token
      this.user = user
      localStorage.setItem('token', token)
      localStorage.setItem('user', JSON.stringify(user))
    },
    logout() {
      this.token = ''
      this.user = {}
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})
