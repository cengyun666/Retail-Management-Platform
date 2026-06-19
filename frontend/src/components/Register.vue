<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { registerUser } from '../api/users'

const router = useRouter()

// 表单数据
const formData = ref({
    username: '',
    password: '',
    confirmPassword: '',
    name: '',
    email: '',
    phone: '',
    address: ''
})

// 表单验证错误
const formErrors = ref({})

// 状态
const isLoading = ref(false)
const registerSuccess = ref(false)

// 表单验证
const validateForm = () => {
    formErrors.value = {}
    let isValid = true
    
    // 用户名验证
    if (!formData.value.username.trim()) {
        formErrors.value.username = '请输入用户名'
        isValid = false
    } else if (formData.value.username.length < 3) {
        formErrors.value.username = '用户名至少需要3个字符'
        isValid = false
    } else if (!/^[a-zA-Z0-9_]+$/.test(formData.value.username)) {
        formErrors.value.username = '用户名只能包含字母、数字和下划线'
        isValid = false
    }
    
    // 密码验证
    if (!formData.value.password) {
        formErrors.value.password = '请输入密码'
        isValid = false
    } else if (formData.value.password.length < 6) {
        formErrors.value.password = '密码至少需要6个字符'
        isValid = false
    }
    
    // 确认密码验证
    if (!formData.value.confirmPassword) {
        formErrors.value.confirmPassword = '请确认密码'
        isValid = false
    } else if (formData.value.password !== formData.value.confirmPassword) {
        formErrors.value.confirmPassword = '两次输入的密码不一致'
        isValid = false
    }
    
    // 姓名验证（必填）
    if (!formData.value.name.trim()) {
        formErrors.value.name = '请输入姓名'
        isValid = false
    } else if (formData.value.name.length < 2) {
        formErrors.value.name = '姓名至少需要2个字符'
        isValid = false
    }
    
    // 邮箱验证（非必填）
    if (formData.value.email.trim() && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.value.email)) {
        formErrors.value.email = '请输入有效的邮箱地址'
        isValid = false
    }
    
    // 手机号验证（非必填）
    if (formData.value.phone.trim() && !/^1[3-9]\d{9}$/.test(formData.value.phone)) {
        formErrors.value.phone = '请输入有效的手机号码'
        isValid = false
    }
    
    // 地址验证（非必填）
    if (formData.value.address.trim() && formData.value.address.length < 5) {
        formErrors.value.address = '地址至少需要5个字符'
        isValid = false
    }
    
    return isValid
}

// 提交注册
const handleRegister = async () => {
    if (!validateForm() || isLoading.value) return
    
    isLoading.value = true
    
    try {
        // 准备提交数据（不包含确认密码）
        const { confirmPassword, ...submitData } = formData.value
        
        // 为非必填字段提供默认值，确保数据库插入成功
        // 提供基于用户名的唯一默认邮箱，避免邮箱重复问题
        if (!submitData.email || submitData.email === '') submitData.email = `${submitData.username}@example.com`
        if (!submitData.phone) submitData.phone = '13800138000'  // 提供默认的有效手机号
        if (!submitData.address) submitData.address = '未知地址123'  // 提供默认的有效地址（至少5个字符）
        
        // 调用注册API
        const response = await registerUser(submitData)
        
        if (response.code === 200) {
            registerSuccess.value = true
            
            // 3秒后跳转到登录页面
            setTimeout(() => {
                router.push('/login')
            }, 3000)
        } else {
            // 显示服务器返回的错误信息
            formErrors.value.submit = response.message
        }
    } catch (error) {
        console.error('注册失败:', error)
        formErrors.value.submit = '注册失败，请重试'
    } finally {
        isLoading.value = false
    }
}

// 重置表单
const resetForm = () => {
    formData.value = {
        username: '',
        password: '',
        confirmPassword: '',
        name: '',
        email: '',
        phone: '',
        address: ''
    }
    formErrors.value = {}
}

// 跳转到登录页面
const goToLogin = () => {
    router.push('/login')
}

// 清除特定字段的错误
const clearFieldError = (field) => {
    if (formErrors.value[field]) {
        formErrors.value[field] = ''
    }
}
</script>

