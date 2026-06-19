// 导入请求工具
import request from '@/utils/request'

// 导入模拟数据函数
import { loginUser as mockLoginUser } from './mock-users.js'

// 获取用户列表
export function getUsersList(params) {
  return request({
    url: '/users/list',
    method: 'get',
    params
  })
}

// 根据ID获取用户详情
export function getUserById(id) {
  return request({
    url: `/users/id/${id}`,
    method: 'get'
  })
}

// 根据用户名获取用户信息
export function getUserByUsername(username) {
  return request({
    url: `/users/${username}`,
    method: 'get'
  })
}

// 根据token获取用户信息
export function getUserByToken(token) {
  return request({
    url: '/users/token',
    method: 'get',
    headers: {
      Authorization: `Bearer ${token}`
    }
  })
}

// 用户注册
export function registerUser(userData) {
  return request({
    url: '/users/register',
    method: 'post',
    data: userData
  })
}

// 删除用户
export function deleteUser(id) {
  return request({
    url: `/users/${id}`,
    method: 'delete'
  })
}

// 用户登录
export function loginUser(loginData) {
  return request({
    url: '/users/login',
    method: 'post',
    data: loginData
  })
}

// 更新用户信息
export function updateUserInfo(userId, userData) {
  return request({
    url: `/users/${userId}`,
    method: 'put',
    data: userData
  })
}