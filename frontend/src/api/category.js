// 导入request工具，用于发送API请求
import request from '../utils/request'

// 获取所有商品分类（支持分页和条件查询）
export function getCategories(params) {
  return request({
    url: '/categories',
    method: 'get',
    params
  })
}

// 根据ID获取商品分类
export function getCategoryById(id) {
  return request({
    url: `/categories/${id}`,
    method: 'get'
  })
}

// 添加商品分类
export function addCategory(data) {
  return request({
    url: '/categories',
    method: 'post',
    data
  })
}

// 更新商品分类
export function updateCategory(id, data) {
  return request({
    url: `/categories/${id}`,
    method: 'put',
    data
  })
}

// 删除商品分类
export function deleteCategory(id) {
  return request({
    url: `/categories/${id}`,
    method: 'delete'
  })
}

// 根据状态获取商品分类
export function getCategoriesByStatus(status) {
  return request({
    url: `/categories/status/${status}`,
    method: 'get'
  })
}

// 根据名称获取商品分类
export function getCategoryByName(name) {
  return request({
    url: `/categories/name/${name}`,
    method: 'get'
  })
}

// 检查分类名称是否存在
export function existsByName(name, id) {
  return request({
    url: '/categories/exists',
    method: 'get',
    params: { name, id }
  })
}

// 统计分类数量
export function countCategories() {
  return request({
    url: '/categories/count',
    method: 'get'
  })
}

// 获取所有分类（不分页）
export function getAllCategories() {
  return request({
    url: '/categories/all',
    method: 'get'
  })
}

// 获取所有顶级分类
export function getTopLevelCategories() {
  return request({
    url: '/categories/top',
    method: 'get'
  })
}

// 根据父分类ID获取子分类
export function getChildrenCategories(parentId) {
  return request({
    url: `/categories/children/${parentId}`,
    method: 'get'
  })
}

// 获取完整的分类树结构
export function getCategoryTree() {
  return request({
    url: '/categories/tree',
    method: 'get'
  })
}

// 检查分类是否有子分类
export function hasChildrenCategories(id) {
  return request({
    url: `/categories/has-children/${id}`,
    method: 'get'
  })
}