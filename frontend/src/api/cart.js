// 导入模拟API，实际项目中应该使用真实的API
import { 
  getCartItems as mockGetCartItems, 
  addToCart as mockAddToCart,
  updateCartItem as mockUpdateCartItem,
  removeFromCart as mockRemoveFromCart,
  clearCart as mockClearCart
} from './mock-cart'

// 获取购物车列表
export function getCartItems(params) {
  return mockGetCartItems(params)
}

// 添加商品到购物车
export function addToCart(data) {
  return mockAddToCart(data)
}

// 更新购物车商品数量
export function updateCartItem(id, data) {
  return mockUpdateCartItem(id, data)
}

// 从购物车删除商品
export function removeFromCart(id) {
  return mockRemoveFromCart(id)
}

// 清空购物车
export function clearCart() {
  return mockClearCart()
}