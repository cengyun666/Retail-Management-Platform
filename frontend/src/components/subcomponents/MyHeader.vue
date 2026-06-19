<template>
    <header class="layout-header-container">
        <div class="layout-header-left">
            <div class="logo-container">
                <h1 class="layout-header-left-title">零食业务管理平台</h1>
            </div>
        </div>
        <div class="layout-header-right">
            <!-- 用户信息 -->
            <div class="user-info">
                <div class="user-avatar">
                    <span class="avatar-icon">👤</span>
                </div>
                <div class="user-details">
                    <span class="username">{{ userInfo.username || '管理员' }}</span>
                    <span class="login-time">{{ userInfo.loginTime || '已登录' }}</span>
                </div>
            </div>
            <!-- 用户操作下拉框 -->
            <el-dropdown trigger="click" @command="handleDropdownCommand">
                <div class="dropdown-btn">
                    <span class="dropdown-text">操作</span>
                    <el-icon class="el-icon--right"><arrow-down /></el-icon>
                </div>
                <template #dropdown>
                    <el-dropdown-menu>
                        <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                        <el-dropdown-item command="address">收货地址</el-dropdown-item>
                        <el-dropdown-item command="logout">退出</el-dropdown-item>
                    </el-dropdown-menu>
                </template>
            </el-dropdown>
        </div>
        
        <!-- 退出确认对话框 -->
        <div v-if="showConfirmDialog" class="confirm-dialog-overlay" @click="hideLogoutConfirm">
            <div class="confirm-dialog" @click.stop>
                <div class="dialog-header">
                    <span class="dialog-icon">⚠️</span>
                    <h3 class="dialog-title">确认退出</h3>
                </div>
                <div class="dialog-content">
                    <p>确定要退出系统吗？</p>
                </div>
                <div class="dialog-actions">
                    <button class="btn-cancel" @click="hideLogoutConfirm">取消</button>
                    <button class="btn-confirm" @click="onLogout">确定退出</button>
                </div>
            </div>
        </div>
    </header>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ref, onMounted } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()
const showConfirmDialog = ref(false)
const userInfo = ref({})

onMounted(() => {
  // 获取用户信息
  try {
    const storedUserInfo = localStorage.getItem('userInfo')
    if (storedUserInfo) {
      userInfo.value = JSON.parse(storedUserInfo)
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
})

const handleDropdownCommand = (command) => {
  if (command === 'logout') {
    showLogoutConfirm()
  } else if (command === 'profile') {
    router.push('/home/profile')
  } else if (command === 'address') {
    router.push('/home/address')
  }
}

const showLogoutConfirm = () => {
  showConfirmDialog.value = true
}

const hideLogoutConfirm = () => {
  showConfirmDialog.value = false
}

const onLogout = () => {
  localStorage.removeItem('userInfo')
  router.push('/login')
}
</script>

<style scoped>
.layout-header-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 24px;
  background: linear-gradient(135deg, #1e90ff 0%, #4169e1 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 100;
}

.layout-header-left {
  display: flex;
  align-items: center;
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.layout-header-left-img {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  object-fit: cover;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.layout-header-left-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.layout-header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  backdrop-filter: blur(10px);
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-icon {
  font-size: 18px;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.username {
  font-size: 14px;
  font-weight: 500;
}

.login-time {
  font-size: 12px;
  opacity: 0.8;
}

/* 下拉框样式 */
.dropdown-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  color: white;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.dropdown-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.dropdown-text {
  white-space: nowrap;
}

/* 确认对话框样式 */
.confirm-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

.confirm-dialog {
  background: white;
  border-radius: 12px;
  padding: 24px;
  max-width: 400px;
  width: 90%;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
  animation: slideUp 0.3s ease;
}

.dialog-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.dialog-icon {
  font-size: 24px;
}

.dialog-title {
  margin: 0;
  color: #1e293b;
  font-size: 18px;
  font-weight: 600;
}

.dialog-content {
  margin-bottom: 24px;
}

.dialog-content p {
  margin: 0;
  color: #64748b;
  font-size: 14px;
  line-height: 1.5;
}

.dialog-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.btn-cancel, .btn-confirm {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-cancel {
  background: #f1f5f9;
  color: #64748b;
}

.btn-cancel:hover {
  background: #e2e8f0;
}

.btn-confirm {
  background: #ef4444;
  color: white;
}

.btn-confirm:hover {
  background: #dc2626;
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .layout-header-container {
    padding: 0 16px;
  }
  
  .layout-header-left-title {
    font-size: 18px;
  }
  
  .user-details {
    display: none;
  }
  
  .dropdown-text {
    display: none;
  }
  
  .dropdown-btn {
    padding: 8px;
    min-width: 40px;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .layout-header-container {
    padding: 0 12px;
  }
  
  .layout-header-left-title {
    font-size: 16px;
  }
  
  .layout-header-left-img {
    width: 32px;
    height: 32px;
  }
  
  .user-info {
    padding: 6px 12px;
  }
  
  .user-avatar {
    width: 32px;
    height: 32px;
  }
}
</style>