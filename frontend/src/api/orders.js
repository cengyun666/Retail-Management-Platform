// 导入axios实例
import request from '../utils/request'

// 获取订单列表
export function getOrdersList(params) {
  return request.get('/orders/list', { params })
}

// 根据ID获取订单详情
export function getOrderById(id) {
  return request.get(`/orders/${id}`)
}

// 更新订单状态
export function updateOrderStatus(id, status, merchantId) {
  return request.put(`/orders/${id}/status`, { status, merchantId })
}

// 删除订单
export function deleteOrder(id) {
  return request.delete(`/orders/${id}`)
}

// 获取订单状态列表
export function getOrderStatusList() {
  return request.get('/orders/status-list')
}

// 创建订单
export function createOrder(data) {
  return request.post('/orders/create', data)
}

// 确认收货
export function confirmReceipt(id) {
  return request.put(`/orders/${id}/confirm-receipt`)
}

// 取消订单
export function cancelOrder(id) {
  return request.put(`/orders/${id}/cancel`)
}