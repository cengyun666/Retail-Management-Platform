import request from '../utils/request'

// 创建支付
export async function createPayment(paymentData) {
  try {
    const response = await request.post('/payment', paymentData)
    return {
      success: true,
      data: response.data,
      message: '支付创建成功'
    }
  } catch (error) {
    console.error('创建支付失败:', error)
    return {
      success: false,
      message: error.response?.data?.message || '创建支付失败'
    }
  }
}

// 获取支付状态
export async function getPaymentStatus(paymentId) {
  try {
    const response = await request.get(`/payment/${paymentId}`)
    return {
      success: true,
      data: response.data,
      message: '获取支付状态成功'
    }
  } catch (error) {
    console.error('获取支付状态失败:', error)
    return {
      success: false,
      message: error.response?.data?.message || '获取支付状态失败'
    }
  }
}

// 处理支付回调
export async function handlePaymentCallback(paymentId, callbackData) {
  try {
    const response = await request.post(`/payment/${paymentId}/callback`, callbackData)
    return {
      success: true,
      data: response.data,
      message: '处理支付回调成功'
    }
  } catch (error) {
    console.error('处理支付回调失败:', error)
    return {
      success: false,
      message: error.response?.data?.message || '处理支付回调失败'
    }
  }
}

// 根据订单ID获取支付信息
export async function getPaymentByOrderId(orderId) {
  try {
    const response = await request.get(`/payment/order/${orderId}`)
    return {
      success: true,
      data: response.data,
      message: '获取支付信息成功'
    }
  } catch (error) {
    console.error('获取支付信息失败:', error)
    return {
      success: false,
      message: error.response?.data?.message || '获取支付信息失败'
    }
  }
}

// 取消支付
export async function cancelPayment(paymentId) {
  try {
    const response = await request.put(`/payment/${paymentId}/cancel`)
    return {
      success: true,
      data: response.data,
      message: '取消支付成功'
    }
  } catch (error) {
    console.error('取消支付失败:', error)
    return {
      success: false,
      message: error.response?.data?.message || '取消支付失败'
    }
  }
}

// 退款
export async function refundPayment(paymentId) {
  try {
    const response = await request.put(`/payment/${paymentId}/refund`)
    return {
      success: true,
      data: response.data,
      message: '退款成功'
    }
  } catch (error) {
    console.error('退款失败:', error)
    return {
      success: false,
      message: error.response?.data?.message || '退款失败'
    }
  }
}
