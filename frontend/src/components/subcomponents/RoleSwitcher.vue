<template>
  <div class="role-switcher">
    <el-card class="role-card">
      <template #header>
        <div class="card-header">
          <span>当前角色</span>
          <el-button v-if="hasMultipleRoles" type="primary" size="small" @click="showSwitchDialog = true">
            切换角色
          </el-button>
        </div>
      </template>
      
      <div class="current-role">
        <el-tag :type="getRoleTagType(currentRole)" size="large">
          <i :class="getRoleIcon(currentRole)"></i>
          {{ getRoleName(currentRole) }}
        </el-tag>
        <p class="role-description">{{ getRoleDescription(currentRole) }}</p>
      </div>
      
      <div class="role-permissions">
        <h4>当前权限</h4>
        <el-tag 
          v-for="permission in getCurrentPermissions" 
          :key="permission" 
          size="small" 
          class="permission-tag"
        >
          {{ permission }}
        </el-tag>
      </div>
    </el-card>

    <!-- 角色切换对话框 -->
    <el-dialog 
      v-model="showSwitchDialog" 
      title="切换角色" 
      width="400px"
    >
      <div class="role-options">
        <el-radio-group v-model="selectedRole">
          <div 
            v-for="role in availableRoles" 
            :key="role.value" 
            class="role-option"
          >
            <el-radio :label="role.value">
              <div class="role-info">
                <div class="role-name">
                  <i :class="role.icon"></i>
                  {{ role.name }}
                </div>
                <div class="role-desc">{{ role.description }}</div>
              </div>
            </el-radio>
          </div>
        </el-radio-group>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showSwitchDialog = false">取消</el-button>
          <el-button type="primary" @click="switchRole" :disabled="!selectedRole || selectedRole === currentRole">
            确认切换
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ROLES, ROLE_DESCRIPTIONS, ROLE_PERMISSIONS } from '../../utils/roles.js'
import { hasPermission } from '../../utils/permissions.js'

export default {
  name: 'RoleSwitcher',
  setup() {
    const currentRole = ref(ROLES.USER)
    const showSwitchDialog = ref(false)
    const selectedRole = ref('')
    
    // 获取用户信息
    const getUserInfo = () => {
      const userInfo = localStorage.getItem('userInfo')
      if (userInfo) {
        try {
          return JSON.parse(userInfo)
        } catch (e) {
          console.error('解析用户信息失败', e)
          return {}
        }
      }
      return {}
    }
    
    // 更新用户信息到localStorage
    const updateUserInfo = (userInfo) => {
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    }
    
    // 获取当前用户可用的角色列表
    const availableRoles = computed(() => {
      const userInfo = getUserInfo()
      
      // 这里可以根据实际业务逻辑确定用户可切换的角色
      // 例如：某些用户可能有多个角色，或者只有管理员可以切换角色
      const roles = []
      
      // 所有用户都可以是普通用户
      roles.push({
        value: ROLES.USER,
        name: '普通用户',
        icon: 'el-icon-user',
        description: ROLE_DESCRIPTIONS[ROLES.USER]
      })
      
      // 如果用户名是admin，可以切换到商家和管理员角色
      if (userInfo.username === 'admin') {
        roles.push({
          value: ROLES.MERCHANT,
          name: '商家',
          icon: 'el-icon-suitcase',
          description: ROLE_DESCRIPTIONS[ROLES.MERCHANT]
        })
        
        roles.push({
          value: ROLES.ADMIN,
          name: '管理员',
          icon: 'el-icon-s-tools',
          description: ROLE_DESCRIPTIONS[ROLES.ADMIN]
        })
      }
      
      return roles
    })
    
    // 是否有多个角色可选
    const hasMultipleRoles = computed(() => {
      return availableRoles.value.length > 1
    })
    
    // 获取当前角色的权限
    const getCurrentPermissions = computed(() => {
      return ROLE_PERMISSIONS[currentRole.value] || []
    })
    
    // 获取角色名称
    const getRoleName = (role) => {
      const roleMap = {
        [ROLES.ADMIN]: '管理员',
        [ROLES.MERCHANT]: '商家',
        [ROLES.USER]: '普通用户'
      }
      return roleMap[role] || '未知角色'
    }
    
    // 获取角色标签类型
    const getRoleTagType = (role) => {
      const typeMap = {
        [ROLES.ADMIN]: 'danger',
        [ROLES.MERCHANT]: 'warning',
        [ROLES.USER]: 'info'
      }
      return typeMap[role] || 'info'
    }
    
    // 获取角色图标
    const getRoleIcon = (role) => {
      const iconMap = {
        [ROLES.ADMIN]: 'el-icon-s-tools',
        [ROLES.MERCHANT]: 'el-icon-suitcase',
        [ROLES.USER]: 'el-icon-user'
      }
      return iconMap[role] || 'el-icon-user'
    }
    
    // 获取角色描述
    const getRoleDescription = (role) => {
      return ROLE_DESCRIPTIONS[role] || '无描述'
    }
    
    // 切换角色
    const switchRole = () => {
      if (!selectedRole.value || selectedRole.value === currentRole.value) {
        return
      }
      
      // 更新当前角色
      currentRole.value = selectedRole.value
      
      // 更新localStorage中的用户信息
      const userInfo = getUserInfo()
      userInfo.role = selectedRole.value
      // 更新权限列表
      userInfo.permissions = ROLE_PERMISSIONS[selectedRole.value]
      updateUserInfo(userInfo)
      
      // 关闭对话框
      showSwitchDialog.value = false
      
      // 显示成功消息
      ElMessage.success(`已切换到${getRoleName(selectedRole.value)}角色`)
      
      // 刷新页面以应用新权限
      window.location.reload()
    }
    
    // 初始化
    onMounted(() => {
      const userInfo = getUserInfo()
      if (userInfo.role) {
        currentRole.value = userInfo.role
      }
      selectedRole.value = currentRole.value
    })
    
    return {
      currentRole,
      showSwitchDialog,
      selectedRole,
      availableRoles,
      hasMultipleRoles,
      getCurrentPermissions,
      getRoleName,
      getRoleTagType,
      getRoleIcon,
      getRoleDescription,
      switchRole
    }
  }
}
</script>

<style scoped>
.role-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.current-role {
  text-align: center;
  margin-bottom: 20px;
}

.role-description {
  margin-top: 10px;
  color: #666;
  font-size: 14px;
}

.role-permissions h4 {
  margin-bottom: 10px;
  font-size: 16px;
}

.permission-tag {
  margin-right: 8px;
  margin-bottom: 8px;
}

.role-options {
  margin: 15px 0;
}

.role-option {
  margin-bottom: 15px;
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.role-info {
  margin-left: 20px;
}

.role-name {
  font-weight: bold;
  margin-bottom: 5px;
}

.role-name i {
  margin-right: 5px;
}

.role-desc {
  font-size: 12px;
  color: #999;
}

.dialog-footer {
  text-align: right;
}
</style>