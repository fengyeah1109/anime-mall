<template>
  <div class="login-page">
    <!-- 装饰元素 -->
    <div class="decorations">
      <div class="decoration cloud cloud-1">☁️</div>
      <div class="decoration cloud cloud-2">☁️</div>
      <div class="decoration star star-1">⭐</div>
      <div class="decoration star star-2">✨</div>
      <div class="decoration cat-ear cat-ear-1">🐱</div>
      <div class="decoration cat-ear cat-ear-2">🐱</div>
    </div>
    
    <!-- 噪点纹理 -->
    <div class="noise"></div>
    
    <div class="login-box">
      <!-- 卡片内装饰 -->
      <div class="card-decoration card-decoration-top-left">🌸</div>
      <div class="card-decoration card-decoration-top-right">✨</div>
      <div class="card-decoration card-decoration-bottom-left">🌟</div>
      <div class="card-decoration card-decoration-bottom-right">🌸</div>
      
      <div class="login-header">
        <h1 class="logo">动漫商城</h1>
        <p class="slogan">正品周边，品质保证</p>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" class="login-form">
        <el-form-item prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="用户名" 
            size="large"
            prefix-icon="UserFilled"
          />
        </el-form-item>
        <el-form-item prop="identifier">
          <el-input 
            v-model="form.identifier" 
            placeholder="手机号/邮箱" 
            size="large"
            prefix-icon="Message"
          />
        </el-form-item>
        <el-form-item prop="code">
          <el-input 
            v-model="form.code" 
            placeholder="验证码" 
            size="large"
            prefix-icon="Phone"
          >
            <template #append>
              <el-button :disabled="countdown > 0" @click="sendCode" size="small" class="code-btn">
                {{ countdown > 0 ? `${countdown}s后重新发送` : '发送验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            show-password 
            placeholder="密码（至少6位）" 
            size="large"
            prefix-icon="Lock"
          />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input 
            v-model="form.nickname" 
            placeholder="昵称" 
            size="large"
            prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="gender">
          <el-select v-model="form.gender" placeholder="性别" size="large" class="gender-select">
            <el-option label="男" :value="1" />
            <el-option label="女" :value="0" />
            <el-option label="保密" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item prop="birthday">
          <el-date-picker 
            v-model="form.birthday" 
            type="date" 
            placeholder="生日" 
            size="large"
            :disabled-date="(time) => time.getTime() > Date.now()"
            class="date-picker"
          />
        </el-form-item>
        <el-button type="primary" size="large" class="login-btn" @click="submit">
          注册
        </el-button>
        <div class="login-footer">
          <span class="text">已有账号？</span>
          <el-link type="primary" @click="router.push('/login')">立即登录</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { registerApi, sendCodeApi } from '@/api/auth'

const router = useRouter()
const formRef = ref()
const form = ref({
  username: '',
  identifier: '', 
  code: '', 
  password: '',
  nickname: '',
  gender: null,
  birthday: null
})
const countdown = ref(0)

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20位', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        const isUsername = /^[a-zA-Z0-9_]+$/.test(value)
        if (isUsername) {
          callback()
        } else {
          callback(new Error('用户名只能包含字母、数字和下划线'))
        }
      },
      trigger: 'blur'
    }
  ],
  identifier: [
    { required: true, message: '请输入手机号或邮箱', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        const isPhone = /^1[3-9]\d{9}$/.test(value)
        const isEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)
        if (isPhone || isEmail) {
          callback()
        } else {
          callback(new Error('请输入有效的手机号或邮箱'))
        }
      },
      trigger: 'blur'
    }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { length: 6, message: '验证码长度为6位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度为2-20位', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'blur' }
  ]
}

const sendCode = async () => {
  if (!form.value.identifier) {
    ElMessage.warning('请输入手机号或邮箱')
    return
  }
  
  // 验证手机号或邮箱格式
  const isPhone = /^1[3-9]\d{9}$/.test(form.value.identifier)
  const isEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.identifier)
  
  if (!isPhone && !isEmail) {
    ElMessage.warning('请输入有效的手机号或邮箱')
    return
  }
  
  await sendCodeApi(form.value.identifier)
  ElMessage.success('验证码已发送（开发环境：123456）')
  
  // 倒计时
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

const submit = async () => {
  await formRef.value.validate()
  await registerApi(form.value)
  ElMessage.success('注册成功')
  router.push('/login')
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #d4e5ef 0%, #e8f0f5 50%, #b8d4e3 100%);
  padding: 20px;
  position: relative;
  overflow: hidden;
}

/* 噪点纹理 */
.noise {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  opacity: 0.03;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.65' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)'/%3E%3C/svg%3E");
}

