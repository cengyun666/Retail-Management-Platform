<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserByToken, updateUserInfo } from '../../api/users.js'

// 用户信息数据
const userInfo = ref({
  id: '',
  username: '',
  name: '',
  role: '',
  email: '',
  phone: '',
  address: ''
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: false, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: false, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3456789]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  address: [
    { required: false, message: '请输入地址', trigger: 'blur' },
    { min: 5, max: 100, message: '地址长度在 5 到 100 个字符', trigger: 'blur' }
  ]
}

// 表单引用
const formRef = ref(null)

// 初始化用户信息
onMounted(() => {
  loadUserInfo()
})

// 加载用户信息
function loadUserInfo() {
  // 从localStorage获取用户信息
  const storedUserInfo = localStorage.getItem('userInfo')
  if (storedUserInfo) {
    const parsedUser = JSON.parse(storedUserInfo)
    userInfo.value = {
      ...userInfo.value,
      ...parsedUser
    }
  }
  
  // 使用token获取最新用户信息
  const token = localStorage.getItem('token')
  if (token) {
    getUserByToken(token).then(response => {
      if (response.code === 200 && response.data) {
        const { id, username, name, role, email, phone, address } = response.data
        userInfo.value = {
          id,
          username,
          name: name || '',
          role,
          email: email || '',
          phone: phone || '',
          address: address || ''
        }
        
        // 更新localStorage
        const updatedUserInfo = {
          ...JSON.parse(localStorage.getItem('userInfo') || '{}'),
          id,
          username,
          name: name || '',
          role,
          email: email || '',
          phone: phone || '',
          address: address || ''
        }
        localStorage.setItem('userInfo', JSON.stringify(updatedUserInfo))
      }
    }).catch(error => {
      console.error('获取用户信息失败:', error)
      ElMessage.error('获取用户信息失败')
    })
  }
}

// 保存用户信息
function saveUserInfo() {
  if (!formRef.value) return
  
  formRef.value.validate((valid) => {
    if (valid) {
      // 准备更新数据
      const updateData = {
        name: userInfo.value.name,
        email: userInfo.value.email,
        phone: userInfo.value.phone,
        address: userInfo.value.address
      }
      
      // 调用API更新用户信息
      updateUserInfo(userInfo.value.id, updateData).then(response => {
        if (response.code === 200) {
          ElMessage.success('保存成功')
        } else {
          ElMessage.error(response.message || '保存失败')
        }
      }).catch(error => {
        console.error('更新用户信息失败:', error)
        ElMessage.error('保存失败')
      })
    } else {
      ElMessage.warning('请确保填写的信息格式正确')
    }
  })
}

// 重置表单
function resetForm() {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  // 重新加载用户信息
  loadUserInfo()
}
</script>

<template>
  <div class="profile-management-container">
    <h2>个人信息</h2>
    <el-card shadow="hover" class="profile-card">
      <el-form ref="formRef" :model="userInfo" :rules="rules" label-width="120px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userInfo.username" disabled />
        </el-form-item>
        <el-form-item label="角色">
          <el-input v-model="userInfo.role" disabled />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="userInfo.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userInfo.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userInfo.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="userInfo.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveUserInfo">保存修改</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style>
.profile-management-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.profile-card {
  margin-top: 20px;
}

.profile-management-container h2 {
  text-align: center;
  margin-bottom: 20px;
  color: #333;
}
</style>