import request from '../utils/request'

// 获取用户的所有地址
export const getUserAddresses = () => {
  return request.get('/addresses')
}

// 获取用户的默认地址
export const getDefaultAddress = () => {
  return request.get('/addresses/default')
}

// 添加新地址
export const addAddress = (addressData) => {
  return request.post('/addresses', addressData)
}

// 更新地址
export const updateAddress = (addressId, addressData) => {
  return request.put(`/addresses/${addressId}`, addressData)
}

// 删除地址
export const deleteAddress = (addressId) => {
  return request.delete(`/addresses/${addressId}`)
}

// 设置默认地址
export const setDefaultAddress = (addressId) => {
  return request.put(`/addresses/${addressId}/default`)
}

// 获取地址详情
export const getAddressById = (addressId) => {
  return request.get(`/addresses/${addressId}`)
}