// 导入request工具，用于发送API请求
import request from '../utils/request'

// 获取商品列表
export function getGoodsList(params) {
  return request({
    url: '/goods',
    method: 'get',
    params
  })
}

// 根据ID获取商品详情
export function getGoodsById(id) {
  return request({
    url: `/goods/${id}`,
    method: 'get'
  })
}

// 添加商品
export function addGoods(data) {
  return request({
    url: '/goods',
    method: 'post',
    data
  })
}

// 更新商品信息
export function updateGoods(id, data) {
  return request({
    url: `/goods/${id}`,
    method: 'put',
    data
  })
}

// 删除商品
export function deleteGoods(id) {
  return request({
    url: `/goods/${id}`,
    method: 'delete'
  })
}

// 更新商品状态
export function updateGoodsStatus(id, status) {
  return request({
    url: `/goods/${id}/status`,
    method: 'put',
    params: { status }
  })
}

// 获取商品分类列表
export function getCategories() {
  return request({
    url: '/categories/list',
    method: 'get'
  })
}