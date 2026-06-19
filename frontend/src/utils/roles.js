// 角色权限系统定义

/**
 * 角色类型枚举
 */
export const ROLES = {
  ADMIN: 'admin',      // 管理员
  MERCHANT: 'merchant', // 商家
  USER: 'user'          // 普通用户
}

/**
 * 角色名称映射
 */
export const ROLE_NAMES = {
  [ROLES.ADMIN]: '管理员',
  [ROLES.MERCHANT]: '商家',
  [ROLES.USER]: '普通用户'
}

/**
 * 权限类型枚举
 */
export const PERMISSIONS = {
  // 用户管理权限
  USER_READ: 'user:read',
  USER_CREATE: 'user:create',
  USER_UPDATE: 'user:update',
  USER_DELETE: 'user:delete',
  
  // 商品管理权限
  GOODS_READ: 'goods:read',
  GOODS_CREATE: 'goods:create',
  GOODS_UPDATE: 'goods:update',
  GOODS_DELETE: 'goods:delete',
  GOODS_PUBLISH: 'goods:publish',
  
  // 订单管理权限
  ORDER_READ: 'order:read',
  ORDER_READ_ALL: 'order:read:all',
  ORDER_CREATE: 'order:create',
  ORDER_UPDATE: 'order:update',
  ORDER_DELETE: 'order:delete',
  ORDER_DELETE_OWN: 'order:delete:own',
  ORDER_PROCESS: 'order:process',
  ORDER_CANCEL: 'order:cancel',
  

  
  // 系统管理权限
  SYSTEM_SETTINGS: 'system:settings',
  SYSTEM_LOGS: 'system:logs',
  DASHBOARD_VIEW: 'dashboard:view',
  
  // 轮播图管理权限
  CAROUSEL_MANAGE: 'carousel:manage',
  
  // 分类管理权限
  CATEGORY_READ: 'category:read',
  CATEGORY_CREATE: 'category:create',
  CATEGORY_UPDATE: 'category:update',
  CATEGORY_DELETE: 'category:delete'
}

/**
 * 角色权限配置
 */
export const ROLE_PERMISSIONS = {
  [ROLES.ADMIN]: [
    // 管理员拥有所有权限
    PERMISSIONS.USER_READ,
    PERMISSIONS.USER_CREATE,
    PERMISSIONS.USER_UPDATE,
    PERMISSIONS.USER_DELETE,
    PERMISSIONS.GOODS_READ,
    PERMISSIONS.GOODS_CREATE,
    PERMISSIONS.GOODS_UPDATE,
    PERMISSIONS.GOODS_DELETE,
    PERMISSIONS.GOODS_PUBLISH,
    PERMISSIONS.ORDER_READ,
    PERMISSIONS.ORDER_READ_ALL,
    PERMISSIONS.ORDER_CREATE,
    PERMISSIONS.ORDER_UPDATE,
    PERMISSIONS.ORDER_DELETE,
    PERMISSIONS.ORDER_PROCESS,
    PERMISSIONS.ORDER_CANCEL,

    PERMISSIONS.SYSTEM_SETTINGS,
    PERMISSIONS.SYSTEM_LOGS,
    PERMISSIONS.DASHBOARD_VIEW,
    PERMISSIONS.CAROUSEL_MANAGE,
    PERMISSIONS.CATEGORY_READ,
    PERMISSIONS.CATEGORY_CREATE,
    PERMISSIONS.CATEGORY_UPDATE,
    PERMISSIONS.CATEGORY_DELETE
  ],
  
  [ROLES.MERCHANT]: [
    // 商家权限
    PERMISSIONS.GOODS_READ,
    PERMISSIONS.GOODS_CREATE,
    PERMISSIONS.GOODS_UPDATE,
    PERMISSIONS.GOODS_PUBLISH, // 商家可以发布自己的商品
    PERMISSIONS.ORDER_READ,
    PERMISSIONS.ORDER_READ_ALL,
    PERMISSIONS.ORDER_PROCESS, // 商家可以处理订单
    PERMISSIONS.ORDER_DELETE_OWN, // 商家可以删除自己的订单
    PERMISSIONS.ORDER_CANCEL, // 商家可以取消订单
    // 添加分类管理权限
    PERMISSIONS.CATEGORY_READ,
    PERMISSIONS.CATEGORY_CREATE,
    PERMISSIONS.CATEGORY_UPDATE,
    // 商家不能删除分类，只能由管理员删除

    // 商家不能删除商品，只能下架
    // 商家不能管理用户
    // 商家不能访问系统设置
  ],
  
  [ROLES.USER]: [
    // 普通用户权限
    PERMISSIONS.GOODS_READ, // 用户可以查看商品
    PERMISSIONS.ORDER_CREATE, // 用户可以创建订单
    PERMISSIONS.ORDER_READ, // 用户可以查看自己的订单
    PERMISSIONS.ORDER_DELETE_OWN, // 用户可以删除自己的订单
    PERMISSIONS.ORDER_CANCEL, // 用户可以取消自己的订单

    // 用户不能管理商品
    // 用户不能处理订单
    // 用户不能管理其他用户
    // 用户不能访问系统设置
  ]
}

/**
 * 角色可访问路由配置
 */
export const ROLE_ROUTES = {
  [ROLES.ADMIN]: [
    '/home/users',
    '/home/goods',
    '/home/orders',
    '/home/rights'
  ],
  
  [ROLES.MERCHANT]: [
    '/home/goods', // 商家可以管理商品
    '/home/categories', // 商家可以管理分类
    '/home/orders' // 商家可以处理订单
  ],
  
  [ROLES.USER]: [
    '/home/goods', // 用户可以浏览商品
    '/home/orders' // 用户可以查看自己的订单
  ]
}

/**
 * 角色功能描述
 */
export const ROLE_DESCRIPTIONS = {
  [ROLES.ADMIN]: {
    title: '系统管理员',
    description: '拥有系统所有权限，可以管理用户、商品和订单',
    features: [
      '用户管理：创建、查看、修改、删除用户',
      '商品管理：创建、查看、修改、删除、发布商品',
      '订单管理：查看、处理所有订单',
      '权限管理：分配和管理用户角色'
    ],
    restrictions: []
  },
  
  [ROLES.MERCHANT]: {
    title: '商家',
    description: '可以管理自己的商品、分类和处理订单',
    features: [
      '商品管理：创建、查看、修改、发布自己的商品',
      '分类管理：创建、查看、修改商品分类',
      '订单处理：查看和处理相关订单',
      '库存管理：管理商品库存'
    ],
    restrictions: [
      '不能删除商品，只能下架',
      '不能删除分类，只能由管理员删除',
      '不能管理用户信息',
      '只能管理自己创建的商品'
    ]
  },
  
  [ROLES.USER]: {
    title: '普通用户',
    description: '可以浏览商品和购买商品',
    features: [
      '商品浏览：查看商品信息',
      '下单购买：创建订单并购买商品',
      '订单查看：查看自己的订单状态'
    ],
    restrictions: [
      '不能管理商品',
      '不能管理其他用户',
      '只能查看自己的订单'
    ]
  }
}

/**
 * 默认角色
 */
export const DEFAULT_ROLE = ROLES.USER