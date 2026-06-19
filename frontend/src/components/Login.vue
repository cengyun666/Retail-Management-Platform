<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { loginUser, getUserByToken } from '../api/users'
import { ROLES } from '../utils/roles.js'

const username = ref('')
const password = ref('')
const isLoading = ref(false)
const errorMessage = ref('')
const router = useRouter()

// 根据用户角色获取默认重定向路径
const getDefaultRouteByRole = (role) => {
  switch (role) {
    case ROLES.ADMIN:
      return '/home/users'  // 管理员默认进入用户管理页面
    case ROLES.MERCHANT:
      return '/home/goods'  // 商家默认进入商品管理页面
    case ROLES.USER:
      return '/home/goods'  // 普通用户默认进入商品浏览页面
    default:
      return '/home/goods'  // 默认进入商品页面
  }
}

const onLogin = async () => {
    if (!username.value.trim() || !password.value.trim()) {
        errorMessage.value = '请输入用户名和密码'
        return
    }
    
    isLoading.value = true
    errorMessage.value = ''
    
    try {
        const response = await loginUser({
            username: username.value,
            password: password.value
        })
        
        if (response.code === 200) {
            // 登录成功，先保存token
            if (response.data.token) {
                localStorage.setItem('token', response.data.token)
            }
            
            // 使用token获取用户信息
            try {
                const userResponse = await getUserByToken(response.data.token)
                if (userResponse.code === 200) {
                    // 保存用户信息，确保角色为小写
                    const userInfo = {
                        ...userResponse.data,
                        role: userResponse.data.role ? userResponse.data.role.toLowerCase() : ROLES.USER,
                        loginTime: new Date().toLocaleString()
                    }
                    localStorage.setItem('userInfo', JSON.stringify(userInfo))
                    
                    // 先将isLoading设置为false，再进行路由跳转
                    isLoading.value = false
                    
                    // 根据用户角色重定向到不同页面
                    // 将后端返回的大写角色转换为前端定义的小写角色
                    const userRole = userResponse.data.role ? userResponse.data.role.toLowerCase() : ROLES.USER
                    const redirectPath = getDefaultRouteByRole(userRole)
                    router.push(redirectPath)
                } else {
                    // 获取用户信息失败
                    errorMessage.value = userResponse.message || '获取用户信息失败'
                    localStorage.removeItem('userInfo')
                    localStorage.removeItem('token')
                }
            } catch (userError) {
                console.error('获取用户信息失败:', userError)
                errorMessage.value = '获取用户信息失败，请重试'
                localStorage.removeItem('userInfo')
                localStorage.removeItem('token')
            }
        } else {
            // 登录失败，显示错误信息
            errorMessage.value = response.message
            localStorage.removeItem('userInfo')
            localStorage.removeItem('token')
        }
    } catch (error) {
        console.error('登录失败:', error)
        errorMessage.value = '登录失败，请重试'
    } finally {
        isLoading.value = false
    }
}

// 回车键登录
const handleKeyPress = (event) => {
    if (event.key === 'Enter') {
        onLogin()
    }
}

// 清除错误信息
const clearError = () => {
    errorMessage.value = ''
}
</script>

<template>
    <div class="login-container">
        <!-- 背景装饰 -->
        <div class="background-decoration">
            <div class="shape shape-1"></div>
            <div class="shape shape-2"></div>
            <div class="shape shape-3"></div>
        </div>
        
        <div class="login-box">
            <!-- 头部区域 -->
            <div class="login-header">
                <div class="logo">
                    <div class="logo-icon">🔐</div>
                    <h1>商超管理系统</h1>
                </div>
                <p class="welcome-text">欢迎回来，请登录您的账户</p>
            </div>
            
            <!-- 表单区域 -->
            <div class="form-login">
                <!-- 错误提示 -->
                <div v-if="errorMessage" class="error-message">
                    <span class="error-icon">⚠️</span>
                    {{ errorMessage }}
                </div>
                
                <!-- 登录名称 -->
                <div class="form-group">
                    <label for="username">用户名</label>
                    <div class="input-wrapper">
                        <span class="input-icon">👤</span>
                        <input 
                            type="text" 
                            id="username" 
                            placeholder="请输入用户名" 
                            autocomplete="username"
                            v-model.trim="username"
                            @input="clearError"
                            @keypress="handleKeyPress"
                            :disabled="isLoading"
                        />
                    </div>
                </div>
                
                <!-- 登录密码 -->
                <div class="form-group">
                    <label for="password">密码</label>
                    <div class="input-wrapper">
                        <span class="input-icon">🔒</span>
                        <input 
                            type="password" 
                            id="password" 
                            placeholder="请输入密码" 
                            autocomplete="current-password"
                            v-model="password"
                            @input="clearError"
                            @keypress="handleKeyPress"
                            :disabled="isLoading"
                        />
                    </div>
                </div>
                
                <!-- 登录按钮 -->
                <div class="form-group">
                    <button 
                        type="button" 
                        class="login-btn" 
                        @click="onLogin"
                        :disabled="isLoading || !username.trim() || !password.trim()"
                    >
                        <span v-if="isLoading" class="loading-spinner"></span>
                        {{ isLoading ? '登录中...' : '登录' }}
                    </button>
                </div>
                
                <!-- 注册链接 -->
                <div class="form-group register-link">
                    <p>还没有账户？ <router-link to="/register" class="register-text">立即注册</router-link></p>
                </div>
            </div>
            
            <!-- 底部信息 -->
            <div class="login-footer">
                <p>© 2025 商超管理系统. All rights reserved.</p>
            </div>
        </div>
    </div>
