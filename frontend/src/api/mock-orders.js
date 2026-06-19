// 模拟订单数据
const mockOrders = [
    {
        id: '1',
        orderNo: 'ORD202501010001',
        userId: '1',
        userName: '管理员',
        merchantId: '7', // 商家ID
        totalAmount: 199.99,
        status: 1, // 1:待付款, 2:待发货, 3:待收货, 4:已完成, 5:已取消
        createTime: '2025-01-01 10:00:00',
        payTime: null,
        deliverTime: null,
        finishTime: null,
        orderItems: [
            {
                id: '1',
                orderId: '1',
                goodsId: '1',
                goodsName: '示例商品1',
                price: 99.99,
                quantity: 1
            },
            {
                id: '2',
                orderId: '1',
                goodsId: '2',
                goodsName: '示例商品2',
                price: 99.99,
                quantity: 1
            }
        ]
    },
    {
        id: '2',
        orderNo: 'ORD202501010002',
        userId: '2',
        userName: '小明',
        merchantId: '7', // 商家ID
        totalAmount: 299.99,
        status: 4,
        createTime: '2025-01-02 15:30:00',
        payTime: '2025-01-02 15:35:00',
        deliverTime: '2025-01-03 10:00:00',
        finishTime: '2025-01-05 16:00:00',
        orderItems: [
            {
                id: '3',
                orderId: '2',
                goodsId: '3',
                goodsName: '示例商品3',
                price: 149.99,
                quantity: 2
            }
        ]
    },
    {
        id: '3',
        orderNo: 'ORD202501010003',
        userId: '3',
        userName: '小红',
        merchantId: '7', // 商家ID
        totalAmount: 49.99,
        status: 2,
        createTime: '2025-01-03 09:15:00',
        payTime: '2025-01-03 09:20:00',
        deliverTime: null,
        finishTime: null,
        orderItems: [
            {
                id: '4',
                orderId: '3',
                goodsId: '4',
                goodsName: '示例商品4',
                price: 49.99,
                quantity: 1
            }
        ]
    },
    {
        id: '4',
        orderNo: 'ORD202501010004',
        userId: '4',
        userName: '小兰',
        merchantId: '7', // 商家ID
        totalAmount: 129.99,
        status: 3,
        createTime: '2025-01-04 14:20:00',
        payTime: '2025-01-04 14:25:00',
        deliverTime: '2025-01-04 16:00:00',
        finishTime: null,
        orderItems: [
            {
                id: '5',
                orderId: '4',
                goodsId: '5',
                goodsName: '示例商品5',
                price: 129.99,
                quantity: 1
            }
        ]
    },
    {
        id: '5',
        orderNo: 'ORD202501010005',
        userId: '2',
        userName: '小明',
        merchantId: '7', // 商家ID
        totalAmount: 79.99,
        status: 5,
        createTime: '2025-01-05 11:45:00',
        payTime: null,
        deliverTime: null,
        finishTime: null,
        orderItems: [
            {
                id: '6',
                orderId: '5',
                goodsId: '6',
                goodsName: '示例商品6',
                price: 79.99,
                quantity: 1
            }
        ]
    },
    {
        id: '6',
        orderNo: 'ORD202511270006',
        userId: '1',
        userName: 'user001',
        merchantId: '7', // 商家ID
        totalAmount: 5999.00,
        status: 3, // 待收货
        createTime: '2025-11-27 10:00:00',
        payTime: '2025-11-27 10:05:00',
        deliverTime: '2025-11-27 15:00:00',
        finishTime: null,
        orderItems: [
            {
                id: '7',
                orderId: '6',
                goodsId: '1',
                goodsName: '苹果手机',
                price: 5999.00,
                quantity: 1
            }
        ]
    }
]

// 模拟API延迟
const delay = (ms = 300) => new Promise(resolve => setTimeout(resolve, ms))

// 从localStorage加载订单数据
const loadOrdersFromStorage = () => {
  try {
    const storedOrders = localStorage.getItem('mockOrders')
    if (storedOrders) {
      return JSON.parse(storedOrders)
    }
  } catch (error) {
    console.error('加载订单数据失败:', error)
  }
  return mockOrders
}

