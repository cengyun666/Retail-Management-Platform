// 模拟支付数据
let payments = []
let paymentIdCounter = 1000

// 导入订单API中的updateOrderStatus函数
import { updateOrderStatus } from './mock-orders.js'

// 初始化支付数据
const initMockPaymentData = () => {
  if (payments.length === 0) {
    // 创建一些示例支付记录
    payments = [
      {
        id: 1001,
        orderId: 2001,
        userId: 1,
        amount: 299.00,
        paymentMethod: 'alipay',
        status: 'success', // pending, success, failed, cancelled, refunded
        paymentTime: '2023-06-15 14:30:25',
        transactionId: 'TXN20230615143025001',
        createTime: '2023-06-15 14:25:10',
        updateTime: '2023-06-15 14:30:25'
      },
      {
        id: 1002,
        orderId: 2002,
        userId: 2,
        amount: 158.50,
        paymentMethod: 'wechat',
        status: 'pending',
        paymentTime: null,
        transactionId: null,
        createTime: '2023-06-16 09:15:30',
        updateTime: '2023-06-16 09:15:30'
      }
    ]
    paymentIdCounter = 1003
  }
}

// 创建支付
const createPayment = async (paymentData) => {
  initMockPaymentData()
  
  try {
    const newPayment = {
      id: paymentIdCounter++,
      orderId: paymentData.orderId,
      userId: paymentData.userId,
      amount: paymentData.amount,
      paymentMethod: paymentData.paymentMethod || 'alipay',
      status: 'pending',
      paymentTime: null,
      transactionId: null,
      createTime: new Date().toISOString().replace('T', ' ').substring(0, 19),
      updateTime: new Date().toISOString().replace('T', ' ').substring(0, 19)
    }
    
    payments.push(newPayment)
    
    // 模拟支付处理（总是成功）
    setTimeout(() => {
      const paymentIndex = payments.findIndex(p => p.id === newPayment.id)
      if (paymentIndex !== -1) {
        // 100% 成功率，移除支付失败逻辑
        payments[paymentIndex].status = 'success'
        payments[paymentIndex].paymentTime = new Date().toISOString().replace('T', ' ').substring(0, 19)
        payments[paymentIndex].transactionId = `TXN${Date.now()}`
        payments[paymentIndex].updateTime = new Date().toISOString().replace('T', ' ').substring(0, 19)
        console.log(`支付 ${newPayment.id} 状态更新为: ${payments[paymentIndex].status}`)
        
        // 支付成功后，更新订单状态为"待发货"(状态值2)
        updateOrderStatus(newPayment.orderId, 2)
          .then(result => {
            if (result.success) {
              console.log(`订单 ${newPayment.orderId} 状态已更新为待发货`)
            } else {
              console.error(`更新订单 ${newPayment.orderId} 状态失败:`, result.message)
            }
          })
          .catch(error => {
            console.error(`更新订单 ${newPayment.orderId} 状态时发生错误:`, error)
          })
      }
    }, 2000) // 2秒后模拟支付结果
    
    return {
      success: true,
      data: newPayment,
      message: '支付创建成功，请等待支付结果'
    }
  } catch (error) {
    return {
      success: false,
      message: '创建支付失败'
    }
  }
}

// 获取支付状态
const getPaymentStatus = async (paymentId) => {
  initMockPaymentData()
  
  try {
    const payment = payments.find(p => p.id === paymentId)
    if (!payment) {
      return {
        success: false,
        message: '支付记录不存在'
      }
    }
    
    return {
      success: true,
      data: payment,
      message: '获取支付状态成功'
    }
  } catch (error) {
    return {
      success: false,
      message: '获取支付状态失败'
    }
  }
}

// 取消支付
const cancelPayment = async (paymentId) => {
  initMockPaymentData()
  
  try {
    const paymentIndex = payments.findIndex(p => p.id === paymentId)
    if (paymentIndex === -1) {
      return {
        success: false,
        message: '支付记录不存在'
      }
    }
    
    if (payments[paymentIndex].status !== 'pending') {
      return {
        success: false,
        message: '只能取消待支付状态的订单'
      }
    }
    
    payments[paymentIndex].status = 'cancelled'
    payments[paymentIndex].updateTime = new Date().toISOString().replace('T', ' ').substring(0, 19)
    
    return {
      success: true,
      data: payments[paymentIndex],
      message: '支付已取消'
    }
  } catch (error) {
    return {
      success: false,
      message: '取消支付失败'
    }
  }
}

// 获取支付列表
const getPaymentList = async (params = {}) => {
  initMockPaymentData()
  
  try {
    const { page = 1, pageSize = 10, userId, status, orderId } = params
    let filteredPayments = [...payments]
    
    // 过滤条件
    if (userId) {
      filteredPayments = filteredPayments.filter(p => p.userId === userId)
    }
    
    if (status) {
      filteredPayments = filteredPayments.filter(p => p.status === status)
    }
    
    if (orderId) {
      filteredPayments = filteredPayments.filter(p => p.orderId === orderId)
    }
    
    // 排序（按创建时间倒序）
    filteredPayments.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
    
    // 分页
    const total = filteredPayments.length
    const startIndex = (page - 1) * pageSize
    const endIndex = startIndex + pageSize
    const list = filteredPayments.slice(startIndex, endIndex)
    
    return {
      success: true,
      data: {
        list,
        total,
        page: parseInt(page),
        pageSize: parseInt(pageSize)
      },
      message: '获取支付列表成功'
    }
  } catch (error) {
    return {
      success: false,
      message: '获取支付列表失败'
    }
  }
}

// 申请退款
const refundPayment = async (paymentId, refundReason = '') => {
  initMockPaymentData()
  
  try {
    const paymentIndex = payments.findIndex(p => p.id === paymentId)
    if (paymentIndex === -1) {
      return {
        success: false,
        message: '支付记录不存在'
      }
    }
    
    if (payments[paymentIndex].status !== 'success') {
      return {
        success: false,
        message: '只能对已支付的订单申请退款'
      }
    }
    
    // 模拟退款处理
    setTimeout(() => {
      const payment = payments.find(p => p.id === paymentId)
      if (payment) {
        payment.status = 'refunded'
        payment.updateTime = new Date().toISOString().replace('T', ' ').substring(0, 19)
        payment.refundReason = refundReason
        payment.refundTime = new Date().toISOString().replace('T', ' ').substring(0, 19)
      }
    }, 2000) // 2秒后模拟退款完成
    
    return {
      success: true,
      message: '退款申请已提交，请等待处理'
    }
  } catch (error) {
    return {
      success: false,
      message: '申请退款失败'
    }
  }
}

export {
  createPayment,
  getPaymentStatus,
  cancelPayment,
  getPaymentList,
  refundPayment,
  initMockPaymentData
}