package com.example.userauth.service;

import com.example.userauth.entity.Payment;
import com.example.userauth.entity.Order;
import com.example.userauth.dto.UserInfo;

import java.math.BigDecimal;
import java.util.Map;

public interface PaymentService {
    // 创建支付
    Payment createPayment(Long orderId, Long userId, BigDecimal amount, String paymentMethod);
    
    // 获取支付状态
    Payment getPaymentStatus(Long paymentId);
    
    // 处理支付回调
    Payment handlePaymentCallback(Long paymentId, String status, String transactionId);
    
    // 根据订单ID获取支付信息
    Payment getPaymentByOrderId(Long orderId);
    
    // 取消支付
    Payment cancelPayment(Long paymentId);
    
    // 退款
    Payment refundPayment(Long paymentId);
}
