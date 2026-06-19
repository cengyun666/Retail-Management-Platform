import { RouterView, createRouter, createWebHistory } from "vue-router"
import { h } from 'vue'
import Login from './components/Login.vue'
import Register from './components/Register.vue'
import Home from './components/Home.vue'
import { hasPermission } from './utils/permissions.js'
import { ROLES, PERMISSIONS } from './utils/roles.js'

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

// 角色权限验证函数
const checkPermission = (requiredPermissions) => {
  const userRole = getUserRole()
  if (!requiredPermissions || requiredPermissions.length === 0) {
    return true
  }
  return hasPermission(userRole, requiredPermissions)
}

const router = createRouter({
    history: createWebHistory(),
    routes: [
        { path: '/', redirect: '/login' },
        { path: '/login', component: Login },
        { path: '/register', component: Register },
        {
            path: '/home',
            component: Home,
            redirect: to => {
                // 根据用户角色动态重定向
                const userRole = getUserRole()
                return getDefaultRouteByRole(userRole)
            },
            children: [
                {
                    path: 'users',
                    component: { render: () => h(RouterView) },
                    meta: {
                        permissions: ['user:read']
                    },
                    children: [
                        {
                            path: '',
                            name: 'UserManagement',
                            component: () => import('./views/UserManagement.vue')
                        },
                        {
                            path: ':id',
                            name: 'userDetail',
                            component: () => import('./components/subcomponents/UserDetail.vue'),
                            props: true,
                            meta: {
                                permissions: ['user:read']
                            }
                        }
                    ]
                },
                {
                    path: 'goods',
                    component: { render: () => h(RouterView) },
                    meta: {
                        permissions: ['goods:read']
                    },
                    children: [
                        {
                            path: '',
                            component: () => import('./components/subcomponents/MyGoods.vue')
                        },
                        {
                            path: ':id',
                            name: 'goodsDetail',
                            component: () => import('./components/subcomponents/GoodsDetail.vue'),
                            props: true,
                            meta: {
                                permissions: ['goods:read']
                            }
                        }
                    ]
                },
                {
                    path: 'orders',
                    name: 'orders',
                    component: () => import('./components/subcomponents/MyOrders.vue'),
                    meta: {
                        title: '订单管理',
                        permissions: ['order:read']
                    }
                },
                {
                    path: 'cart',
                    name: 'cart',
                    component: () => import('./components/subcomponents/MyCart.vue'),
                    meta: {
                        title: '购物车',
                        permissions: ['goods:read'],
                        roles: [ROLES.USER] // 只有普通用户可以访问购物车
                    }
                },
                {
                    path: 'checkout',
                    name: 'checkout',
                    component: () => import('./components/subcomponents/MyCheckout.vue'),
                    meta: {
                        title: '结算',
                        permissions: ['goods:read'],
                        roles: [ROLES.USER] // 只有普通用户可以访问结算页面
                    }
                },
                {
                    path: 'payment',
                    name: 'payment',
                    component: () => import('./components/subcomponents/MyPayment.vue'),
                    meta: {
                        title: '支付',
                        permissions: ['goods:read'],
                        roles: [ROLES.USER] // 只有普通用户可以访问支付页面
                    }
                },
                {
                    path: 'dashboard',
                    name: 'dashboard',
                    component: () => import('./views/Dashboard.vue'),
                    meta: {
                        title: '数据大屏',
                        permissions: ['dashboard:view'] // 只有管理员才能查看数据大屏
                    }
                },


                {
                    path: 'carousel',
                    name: 'carousel',
                    component: () => import('./components/subcomponents/CarouselManagement.vue'),
                    meta: {
                        title: '轮播图管理',
                        permissions: ['carousel:manage']
                    }
                },
                {                
                    path: 'categories',
                    name: 'categories',
                    component: () => import('./components/subcomponents/CategoryManagement.vue'),
                    meta: {
                        title: '分类管理',
                        permissions: ['category:read']
                    }
                },
                {                
                    path: 'profile',
                    name: 'profile',
                    component: () => import('./components/subcomponents/ProfileManagement.vue'),
                    meta: {
                        title: '个人信息',
                        permissions: []
                    }
                },
                {                
                    path: 'address',
                    name: 'address',
                    component: () => import('./components/subcomponents/AddressManagement.vue'),
                    meta: {
                        title: '收货地址',
                        permissions: []
                    }
                }
            ]
        },
    ]

})
router.beforeEach((to, from, next) => {
    if (to.path === '/login' || to.path === '/register') {
        return next()
    }
    
    const userRole = getUserRole()
    
    // 检查路由权限
    if (to.meta && to.meta.permissions) {
        const hasRoutePermission = checkPermission(to.meta.permissions)
        if (!hasRoutePermission) {
            // 如果没有权限，重定向到首页
            return next('/home')
        }
    }
    
    // 检查路由角色限制
    if (to.meta && to.meta.roles) {
        if (!to.meta.roles.includes(userRole)) {
            // 如果角色不符合，重定向到首页
            return next('/home')
        }
    }
    
    next()
})

export default router