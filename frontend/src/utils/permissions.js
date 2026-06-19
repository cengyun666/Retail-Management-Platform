import { ROLES, ROLE_PERMISSIONS } from '../utils/roles.js'

/**
 * 检查用户是否有指定权限
 * @param {string} userRole - 用户角色
 * @param {string|Array} permission - 权限名称或权限数组
 * @returns {boolean} - 是否有权限
 */
export function hasPermission(userRole, permission) {
  if (!userRole || !permission) return false
  
  const userPermissions = ROLE_PERMISSIONS[userRole] || []
  
  if (Array.isArray(permission)) {
    return permission.some(p => userPermissions.includes(p))
  }
  
  return userPermissions.includes(permission)
}

/**
 * 检查用户是否有任一指定权限
 * @param {string} userRole - 用户角色
 * @param {Array} permissions - 权限数组
 * @returns {boolean} - 是否有任一权限
 */
export function hasAnyPermission(userRole, permissions) {
  if (!userRole || !permissions || !Array.isArray(permissions)) return false
  
  const userPermissions = ROLE_PERMISSIONS[userRole] || []
  
  return permissions.some(p => userPermissions.includes(p))
}

/**
 * 检查用户是否有所有指定权限
 * @param {string} userRole - 用户角色
 * @param {Array} permissions - 权限数组
 * @returns {boolean} - 是否有所有权限
 */
export function hasAllPermissions(userRole, permissions) {
  if (!userRole || !permissions || !Array.isArray(permissions)) return false
  
  const userPermissions = ROLE_PERMISSIONS[userRole] || []
  
  return permissions.every(p => userPermissions.includes(p))
}

/**
 * 获取用户的所有权限
 * @param {string} userRole - 用户角色
 * @returns {Array} - 权限数组
 */
export function getUserPermissions(userRole) {
  return ROLE_PERMISSIONS[userRole] || []
}

/**
 * 检查用户是否为管理员
 * @param {string} userRole - 用户角色
 * @returns {boolean} - 是否为管理员
 */
export function isAdmin(userRole) {
  return (userRole || '').toLowerCase() === ROLES.ADMIN
}

/**
 * 检查用户是否为商家
 * @param {string} userRole - 用户角色
 * @returns {boolean} - 是否为商家
 */
export function isMerchant(userRole) {
  return (userRole || '').toLowerCase() === ROLES.MERCHANT
}

/**
 * 检查用户是否为普通用户
 * @param {string} userRole - 用户角色
 * @returns {boolean} - 是否为普通用户
 */
export function isUser(userRole) {
  return (userRole || '').toLowerCase() === ROLES.USER
}

/**
 * 获取角色名称
 * @param {string} role - 角色ID
 * @returns {string} - 角色名称
 */
export function getRoleName(role) {
  const roleNames = {
    [ROLES.ADMIN]: '管理员',
    [ROLES.MERCHANT]: '商家',
    [ROLES.USER]: '普通用户'
  }
  
  // 处理大写的角色值
  if (role === 'ADMIN') return '管理员'
  if (role === 'MERCHANT') return '商家'
  if (role === 'USER') return '普通用户'
  
  // 处理可能的数值角色值
  if (role === '1' || role === 1) return '管理员'
  if (role === '2' || role === 2) return '商家'
  if (role === '3' || role === 3) return '普通用户'
  
  return roleNames[role] || '未知角色'
}

/**
 * 创建权限指令
 * 用于在模板中控制元素显示
 * @param {string} permission - 权限名称
 * @returns {Object} - 指令对象
 */
export function createPermissionDirective(permission) {
  return {
    mounted(el, binding) {
      const userRole = binding.value
      if (!hasPermission(userRole, permission)) {
        el.style.display = 'none'
      }
    },
    updated(el, binding) {
      const userRole = binding.value
      if (!hasPermission(userRole, permission)) {
        el.style.display = 'none'
      } else {
        el.style.display = ''
      }
    }
  }
}