package com.example.userauth.repository;

import com.example.userauth.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // 根据订单ID查找支付记录
    Optional<Payment> findByOrderId(Long orderId);
    
    // 根据支付状态查找支付记录
    Optional<Payment> findByStatus(int status);
    
    // 更新支付状态
    @Modifying
    @Query("UPDATE Payment p SET p.status = :status, p.transactionId = :transactionId WHERE p.id = :id")
    int updatePaymentStatus(@Param("id") Long id, @Param("status") int status, @Param("transactionId") String transactionId);
}
