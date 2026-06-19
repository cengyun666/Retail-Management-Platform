import request from '@/utils/request'

// 获取用户列表
export function getUsersList(params) {
  return request({
    url: '/users/list',
    method: 'get',
    params
  })
}

// 根据ID获取用户信息
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

// 注册用户
export function registerUser(data) {
  return request({
    url: '/users/register',
    method: 'post',
    data
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
export function loginUser(data) {
  return request({
    url: '/users/login',
    method: 'post',
    data
  })
}

// 更新用户信息
export function updateUser(id, data) {
  return request({
    url: `/users/${id}`,
    method: 'put',
    data
  })
}