<template>
    <div class="register-container">
        <!-- 背景装饰 -->
        <div class="background-decoration">
            <div class="shape shape-1"></div>
            <div class="shape shape-2"></div>
            <div class="shape shape-3"></div>
        </div>
        
        <div class="register-box">
            <!-- 头部区域 -->
            <div class="register-header">
                <div class="logo">
                    <div class="logo-icon">🔐</div>
                    <h1>商超管理系统</h1>
                </div>
                <p class="welcome-text">创建您的账户，加入我们的系统</p>
            </div>
            
            <!-- 注册成功提示 -->
            <div v-if="registerSuccess" class="success-message">
                <div class="success-icon">✅</div>
                <h3>注册成功！</h3>
                <p>将在3秒后跳转到登录页面...</p>
                <button class="login-btn" @click="goToLogin">立即登录</button>
            </div>
            
            <!-- 表单区域 -->
            <div v-else class="form-register">
                <!-- 全局错误提示 -->
                <div v-if="formErrors.submit" class="error-message">
                    <span class="error-icon">⚠️</span>
                    {{ formErrors.submit }}
                </div>
                
                <!-- 用户名 -->
                <div class="form-group">
                    <label for="username">用户名 *</label>
                    <div class="input-wrapper">
                        <span class="input-icon">👤</span>
                        <input 
                            type="text" 
                            id="username" 
                            placeholder="请输入用户名" 
                            autocomplete="username"
                            v-model.trim="formData.username"
                            @input="clearFieldError('username')"
                            :disabled="isLoading"
                        />
                    </div>
                    <div v-if="formErrors.username" class="field-error">{{ formErrors.username }}</div>
                </div>
                
                <!-- 密码 -->
                <div class="form-group">
                    <label for="password">密码 *</label>
                    <div class="input-wrapper">
                        <span class="input-icon">🔒</span>
                        <input 
                            type="password" 
                            id="password" 
                            placeholder="请输入密码" 
                            autocomplete="new-password"
                            v-model="formData.password"
                            @input="clearFieldError('password')"
                            :disabled="isLoading"
                        />
                    </div>
                    <div v-if="formErrors.password" class="field-error">{{ formErrors.password }}</div>
                </div>
                
                <!-- 确认密码 -->
                <div class="form-group">
                    <label for="confirmPassword">确认密码 *</label>
                    <div class="input-wrapper">
                        <span class="input-icon">🔒</span>
                        <input 
                            type="password" 
                            id="confirmPassword" 
                            placeholder="请再次输入密码" 
                            autocomplete="new-password"
                            v-model="formData.confirmPassword"
                            @input="clearFieldError('confirmPassword')"
                            :disabled="isLoading"
                        />
                    </div>
                    <div v-if="formErrors.confirmPassword" class="field-error">{{ formErrors.confirmPassword }}</div>
                </div>
                
                <!-- 姓名 -->
                <div class="form-group">
                    <label for="name">姓名 *</label>
                    <div class="input-wrapper">
                        <span class="input-icon">👤</span>
                        <input 
                            type="text" 
                            id="name" 
                            placeholder="请输入姓名" 
                            autocomplete="name"
                            v-model.trim="formData.name"
                            @input="clearFieldError('name')"
                            :disabled="isLoading"
                        />
                    </div>
                    <div v-if="formErrors.name" class="field-error">{{ formErrors.name }}</div>
                </div>
                
                <!-- 邮箱 -->
                <div class="form-group">
                    <label for="email">邮箱</label>
                    <div class="input-wrapper">
                        <span class="input-icon">📧</span>
                        <input 
                            type="email" 
                            id="email" 
                            placeholder="请输入邮箱（可选）" 
                            autocomplete="email"
                            v-model.trim="formData.email"
                            @input="clearFieldError('email')"
                            :disabled="isLoading"
                        />
                    </div>
                    <div v-if="formErrors.email" class="field-error">{{ formErrors.email }}</div>
                </div>
                
                <!-- 手机号 -->
                <div class="form-group">
                    <label for="phone">手机号</label>
                    <div class="input-wrapper">
                        <span class="input-icon">📱</span>
                        <input 
                            type="tel" 
                            id="phone" 
                            placeholder="请输入手机号（可选）" 
                            autocomplete="tel"
                            v-model.trim="formData.phone"
                            @input="clearFieldError('phone')"
                            :disabled="isLoading"
                        />
                    </div>
                    <div v-if="formErrors.phone" class="field-error">{{ formErrors.phone }}</div>
                </div>
                
                <!-- 地址 -->
                <div class="form-group">
                    <label for="address">地址</label>
                    <div class="input-wrapper">
                        <span class="input-icon">📍</span>
                        <input 
                            type="text" 
                            id="address" 
                            placeholder="请输入地址（可选）" 
                            autocomplete="street-address"
                            v-model.trim="formData.address"
                            @input="clearFieldError('address')"
                            :disabled="isLoading"
                        />
                    </div>
                    <div v-if="formErrors.address" class="field-error">{{ formErrors.address }}</div>
                </div>
                
                <!-- 注册按钮 -->
                <div class="form-group">
                    <button 
                        type="button" 
                        class="register-btn" 
                        @click="handleRegister"
                        :disabled="isLoading"
                    >
                        <span v-if="isLoading" class="loading-spinner"></span>
                        {{ isLoading ? '注册中...' : '注册' }}
                    </button>
                </div>
                
                <!-- 重置按钮 -->
                <div class="form-group reset-link">
                    <button 
                        type="button" 
                        class="reset-btn" 
                        @click="resetForm"
                        :disabled="isLoading"
                    >
                        重置表单
                    </button>
                </div>
            </div>
            
            <!-- 底部信息 -->
            <div class="register-footer">
                <p>© 2025 商超管理系统. All rights reserved.</p>
                <p class="login-link">已有账户？<button class="go-to-login" @click="goToLogin">返回登录</button></p>
            </div>
        </div>
    </div>