/* 装饰元素 */
.decorations {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.decoration {
  position: absolute;
  font-size: 48px;
  opacity: 0.6;
  animation: float 6s ease-in-out infinite;
}

.cloud {
  font-size: 64px;
  opacity: 0.4;
}

.cloud-1 {
  top: 10%;
  left: 5%;
  animation-delay: 0s;
}

.cloud-2 {
  top: 20%;
  right: 10%;
  animation-delay: 3s;
}

.star {
  font-size: 32px;
}

.star-1 {
  top: 15%;
  right: 20%;
  animation-delay: 1s;
}

.star-2 {
  bottom: 25%;
  left: 15%;
  animation-delay: 4s;
}

.cat-ear {
  font-size: 40px;
}

.cat-ear-1 {
  bottom: 15%;
  right: 5%;
  animation-delay: 2s;
}

.cat-ear-2 {
  top: 30%;
  left: 20%;
  animation-delay: 5s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(5deg);
  }
}

.login-box {
  width: 100%;
  max-width: 450px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 2rem;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.15), 0 0 0 1px rgba(255, 255, 255, 0.1) inset;
  position: relative;
  z-index: 10;
  max-height: 90vh;
  overflow-y: auto;
}

/* 卡片内装饰 */
.card-decoration {
  position: absolute;
  font-size: 24px;
  opacity: 0.5;
  pointer-events: none;
}

.card-decoration-top-left {
  top: 16px;
  left: 16px;
}

.card-decoration-top-right {
  top: 16px;
  right: 16px;
}

.card-decoration-bottom-left {
  bottom: 16px;
  left: 16px;
}

.card-decoration-bottom-right {
  bottom: 16px;
  right: 16px;
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo {
  font-size: 32px;
  font-weight: 700;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 8px;
}

.slogan {
  color: var(--text-secondary);
  font-size: 14px;
  margin: 0;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.login-form :deep(.el-input__wrapper) {
  padding: 0 16px;
  height: 48px;
  border-radius: 12px;
  background: #f5f7fa;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.login-form :deep(.el-input__wrapper:hover) {
  background: #edf2f7;
  border-color: rgba(255, 107, 107, 0.2);
}

.login-form :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  border-color: #FF6B6B;
  box-shadow: 0 0 0 3px rgba(255, 107, 107, 0.15);
}

.login-form :deep(.el-input__prefix) {
  color: var(--text-light);
}

.login-form :deep(.el-input__inner) {
  height: 44px;
  line-height: 44px;
}

/* 验证码按钮 */
.code-btn {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  border: none;
  color: #fff;
  padding: 0 20px;
  border-radius: 0 12px 12px 0;
}

.code-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

/* 性别选择器 */
.gender-select :deep(.el-select__wrapper) {
  padding: 0 16px;
  height: 48px;
  border-radius: 12px;
  background: #f5f7fa;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.gender-select :deep(.el-select__wrapper:hover) {
  background: #edf2f7;
  border-color: rgba(255, 107, 107, 0.2);
}

.gender-select :deep(.el-select__wrapper.is-focus) {
  background: #fff;
  border-color: #FF6B6B;
  box-shadow: 0 0 0 3px rgba(255, 107, 107, 0.15);
}

/* 日期选择器 */
.date-picker :deep(.el-date-editor) {
  padding: 0 16px;
  height: 48px;
  border-radius: 12px;
  background: #f5f7fa;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.date-picker :deep(.el-date-editor:hover) {
  background: #edf2f7;
  border-color: rgba(255, 107, 107, 0.2);
}

.date-picker :deep(.el-date-editor.is-focus) {
  background: #fff;
  border-color: #FF6B6B;
  box-shadow: 0 0 0 3px rgba(255, 107, 107, 0.15);
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 40px;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  border: none;
  color: #fff;
  margin-top: 10px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.login-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

.login-btn:hover::before {
  left: 100%;
}

.login-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 24px rgba(255, 107, 107, 0.35);
}

.login-btn:active {
  transform: translateY(-1px);
}

.login-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
}

.login-footer .text {
  color: var(--text-secondary);
}

.login-footer :deep(.el-link) {
  color: #FF6B6B;
  font-weight: 500;
  margin-left: 4px;
}

.login-footer :deep(.el-link:hover) {
  text-decoration: underline;
}

/* 响应式 */
@media (max-width: 768px) {
  .login-page {
    padding: 15px;
  }
  
  .login-box {
    width: 90%;
    max-width: none;
    padding: 1.5rem;
    border-radius: 20px;
    max-height: 85vh;
  }
  
  .login-form :deep(.el-input__wrapper),
  .gender-select :deep(.el-select__wrapper),
  .date-picker :deep(.el-date-editor) {
    height: 44px;
  }
  
  .login-btn {
    height: 44px;
    font-size: 15px;
  }
  
  .decoration {
    font-size: 32px;
  }
  
  .cloud {
    font-size: 48px;
  }
}
</style>
