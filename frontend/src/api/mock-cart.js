// 模拟购物车数据
// 从localStorage获取购物车数据，如果没有则初始化为空数组
function getCartItemsFromStorage() {
  return JSON.parse(localStorage.getItem('mockCartItems') || '[]')
}

// 保存购物车数据到localStorage
function saveCartData(cartItems, nextCartId) {
  localStorage.setItem('mockCartItems', JSON.stringify(cartItems))
  localStorage.setItem('mockNextCartId', nextCartId.toString())
}

// 获取下一个购物车ID
function getNextCartId() {
  return parseInt(localStorage.getItem('mockNextCartId') || '1')
}

// 模拟获取购物车列表
export function getCartItems(params) {
  return new Promise((resolve) => {
    setTimeout(() => {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      const userId = userInfo.id || 1
      const cartItems = getCartItemsFromStorage()
      
      // 过滤当前用户的购物车商品
      const userCartItems = cartItems.filter(item => item.userId === userId)
      
      // 如果有搜索参数，进行过滤
      let filteredItems = userCartItems
      if (params && params.goodsName) {
        filteredItems = userCartItems.filter(item => 
          item.goodsName.toLowerCase().includes(params.goodsName.toLowerCase())
        )
      }
      
      resolve({
        code: 200,
        message: '获取购物车成功',
        data: {
          list: filteredItems,
          total: filteredItems.length
        }
      })
    }, 300)
  })
}

// 模拟添加商品到购物车
export function addToCart(data) {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      const userId = userInfo.id || 1
      let cartItems = getCartItemsFromStorage()
      let nextCartId = getNextCartId()
      
      // 检查商品是否已在购物车中
      const existingItem = cartItems.find(item => 
        item.userId === userId && item.goodsId === data.goodsId
      )
      
      if (existingItem) {
        // 如果已存在，更新数量
        existingItem.quantity += data.quantity || 1
        existingItem.totalPrice = existingItem.price * existingItem.quantity
        
        // 保存到localStorage
        saveCartData(cartItems, nextCartId)
        
        resolve({
          code: 200,
          message: '更新购物车成功',
          data: existingItem
        })
      } else {
        // 如果不存在，添加新商品
        const newItem = {
          id: nextCartId++,
          userId: userId,
          goodsId: data.goodsId,
          goodsName: data.goodsName,
          price: data.price,
          quantity: data.quantity || 1,
          totalPrice: data.price * (data.quantity || 1),
          image: data.image,
          selected: true, // 默认选中
          createTime: new Date().toISOString(),
          addressId: data.addressId || null, // 地址ID
          addressDetails: data.addressDetails || null // 完整地址信息
        }
        
        cartItems.push(newItem)
        
        // 保存到localStorage
        saveCartData(cartItems, nextCartId)
        
        resolve({
          code: 200,
          message: '添加到购物车成功',
          data: newItem
        })
      }
    }, 300)
  })
}

// 模拟更新购物车商品数量或地址信息
export function updateCartItem(id, data) {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      const userId = userInfo.id || 1
      let cartItems = getCartItemsFromStorage()
      let nextCartId = getNextCartId()
      
      const itemIndex = cartItems.findIndex(item => 
        item.id === id && item.userId === userId
      )
      
      if (itemIndex === -1) {
        reject({
          code: 404,
          message: '购物车商品不存在'
        })
        return
      }
      
      // 更新商品信息
      if (data.quantity !== undefined) {
        cartItems[itemIndex].quantity = data.quantity
        cartItems[itemIndex].totalPrice = cartItems[itemIndex].price * data.quantity
      }
      
      if (data.selected !== undefined) {
        cartItems[itemIndex].selected = data.selected
      }
      
      // 更新地址信息
      if (data.addressId !== undefined) {
        cartItems[itemIndex].addressId = data.addressId
      }
      
      if (data.addressDetails !== undefined) {
        cartItems[itemIndex].addressDetails = data.addressDetails
      }
      
      // 保存到localStorage
      saveCartData(cartItems, nextCartId)
      
      resolve({
        code: 200,
        message: '更新购物车成功',
        data: cartItems[itemIndex]
      })
    }, 300)
  })
}

// 模拟从购物车删除商品
export function removeFromCart(id) {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      const userId = userInfo.id || 1
      let cartItems = getCartItemsFromStorage()
      let nextCartId = getNextCartId()
      
      const itemIndex = cartItems.findIndex(item => 
        item.id === id && item.userId === userId
      )
      
      if (itemIndex === -1) {
        reject({
          code: 404,
          message: '购物车商品不存在'
        })
        return
      }
      
      const removedItem = cartItems.splice(itemIndex, 1)[0]
      
      // 保存到localStorage
      saveCartData(cartItems, nextCartId)
      
      resolve({
        code: 200,
        message: '删除成功',
        data: removedItem
      })
    }, 300)
  })
}

// 模拟清空购物车
export function clearCart() {
  return new Promise((resolve) => {
    setTimeout(() => {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      const userId = userInfo.id || 1
      let cartItems = getCartItemsFromStorage()
      let nextCartId = getNextCartId()
      
      // 清空当前用户的购物车
      cartItems = cartItems.filter(item => item.userId !== userId)
      
      // 保存到localStorage
      saveCartData(cartItems, nextCartId)
      
      resolve({
        code: 200,
        message: '清空购物车成功',
        data: null
      })
    }, 300)
  })
}

// 初始化一些测试数据
export function initMockCartData() {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  const userId = userInfo.id || 1
  let cartItems = getCartItemsFromStorage()
  let nextCartId = getNextCartId()
  
  // 如果购物车为空，添加一些测试数据
  if (cartItems.length === 0) {
    cartItems = [
      {
        id: nextCartId++,
        userId: userId,
        goodsId: 1,
        goodsName: '苹果手机',
        price: 5999,
        quantity: 1,
        totalPrice: 5999,
        image: 'https://picsum.photos/seed/phone1/200/200.jpg',
        selected: true,
        createTime: new Date().toISOString(),
        addressId: null,
        addressDetails: null
      },
      {
        id: nextCartId++,
        userId: userId,
        goodsId: 2,
        goodsName: '华为笔记本',
        price: 6999,
        quantity: 1,
        totalPrice: 6999,
        image: 'https://picsum.photos/seed/laptop1/200/200.jpg',
        selected: true,
        createTime: new Date().toISOString(),
        addressId: null,
        addressDetails: null
      }
    ]
    
    // 保存到localStorage
    saveCartData(cartItems, nextCartId)
  }
}