<template>
  <div class="user-detail">
    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <span>用户详情</span>
          <el-button type="primary" @click="goBack">返回</el-button>
        </div>
      </template>
      
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>
      
      <div v-else-if="user" class="user-info">
        <div class="user-avatar">
          <el-avatar :size="120" :src="user.avatar" fit="cover">
            {{ user.name.substring(0, 1) }}
          </el-avatar>
          <div class="user-name">{{ user.name }}</div>
          <div class="user-role">
            <el-tag :type="user.role === 'admin' ? 'danger' : 'primary'">
              {{ user.role === 'admin' ? '管理员' : '普通用户' }}
            </el-tag>
          </div>
        </div>
        
        <div class="user-details">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="用户ID">{{ user.id }}</el-descriptions-item>
            <el-descriptions-item label="用户名">{{ user.name }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ user.email }}</el-descriptions-item>
            <el-descriptions-item label="电话">{{ user.phone }}</el-descriptions-item>
          </el-descriptions>
          
          <div class="user-actions">
            <el-button type="danger" @click="handleDeleteUser">删除用户</el-button>
          </div>
        </div>
      </div>
      
      <div v-else class="empty-container">
        <el-empty description="用户不存在" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserById, deleteUser } from '../../api/users'

const route = useRoute()
const router = useRouter()

const user = ref(null)
const loading = ref(true)

// 获取用户详情
const fetchUserDetail = async () => {
  try {
    loading.value = true
    const userId = route.params.id
    const response = await getUserById(userId)
    
    if (response.code === 200) {
      user.value = response.data
    } else {
      ElMessage.error(response.message || '获取用户详情失败')
    }
  } catch (error) {
    console.error('获取用户详情失败:', error)
    ElMessage.error('获取用户详情失败')
  } finally {
    loading.value = false
  }
}

// 返回用户列表
const goBack = () => {
  router.push('/home/users')
}

// 删除用户
const handleDeleteUser = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 ${user.value.name} 吗？此操作不可恢复！`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await deleteUser(user.value.id)
    
    if (response.code === 200) {
      ElMessage.success('用户删除成功')
      router.push('/home/users')
    } else {
      ElMessage.error(response.message || '删除用户失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchUserDetail()
})
</script>

<style scoped>
.user-detail {
  padding: 20px;
}

.detail-card {
  max-width: 1000px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.loading-container {
  padding: 20px;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.user-avatar {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.user-name {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.user-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
  margin-top: 20px;
}

.empty-container {
  padding: 40px;
  text-align: center;
}

@media (max-width: 768px) {
  .user-detail {
    padding: 10px;
  }
  
  .user-info {
    gap: 20px;
  }
  
  .user-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .user-actions .el-button {
    width: 200px;
  }
}
</style>