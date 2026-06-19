<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
    id: String
})

const router = useRouter()
const userDetail = ref(null)
const loading = ref(true)
const error = ref(null)

// 模拟用户数据
const mockUsers = [
    { id: '1', name: '小明' },
    { id: '2', name: '小红' },
    { id: '3', name: '小兰' },
    { id: '4', name: '小芳' }
]

// 加载用户详情
const loadUserDetail = () => {
    loading.value = true
    error.value = null
    
    // 模拟API请求延迟
    setTimeout(() => {
        try {
            const user = mockUsers.find(u => u.id === props.id)
            if (user) {
                userDetail.value = user
            } else {
                error.value = '用户不存在'
            }
        } catch (err) {
            error.value = '加载失败，请重试'
        } finally {
            loading.value = false
        }
    }, 300)
}

// 返回上一页
const goBack = () => {
    router.back()
}

// 组件挂载时加载数据
onMounted(() => {
    loadUserDetail()
})
</script>

<template>
    <div class="user-detail-container">
        <!-- 顶部操作栏 -->
        <div class="header-actions">
            <button class="btn btn-default" @click="goBack">
                ← 后退
            </button>
        </div>
        
        <!-- 页面标题 -->
        <h2 class="page-title">用户详情</h2>
        
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
            <div class="spinner"></div>
            <p>加载中...</p>
        </div>
        
        <!-- 错误状态 -->
        <div v-else-if="error" class="error-state">
            <div class="error-icon">⚠️</div>
            <p>{{ error }}</p>
            <button class="btn btn-primary" @click="loadUserDetail">重试</button>
        </div>
        
        <!-- 用户详情卡片 -->
        <div v-else-if="userDetail" class="user-card">
            <div class="user-header">
                <div class="user-avatar">{{ userDetail.name.charAt(0) }}</div>
                <div class="user-basic-info">
                    <h3>{{ userDetail.name }}</h3>
                </div>
            </div>
            
            <div class="user-details">
                <div class="detail-item">
                    <label>用户编号：</label>
                    <span>{{ userDetail.id }}</span>
                </div>
                <div class="detail-item">
                    <label>姓名：</label>
                    <span>{{ userDetail.name }}</span>
                </div>
                <!-- <div class="detail-item">
                    <label>邮箱：</label>
                    <span>{{ userDetail.email }}</span>
                </div>
                <div class="detail-item">
                    <label>电话：</label>
                    <span>{{ userDetail.phone }}</span>
                </div>
                <div class="detail-item">
                    <label>地址：</label>
                    <span>{{ userDetail.address }}</span>
                </div> -->
            </div>
        </div>
        
        <!-- 空状态 -->
        <div v-else class="empty-state">
            <p>暂无用户数据</p>
        </div>
    </div>
</template>

<style scoped>
.user-detail-container {
    max-width: 800px;
    margin: 0 auto;
    padding: 20px;
}

.header-actions {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
}

.btn {
    padding: 8px 16px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 14px;
    transition: all 0.3s ease;
}

.btn:hover {
    opacity: 0.9;
    transform: translateY(-1px);
}

.btn-default {
    background-color: #f0f0f0;
    color: #333;
}

.btn-primary {
    background-color: #007bff;
    color: white;
}

.page-title {
    margin-bottom: 30px;
    color: #333;
    font-size: 24px;
}

/* 加载状态 */
.loading-state {
    text-align: center;
    padding: 60px 20px;
}

.spinner {
    width: 40px;
    height: 40px;
    border: 4px solid #f3f3f3;
    border-top: 4px solid #007bff;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin: 0 auto 15px;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}

/* 错误状态 */
.error-state {
    text-align: center;
    padding: 60px 20px;
    background-color: #f8d7da;
    border: 1px solid #f5c6cb;
    border-radius: 8px;
    color: #721c24;
}

.error-icon {
    font-size: 48px;
    margin-bottom: 15px;
}

/* 用户卡片 */
.user-card {
    background-color: white;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    overflow: hidden;
}

.user-header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 30px;
    display: flex;
    align-items: center;
    gap: 20px;
}

.user-avatar {
    width: 80px;
    height: 80px;
    background-color: rgba(255, 255, 255, 0.2);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32px;
    font-weight: bold;
}
.user-basic-info h3 {
    margin: 0;
    font-size: 24px;
}

.user-details {
    padding: 30px;
}

.detail-item {
    display: flex;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;
}

.detail-item:last-child {
    border-bottom: none;
}

.detail-item label {
    width: 100px;
    color: #666;
    font-weight: 500;
}

.detail-item span {
    color: #333;
    flex: 1;
}

/* 空状态 */
.empty-state {
    text-align: center;
    padding: 60px 20px;
    color: #999;
}
</style>