// 保存订单数据到localStorage
const saveOrdersToStorage = (orders) => {
  try {
    localStorage.setItem('mockOrders', JSON.stringify(orders))
  } catch (error) {
    console.error('保存订单数据失败:', error)
  }
}

// 获取订单列表
export async function getOrdersList(params = {}) {
    await delay()
    
    // 从localStorage加载订单数据
    let result = loadOrdersFromStorage()
    
    // 用户ID过滤 - 如果指定了userId，只返回该用户的订单
    if (params.userId) {
        result = result.filter(order => order.userId === params.userId)
    }
    
    // 商家ID过滤 - 如果指定了merchantId，只返回该商家的订单
    if (params.merchantId) {
        result = result.filter(order => order.merchantId === params.merchantId)
    }
    
    // 订单编号过滤
    if (params.orderNo) {
        const query = params.orderNo.toLowerCase()
        result = result.filter(order => 
            order.orderNo.toLowerCase().includes(query)
        )
    }
    
    // 用户名称过滤
    if (params.userName) {
        const query = params.userName.toLowerCase()
        result = result.filter(order => 
            order.userName.toLowerCase().includes(query)
        )
    }
    
    // 兼容旧的搜索参数（如果同时传递了search参数）
    if (params.search && !params.orderNo && !params.userName) {
        const query = params.search.toLowerCase()
        result = result.filter(order => 
            order.orderNo.toLowerCase().includes(query) ||
            order.userName.toLowerCase().includes(query)
        )
    }
    
    // 状态过滤
    if (params.status) {
        result = result.filter(order => order.status === parseInt(params.status))
    }
    
    // 分页处理
    const page = parseInt(params.page) || 1
    const pageSize = parseInt(params.pageSize) || 10
    const total = result.length
    const start = (page - 1) * pageSize
    const end = start + pageSize
    const list = result.slice(start, end)
    
    return {
        code: 200,
        message: '获取订单列表成功',
        data: {
            list,
            total,
            page,
            pageSize
        }
    }
}

// 根据ID获取订单详情
export async function getOrderById(id) {
    await delay()
    
    // 从localStorage加载订单数据
    const orders = loadOrdersFromStorage()
    const order = orders.find(item => item.id === id)
    
    if (!order) {
        return {
            code: 404,
            message: '订单不存在',
            data: null
        }
    }
    
    return {
        code: 200,
        message: '获取订单详情成功',
        data: order
    }
}

// 更新订单状态
export async function updateOrderStatus(id, status) {
    await delay()
    
    // 从localStorage加载订单数据
    const orders = loadOrdersFromStorage()
    const orderIndex = orders.findIndex(order => order.id === id)
    
    if (orderIndex === -1) {
        return {
            code: 404,
            message: '订单不存在',
            data: null
        }
    }
    
    // 更新订单状态
    orders[orderIndex].status = status
    
    // 根据状态更新相应时间
    if (status === 2 && !orders[orderIndex].payTime) {
        orders[orderIndex].payTime = new Date().toLocaleString('zh-CN')
    } else if (status === 3 && !orders[orderIndex].deliverTime) {
        orders[orderIndex].deliverTime = new Date().toLocaleString('zh-CN')
    } else if (status === 4 && !orders[orderIndex].finishTime) {
        orders[orderIndex].finishTime = new Date().toLocaleString('zh-CN')
    }
    
    // 保存更新后的订单数据
    saveOrdersToStorage(orders)
    
    return {
        code: 200,
        message: '更新订单状态成功',
        data: orders[orderIndex]
    }
}

// 删除订单
export async function deleteOrder(id) {
    await delay()
    
    // 从localStorage加载订单数据
    const orders = loadOrdersFromStorage()
    const orderIndex = orders.findIndex(order => order.id === id)
    
    if (orderIndex === -1) {
        return {
            code: 404,
            message: '订单不存在',
            data: null
        }
    }
    
    const deletedOrder = orders[orderIndex]
    orders.splice(orderIndex, 1)
    
    // 保存更新后的订单数据
    saveOrdersToStorage(orders)
    
    return {
        code: 200,
        message: '删除订单成功',
        data: deletedOrder
    }
}