</template>

<style scoped>
.register-container {
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #1e90ff 0%, #f0f8ff 100%);
    position: relative;
    overflow: hidden;
    padding: 20px 0;
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

.register-box {
    background: white;
    border-radius: 16px;
    padding: 40px;
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
    width: 100%;
    max-width: 500px;
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

.register-header {
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

.success-message {
    text-align: center;
    padding: 40px 20px;
}

.success-icon {
    font-size: 48px;
    margin-bottom: 16px;
}

.success-message h3 {
    margin: 0 0 16px;
    color: #52c41a;
    font-size: 24px;
}

.success-message p {
    margin: 0 0 24px;
    color: #666;
    font-size: 16px;
}

.form-register {
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

.input-wrapper,
.select-wrapper {
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

.form-group input,
.form-group select {
    width: 100%;
    padding: 12px 12px 12px 40px;
    border: 2px solid #e5e7eb;
    border-radius: 8px;
    font-size: 14px;
    transition: all 0.3s ease;
    background-color: #fafafa;
    box-sizing: border-box;
}

.form-group select {
    cursor: pointer;
}

.form-group input:focus,
.form-group select:focus {
    outline: none;
    border-color: #1e90ff;
    background-color: white;
    box-shadow: 0 0 0 3px rgba(30, 144, 255, 0.1);
}

.form-group input:disabled,
.form-group select:disabled {
    background-color: #f3f4f6;
    cursor: not-allowed;
    opacity: 0.7;
}

.field-error {
    color: #dc2626;
    font-size: 12px;
    margin-top: 4px;
}

.register-btn {
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

.register-btn:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 10px 20px rgba(30, 144, 255, 0.3);
}

/* 登录按钮样式，与注册按钮一致 */
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

.register-btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
    transform: none;
}

.reset-link {
    text-align: center;
    margin-top: 10px;
}

.reset-btn {
    background: none;
    border: none;
    color: #1e90ff;
    text-decoration: none;
    font-weight: 500;
    transition: color 0.3s ease;
    padding: 0;
    cursor: pointer;
    font-size: 14px;
}

.reset-btn:hover:not(:disabled) {
    color: #4169e1;
    text-decoration: underline;
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

.register-footer {
    text-align: center;
    border-top: 1px solid #f0f0f0;
    padding-top: 20px;
}

.register-footer p {
    margin: 0;
    color: #999;
    font-size: 12px;
    margin-bottom: 8px;
}

.login-link {
    margin-top: 12px;
    font-size: 14px;
}

.go-to-login {
    background: none;
    border: none;
    color: #1e90ff;
    text-decoration: none;
    font-weight: 500;
    cursor: pointer;
    font-size: 14px;
    padding: 0;
    transition: color 0.3s ease;
}

.go-to-login:hover {
    color: #4169e1;
    text-decoration: underline;
}

/* 已有账户链接在登录页面处理 */

/* 响应式设计 */
@media (max-width: 480px) {
    .register-box {
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