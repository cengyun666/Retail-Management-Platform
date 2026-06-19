<template>
  <div class="layout-aside-container">
    <div class="aside-header">
      <h2 class="aside-title">
        <i class="icon-menu"></i>
        系统菜单
      </h2>
    </div>

    <ul class="menu">
      <li class="menu-item" v-for="item in menuItems" :key="item.path" :class="{ active: isActive(item.path) }">
        <router-link :to="item.path" class="menu-link">
          <i :class="item.icon"></i>
          <span class="menu-text">
            <!-- 根据用户角色动态显示菜单名称 -->
            {{ item.name === '商品管理' && currentUserRole.toLowerCase() === 'user' ? '商品' : 
               item.name === '订单管理' && currentUserRole.toLowerCase() === 'user' ? '我的订单' : 
               item.name }}
          </span>
          <span class="menu-badge" v-if="item.badge">{{ item.badge }}</span>
        </router-link>
      </li>
    </ul>

    <div class="aside-footer">
      <div class="system-info">
        <div class="info-item">
          <i class="icon-time"></i>
          <span>{{ currentTime }}</span>
        </div>
        <div class="info-item">
          <i class="icon-version"></i>
          <span>v1.0.0</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { hasPermission } from '../../utils/permissions.js'
import { ROLES } from '../../utils/roles.js'

export default {
  name: 'MyAside',
  setup() {
    const route = useRoute()
    const currentTime = ref('')
    let timeInterval = null

    // 获取用户角色，确保返回小写
    const getUserRole = () => {
      const userInfo = localStorage.getItem('userInfo')
      if (userInfo) {
        try {
          const user = JSON.parse(userInfo)
          return (user.role || ROLES.USER).toLowerCase()
        } catch (e) {
          console.error('解析用户信息失败', e)
          return ROLES.USER
        }
      }
      return ROLES.USER
    }

    const currentUserRole = getUserRole()

    // 菜单配置
    const allMenuItems = [
      { 
        name: '数据大屏', 
        path: '/home/dashboard', 
        icon: 'icon-dashboard', 
        badge: null,
        permissions: ['dashboard:view'] // 只有管理员才能查看数据大屏
      },
      { 
        name: '用户管理', 
        path: '/home/users', 
        icon: 'icon-user', 
        badge: null,
        permissions: ['user:read']
      },
      { 
        name: '商品管理', 
        path: '/home/goods', 
        icon: 'icon-goods', 
        badge: null, 
        permissions: ['goods:read'] 
      },
      { 
        name: '购物车', 
        path: '/home/cart', 
        icon: 'icon-cart', 
        badge: null,
        roles: [ROLES.USER] // 只有普通用户才能看到购物车
      },
      { 
        name: '订单管理', 
        path: '/home/orders', 
        icon: 'icon-order', 
        badge: null, 
        permissions: ['order:read'] 
      },
      { 
        name: '轮播图管理', 
        path: '/home/carousel', 
        icon: 'icon-carousel', 
        badge: null,
        permissions: ['carousel:manage']
      },
      { 
        name: '分类管理', 
        path: '/home/categories', 
        icon: 'icon-category', 
        badge: null,
        permissions: ['category:read']
      }
    ]

    // 根据用户权限和角色过滤菜单
    const menuItems = computed(() => {
      return allMenuItems.filter(item => {
        // 检查角色限制
        if (item.roles && item.roles.length > 0) {
          return item.roles.includes(currentUserRole)
        }
        
        // 检查权限限制
        if (item.permissions && item.permissions.length > 0) {
          return hasPermission(currentUserRole, item.permissions)
        }
        
        // 如果没有角色和权限限制，则显示
        return true
      })
    })

    // 检查当前路由是否激活
    const isActive = (path) => {
      return route.path.startsWith(path)
    }

    // 更新时间
    const updateTime = () => {
      const now = new Date()
      currentTime.value = now.toLocaleTimeString('zh-CN', {
        hour12: false,
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    }

    onMounted(() => {
      updateTime()
      timeInterval = setInterval(updateTime, 1000)
    })

    onUnmounted(() => {
      if (timeInterval) {
        clearInterval(timeInterval)
      }
    })

    return {
      menuItems,
      currentTime,
      isActive,
      currentUserRole
    }
  }
}
</script>

<style scoped>
.layout-aside-container {
  height: 100vh;
  background: linear-gradient(135deg, #1e90ff 0%, #4169e1 100%);
  color: white;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
}

.aside-header {
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
}

.aside-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 10px;
}

.icon-menu {
  width: 20px;
  height: 20px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  display: inline-block;
  position: relative;
}

.icon-menu::before {
  content: '';
  position: absolute;
  top: 4px;
  left: 4px;
  right: 4px;
  bottom: 4px;
  background: white;
  border-radius: 2px;
}

.menu {
  list-style: none;
  padding: 0;
  margin: 0;
  flex: 1;
  overflow-y: auto;
}

.menu-item {
  margin: 5px 15px;
  border-radius: 8px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.menu-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

.menu-item:hover::before {
  left: 100%;
}

.menu-item.active {
  background: rgba(255, 255, 255, 0.15);
  transform: translateX(5px);
}

.menu-item.active .menu-link {
  color: #fff;
  font-weight: 600;
}

.menu-link {
  display: flex;
  align-items: center;
  padding: 12px 15px;
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  transition: all 0.3s ease;
  border-radius: 8px;
  position: relative;
  z-index: 1;
}

.menu-link:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
  transform: translateX(3px);
}

.menu-link i {
  width: 20px;
  height: 20px;
  margin-right: 12px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-style: normal;
  font-size: 12px;
}

.icon-user::before {
  content: '👤';
}

.icon-lock::before {
  content: '🔒';
}

.icon-goods::before {
  content: '📦';
}

.icon-order::before {
  content: '📋';
}

.icon-settings::before {
  content: '⚙️';
}

.icon-carousel::before {
  content: '🖼️';
}

.icon-category::before {
  content: '📂';
}

.menu-text {
  flex: 1;
  font-size: 14px;
}

.menu-badge {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  min-width: 20px;
  text-align: center;
}

.aside-footer {
  padding: 15px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
}

.system-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
}

.info-item i {
  width: 16px;
  height: 16px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-style: normal;
  font-size: 10px;
}

.icon-time::before {
  content: '🕒';
}

.icon-version::before {
  content: '🔧';
}

/* 滚动条样式 */
.menu::-webkit-scrollbar {
  width: 4px;
}

.menu::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
}

.menu::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 2px;
}

.menu::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.5);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .layout-aside-container {
    width: 60px;
  }

  .menu-text,
  .aside-title,
  .info-item span {
    display: none;
  }

  .menu-link {
    justify-content: center;
    padding: 15px;
  }

  .menu-link i {
    margin-right: 0;
  }

  .menu-badge {
    position: absolute;
    top: 8px;
    right: 8px;
    font-size: 10px;
    padding: 1px 4px;
  }
}
</style>