<template>
  <div class="user-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="showAddUserDialog" v-if="hasPermission(currentUserRole, 'user:create')">
            添加用户
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchQuery"
          placeholder="搜索用户名、姓名或邮箱"
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
          style="width: 300px; margin-right: 10px;"
        >
          <template #append>
            <el-button icon="Search" @click="handleSearch" />
          </template>
        </el-input>
        <el-select v-model="roleFilter" placeholder="角色筛选" clearable style="width: 150px; margin-right: 10px;" @change="handleSearch">
          <el-option label="全部" value="" />
          <el-option label="管理员" value="admin" />
          <el-option label="商家" value="merchant" />
          <el-option label="普通用户" value="user" />
        </el-select>
        <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 150px;" @change="handleSearch">
          <el-option label="全部" value="" />
          <el-option label="正常" value="1" />
          <el-option label="禁用" value="0" />
        </el-select>
      </div>

      <!-- 用户表格 -->
      <el-table :data="paginatedUsers" style="width: 100%" border v-loading="loading">
        <el-table-column prop="id" label="用户ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="phone" label="电话" width="120" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="scope">
            <el-tag :type="getRoleTagType(scope.row.role)">
              {{ getRoleName(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="viewUser(scope.row)">查看</el-button>
            <el-button size="small" type="warning" @click="toggleUserStatus(scope.row)">
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="deleteUser(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :small="false"
          :disabled="loading"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          :total="filteredUsers.length"
          :prev-text="'上一页'"
          :next-text="'下一页'"
          :pager-count="7"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加用户对话框 -->
    <el-dialog
      v-model="addUserDialogVisible"
      title="添加用户"
      width="50%"
      :close-on-click-modal="false"
    >
      <el-form :model="userForm" :rules="userFormRules" ref="userFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="userForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="userForm.address" type="textarea" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择角色">
            <el-option label="普通用户" value="user" />
            <el-option label="商家" value="merchant" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="userForm.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addUserDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="addUser" :loading="submitting">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看用户详情对话框 -->
    <el-dialog
      v-model="viewUserDialogVisible"
      title="用户详情"
      width="50%"
    >
      <el-descriptions :column="2" border v-if="selectedUser">
        <el-descriptions-item label="用户ID">{{ selectedUser.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ selectedUser.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ selectedUser.name }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ selectedUser.email }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{ selectedUser.phone }}</el-descriptions-item>
        <el-descriptions-item label="地址">{{ selectedUser.address || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag :type="getRoleTagType(selectedUser.role)">
            {{ getRoleName(selectedUser.role) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="selectedUser.status === 1 ? 'success' : 'danger'">
            {{ selectedUser.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ selectedUser.createTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="viewUserDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUsersList, registerUser, updateUser, deleteUser as deleteUserApi } from '../api/real-users.js'
import { hasPermission, getRoleName } from '../utils/permissions.js'
import { ROLES } from '../utils/roles.js'

export default {
  name: 'UserManagement',
  setup() {
    const users = ref([])
    const loading = ref(false)
    const submitting = ref(false)
    const addUserDialogVisible = ref(false)
    const viewUserDialogVisible = ref(false)
    const selectedUser = ref(null)
    const userFormRef = ref(null)
    const currentUserRole = ref(ROLES.ADMIN) // 实际应用中应从用户信息中获取

    // 搜索和筛选
    const searchQuery = ref('')
    const roleFilter = ref('')
    const statusFilter = ref('')

    // 分页
    const currentPage = ref(1)
    const pageSize = ref(10)

    // 用户表单
    const userForm = reactive({
      username: '',
      password: '',
      name: '',
      email: '',
      phone: '',
      address: '',
      role: 'user',
      status: 1
    })

    // 表单验证规则
    const userFormRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' },
        { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
      ],
      name: [
        { required: true, message: '请输入姓名', trigger: 'blur' },
        { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
      ],
      email: [
        { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
      ],
      phone: [
        { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
      ],
      address: [
        { min: 5, max: 100, message: '地址长度在 5 到 100 个字符', trigger: 'blur' }
      ],
      role: [
        { required: true, message: '请选择角色', trigger: 'change' }
      ]
    }

    // 获取角色标签类型
    const getRoleTagType = (role) => {
      const typeMap = {
        [ROLES.ADMIN]: 'danger',
        [ROLES.MERCHANT]: 'warning',
        [ROLES.USER]: 'info'
      }
      
      // 处理大写的角色值
      if (role === 'ADMIN') return typeMap[ROLES.ADMIN]
      if (role === 'MERCHANT') return typeMap[ROLES.MERCHANT]
      if (role === 'USER') return typeMap[ROLES.USER]
      
      // 处理可能的数值角色值
      if (role === '1' || role === 1) return typeMap[ROLES.ADMIN]
      if (role === '2' || role === 2) return typeMap[ROLES.MERCHANT]
      if (role === '3' || role === 3) return typeMap[ROLES.USER]
      
      return typeMap[role] || 'info'
    }

    // 获取用户列表
    const fetchUsers = async () => {
      try {
        loading.value = true
        const response = await getUsersList({
          search: searchQuery.value,
          role: roleFilter.value,
          status: statusFilter.value
        })
        
        if (response.code === 200) {
          users.value = response.data
        } else {
          ElMessage.error(response.message || '获取用户列表失败')
        }
      } catch (error) {
        console.error('获取用户列表错误:', error)
        ElMessage.error('获取用户列表失败')
      } finally {
        loading.value = false
      }
    }

    // 过滤用户
    const filteredUsers = computed(() => {
      let result = [...users.value]
      
      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        result = result.filter(user => 
          user.username.toLowerCase().includes(query) ||
          user.name.toLowerCase().includes(query) ||
          user.email.toLowerCase().includes(query)
        )
      }
      
      if (roleFilter.value) {
        result = result.filter(user => user.role === roleFilter.value)
      }
      
      if (statusFilter.value) {
        result = result.filter(user => user.status === parseInt(statusFilter.value))
      }
      
      return result
    })

    // 分页用户
    const paginatedUsers = computed(() => {
      const start = (currentPage.value - 1) * pageSize.value
      const end = start + pageSize.value
      return filteredUsers.value.slice(start, end)
    })

    // 搜索处理
    const handleSearch = () => {
      currentPage.value = 1
      fetchUsers()
    }

    // 分页处理
    const handleSizeChange = (size) => {
      pageSize.value = size
      currentPage.value = 1
    }

    const handleCurrentChange = (page) => {
      currentPage.value = page
    }

    // 显示添加用户对话框
    const showAddUserDialog = () => {
      // 重置表单
      Object.assign(userForm, {
        username: '',
        password: '',
        name: '',
        email: '',
        phone: '',
        address: '',
        role: 'user',
        status: 1
      })
      addUserDialogVisible.value = true
    }

    // 添加用户
    const addUser = async () => {
      if (!userFormRef.value) return
      
      try {
        await userFormRef.value.validate()
        submitting.value = true
        
        // 将空字符串转换为null，避免后端验证错误
        const processedForm = {
          ...userForm,
          name: userForm.name || null,
          email: userForm.email || null,
          phone: userForm.phone || null,
          address: userForm.address || null
        }
        
        const response = await registerUser(processedForm)
        
        if (response.code === 200) {
          ElMessage.success('用户添加成功')
          addUserDialogVisible.value = false
          fetchUsers() // 重新获取用户列表
        } else {
          ElMessage.error(response.message || '添加用户失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('添加用户错误:', error)
          ElMessage.error('添加用户失败')
        }
      } finally {
        submitting.value = false
      }
    }

    // 查看用户详情
    const viewUser = (user) => {
      selectedUser.value = { ...user }
      viewUserDialogVisible.value = true
    }

    // 切换用户状态
    const toggleUserStatus = async (user) => {
      const newStatus = user.status === 1 ? 0 : 1
      const statusText = newStatus === 1 ? '启用' : '禁用'
      
      try {
        // 立即更新本地状态，提供即时反馈
        const userIndex = users.value.findIndex(u => u.id === user.id)
        if (userIndex !== -1) {
          users.value[userIndex].status = newStatus
        }
        
        // 发送API请求更新后端状态
        const response = await updateUser(user.id, { status: newStatus })
        
        if (response.code === 200) {
          ElMessage.success(`用户${statusText}成功`)
        } else {
          // 如果API请求失败，恢复本地状态
          const userIndex = users.value.findIndex(u => u.id === user.id)
          if (userIndex !== -1) {
            users.value[userIndex].status = user.status
          }
          ElMessage.error(response.message || `${statusText}用户失败`)
        }
      } catch (error) {
        // 如果API请求失败，恢复本地状态
        const userIndex = users.value.findIndex(u => u.id === user.id)
        if (userIndex !== -1) {
          users.value[userIndex].status = user.status
        }
        console.error(`${statusText}用户错误:`, error)
        ElMessage.error(`${statusText}用户失败`)
      }
    }

    // 删除用户（逻辑删除）
    const deleteUser = async (user) => {
      try {
        await ElMessageBox.confirm(
          `确定要删除用户 "${user.name}" 吗？删除后用户将无法登录系统！`,
          '删除确认',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        const response = await deleteUserApi(user.id)
        
        if (response.code === 200) {
          ElMessage.success('用户删除成功')
          fetchUsers() // 重新获取用户列表
        } else {
          ElMessage.error(response.message || '删除用户失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除用户错误:', error)
          ElMessage.error('删除用户失败')
        }
      }
    }

    onMounted(() => {
      fetchUsers()
    })

    return {
      users,
      loading,
      submitting,
      addUserDialogVisible,
      viewUserDialogVisible,
      selectedUser,
      userFormRef,
      userForm,
      userFormRules,
      searchQuery,
      roleFilter,
      statusFilter,
      currentPage,
      pageSize,
      filteredUsers,
      paginatedUsers,
      currentUserRole,
      hasPermission,
      getRoleName,
      getRoleTagType,
      handleSearch,
      handleSizeChange,
      handleCurrentChange,
      showAddUserDialog,
      addUser,
      viewUser,
      toggleUserStatus,
      deleteUser
    }
  }
}
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-bar {
  display: flex;
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>