// 创建订单
export async function createOrder(orderData) {
  await delay()
  
  // 从localStorage加载订单数据
  const orders = loadOrdersFromStorage()
  
  // 获取当前最大ID值，确保ID唯一性
  const maxId = orders.length > 0 ? Math.max(...orders.map(order => parseInt(order.id))) : 0
  const newId = String(maxId + 1)
  
  // 获取当前日期，用于生成订单号
  const now = new Date()
  const dateStr = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}`
  
  // 查找当天订单的最大序号
  const todayOrders = orders.filter(order => order.orderNo.includes(`ORD${dateStr}`))
  const maxOrderNo = todayOrders.length > 0 
    ? Math.max(...todayOrders.map(order => parseInt(order.orderNo.slice(-4))))
    : 0
  
  // 生成唯一订单号
  const orderNo = `ORD${dateStr}${String(maxOrderNo + 1).padStart(4, '0')}`
  
  // 创建新订单
  const newOrder = {
    id: newId,
    orderNo,
    userId: orderData.userId,
    userName: orderData.userName,
    merchantId: orderData.merchantId || '7', // 默认商家ID为'7'，与商家用户ID一致
    totalAmount: orderData.totalAmount,
    status: 1, // 默认待付款
    createTime: new Date().toLocaleString('zh-CN'),
    payTime: null,
    deliverTime: null,
    finishTime: null,
    orderItems: orderData.goods.map((item, index) => ({
      id: String(parseInt(newId) + index + 1),
      orderId: newId,
      goodsId: item.id,
      goodsName: item.name,
      price: item.price,
      quantity: item.quantity
    }))
  }
  
  // 添加到订单数据中
  orders.push(newOrder)
  
  // 保存更新后的订单数据
  saveOrdersToStorage(orders)
  
  return {
    code: 200,
    data: newOrder,
    message: '创建订单成功'
  }
}

// 获取订单状态列表
export async function getOrderStatusList() {
    await delay()
    
    return {
        code: 200,
        message: '获取订单状态列表成功',
        data: [
            { value: 1, label: '待付款' },
            { value: 2, label: '待发货' },
            { value: 3, label: '待收货' },
            { value: 4, label: '已完成' },
            { value: 5, label: '已取消' }
        ]
    }
}

// 确认收货
export async function confirmReceipt(id) {
    await delay()
    
    // 从localStorage加载订单数据
    const orders = loadOrdersFromStorage()
    const orderIndex = orders.findIndex(order => order.id === id)
    
    if (orderIndex === -1) {
        return {
            code: 404,
            message: '订单不存在',
            data: null
        }
    }
    
    // 检查订单状态是否为待收货
    if (orders[orderIndex].status !== 3) {
        return {
            code: 400,
            message: '只有待收货状态的订单才能确认收货',
            data: null
        }
    }
    
    // 更新订单状态为已完成
    orders[orderIndex].status = 4
    orders[orderIndex].finishTime = new Date().toLocaleString('zh-CN')
    
    // 保存更新后的订单数据
    saveOrdersToStorage(orders)
    
    return {
        code: 200,
        message: '确认收货成功',
        data: orders[orderIndex]
    }
}

// 取消订单
export async function cancelOrder(id) {
    await delay()
    
    // 从localStorage加载订单数据
    const orders = loadOrdersFromStorage()
    const orderIndex = orders.findIndex(order => order.id === id)
    
    if (orderIndex === -1) {
        return {
            code: 404,
            message: '订单不存在',
            data: null
        }
    }
    
    // 检查订单状态是否可以取消
    if (orders[orderIndex].status === 4) {
        return {
            code: 400,
            message: '已完成的订单不能取消',
            data: null
        }
    }
    
    if (orders[orderIndex].status === 5) {
        return {
            code: 400,
            message: '订单已经是取消状态',
            data: null
        }
    }
    
    // 更新订单状态为已取消
    orders[orderIndex].status = 5
    
    // 保存更新后的订单数据
    saveOrdersToStorage(orders)
    
    return {
        code: 200,
        message: '取消订单成功',
        data: orders[orderIndex]
    }
}

// 导出订单数据，供其他模块使用
export const ordersData = loadOrdersFromStorage()

