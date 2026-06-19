package com.example.userauth.service.impl;

import com.example.userauth.entity.Payment;
import com.example.userauth.entity.Order;
import com.example.userauth.repository.PaymentRepository;
import com.example.userauth.repository.OrderRepository;
import com.example.userauth.service.PaymentService;
import com.example.userauth.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PaymentServiceImpl implements PaymentService {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderService orderService;

    @Override
    @Transactional
    public Payment createPayment(Long orderId, Long userId, BigDecimal amount, String paymentMethod) {
        // 1. 验证订单是否存在
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        
        // 2. 验证订单状态是否为待付款
        if (order.getStatus() != 1) {
            throw new RuntimeException("只有待付款状态的订单才能支付");
        }
        
        // 3. 验证支付金额与订单金额是否一致
        // 使用compareTo比较BigDecimal值，避免精度问题
        if (order.getPayAmount().compareTo(amount) != 0) {
            throw new RuntimeException("支付金额与订单金额不一致");
        }
        
        // 4. 检查是否已经存在该订单的支付记录
        Payment existingPayment = paymentRepository.findByOrderId(orderId)
                .orElse(null);
        
        if (existingPayment != null) {
            // 如果存在未完成的支付记录，返回该记录
            if (existingPayment.getStatus() == Payment.STATUS_PENDING) {
                return existingPayment;
            } else if (existingPayment.getStatus() == Payment.STATUS_SUCCESS) {
                throw new RuntimeException("该订单已经支付成功");
            }
        }
        
        // 5. 创建新的支付记录
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setUserId(userId);
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus(Payment.STATUS_PENDING);
        payment.setCreateTime(LocalDateTime.now());
        payment.setUpdateTime(LocalDateTime.now());
        payment.setIsDeleted(false);
        
        Payment savedPayment = paymentRepository.save(payment);
        return savedPayment;
    }

    @Override
    public Payment getPaymentStatus(Long paymentId) {
        return paymentRepository.findById(paymentId).orElse(null);
    }

    @Override
    @Transactional
    public Payment handlePaymentCallback(Long paymentId, String status, String transactionId) {
        // 1. 查找支付记录
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("支付记录不存在"));
        
        // 2. 检查支付记录状态，如果已经处理过则不再处理
        if (payment.getStatus() != Payment.STATUS_PENDING) {
            throw new RuntimeException("该支付记录已经处理过");
        }
        
        // 3. 更新支付记录
        int intStatus = Payment.STATUS_PENDING;
        if ("success".equals(status)) {
            intStatus = Payment.STATUS_SUCCESS;
        } else if ("failed".equals(status)) {
            intStatus = Payment.STATUS_FAILED;
        }
        payment.setStatus(intStatus);
        payment.setTransactionId(transactionId);
        payment.setUpdateTime(LocalDateTime.now());
        
        Payment updatedPayment = paymentRepository.save(payment);
        
        // 4. 如果支付成功，更新订单状态为待发货
        if (intStatus == Payment.STATUS_SUCCESS) {
            orderService.updateOrderStatus(payment.getOrderId(), 2, null);
        }
        
        return updatedPayment;
    }

    @Override
    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("支付记录不存在"));
    }

    @Override
    @Transactional
    public Payment cancelPayment(Long paymentId) {
        // 1. 查找支付记录
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("支付记录不存在"));
        
        // 2. 检查支付记录状态，如果已经处理过则不能取消
        if (payment.getStatus() != Payment.STATUS_PENDING) {
            throw new RuntimeException("只有待处理的支付记录才能取消");
        }
        
        // 3. 更新支付记录状态为取消
        payment.setStatus(Payment.STATUS_CANCELLED);
        payment.setUpdateTime(LocalDateTime.now());
        
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public Payment refundPayment(Long paymentId) {
        // 1. 查找支付记录
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("支付记录不存在"));
        
        // 2. 检查支付记录状态，如果不是成功则不能退款
        if (payment.getStatus() != Payment.STATUS_SUCCESS) {
            throw new RuntimeException("只有支付成功的记录才能退款");
        }
        
        // 3. 更新支付记录状态为退款
        payment.setStatus(Payment.STATUS_REFUNDED);
        payment.setUpdateTime(LocalDateTime.now());
        
        // 4. 更新订单状态为已取消
        orderService.updateOrderStatus(payment.getOrderId(), 5, null);
        
        return paymentRepository.save(payment);
    }
}