</template>

<style scoped>
.login-container {
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #1e90ff 0%, #f0f8ff 100%);
    position: relative;
    overflow: hidden;
}

.background-decoration {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 0;
}

.shape {
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.1);
}

.shape-1 {
    width: 200px;
    height: 200px;
    top: -100px;
    right: -50px;
}

.shape-2 {
    width: 150px;
    height: 150px;
    bottom: -75px;
    left: -50px;
}

.shape-3 {
    width: 100px;
    height: 100px;
    top: 50%;
    right: 20%;
}

.login-box {
    background: white;
    border-radius: 16px;
    padding: 40px;
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
    width: 100%;
    max-width: 420px;
    position: relative;
    z-index: 1;
    animation: slideUp 0.6s ease-out;
}

@keyframes slideUp {
    from {
        opacity: 0;
        transform: translateY(30px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.login-header {
    text-align: center;
    margin-bottom: 32px;
}

.logo {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    margin-bottom: 12px;
}

.logo-icon {
    font-size: 32px;
}

.logo h1 {
    margin: 0;
    color: #333;
    font-size: 28px;
    font-weight: 600;
}

.welcome-text {
    margin: 0;
    color: #666;
    font-size: 14px;
}

.form-login {
    margin-bottom: 24px;
}

.error-message {
    background-color: #fef2f2;
    border: 1px solid #fecaca;
    color: #dc2626;
    padding: 12px 16px;
    border-radius: 8px;
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
}

.error-icon {
    font-size: 16px;
}

.form-group {
    margin-bottom: 20px;
}

.form-group label {
    display: block;
    margin-bottom: 8px;
    color: #333;
    font-weight: 500;
    font-size: 14px;
}

.input-wrapper {
    position: relative;
    display: flex;
    align-items: center;
}

.input-icon {
    position: absolute;
    left: 12px;
    font-size: 16px;
    z-index: 1;
}

.form-group input {
    width: 100%;
    padding: 12px 12px 12px 40px;
    border: 2px solid #e5e7eb;
    border-radius: 8px;
    font-size: 14px;
    transition: all 0.3s ease;
    background-color: #fafafa;
}

.form-group input:focus {
    outline: none;
    border-color: #1e90ff;
    background-color: white;
    box-shadow: 0 0 0 3px rgba(30, 144, 255, 0.1);
}

.form-group input:disabled {
    background-color: #f3f4f6;
    cursor: not-allowed;
    opacity: 0.7;
}

.login-btn {
    width: 100%;
    padding: 14px;
    background: linear-gradient(135deg, #1e90ff 0%, #4169e1 100%);
    color: white;
    border: none;
    border-radius: 8px;
    font-size: 16px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
}

.login-btn:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 10px 20px rgba(30, 144, 255, 0.3);
}

.login-btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
    transform: none;
}

.loading-spinner {
    width: 16px;
    height: 16px;
    border: 2px solid transparent;
    border-top: 2px solid white;
    border-radius: 50%;
    animation: spin 1s linear infinite;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}

.register-link {
    text-align: center;
    margin-top: 10px;
}

.register-link p {
    margin: 0;
    color: #666;
    font-size: 14px;
}

.register-text {
    color: #1e90ff;
    text-decoration: none;
    font-weight: 500;
    transition: color 0.3s ease;
}

.register-text:hover {
    color: #4169e1;
    text-decoration: underline;
}

.login-tips {
    text-align: center;
    margin-top: 20px;
    padding: 12px;
    background-color: #f0f9ff;
    border-radius: 8px;
    border: 1px solid #e0f2fe;
}

.login-tips p {
    margin: 0;
    color: #0369a1;
    font-size: 13px;
    font-weight: 500;
}

.login-footer {
    text-align: center;
    border-top: 1px solid #f0f0f0;
    padding-top: 20px;
}

.login-footer p {
    margin: 0;
    color: #999;
    font-size: 12px;
}

/* 响应式设计 */
@media (max-width: 480px) {
    .login-box {
        margin: 20px;
        padding: 30px 24px;
    }
    
    .logo h1 {
        font-size: 24px;
    }
    
    .logo-icon {
        font-size: 28px;
    }
}
</style>