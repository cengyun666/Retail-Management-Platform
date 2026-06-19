package com.example.userauth.repository;

import com.example.userauth.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNo(String orderNo);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.goods WHERE o.id = :id")
    Optional<Order> findByIdWithItemsAndGoods(@Param("id") Long id);
    List<Order> findByUserId(Long userId);
    List<Order> findByStatus(Integer status);
    List<Order> findByUserIdAndStatus(Long userId, Integer status);

    @Query("SELECT o FROM Order o WHERE o.isDeleted = false")
    List<Order> findAllActive();

    @Query("SELECT o FROM Order o WHERE o.isDeleted = false AND o.userId = :userId AND o.userDeleted = false")
    List<Order> findAllActiveByUserId(@Param("userId") Long userId);
    
    @Query("SELECT o FROM Order o WHERE o.isDeleted = false AND o.userId = :userId AND o.userDeleted = false AND o.status = :status")
    List<Order> findAllActiveByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);

    @Query("SELECT o FROM Order o WHERE o.isDeleted = false AND o.userId = :userId AND o.userDeleted = false AND o.orderNo LIKE %:orderNo%")
    List<Order> findAllActiveByUserIdAndOrderNo(@Param("userId") Long userId, @Param("orderNo") String orderNo);

    @Query("SELECT o FROM Order o WHERE o.isDeleted = false AND o.orderNo LIKE %:orderNo%")
    List<Order> findAllActiveByOrderNo(@Param("orderNo") String orderNo);

    @Query("SELECT o FROM Order o WHERE o.isDeleted = false AND o.status = :status")
    List<Order> findAllActiveByStatus(@Param("status") Integer status);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi JOIN oi.goods g WHERE o.isDeleted = false AND g.merchantId = :merchantId AND o.merchantDeleted = false")
    List<Order> findAllActiveByMerchantId(@Param("merchantId") Long merchantId);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi JOIN oi.goods g WHERE o.isDeleted = false AND g.merchantId = :merchantId AND o.merchantDeleted = false AND o.status = :status")
    List<Order> findAllActiveByMerchantIdAndStatus(@Param("merchantId") Long merchantId, @Param("status") Integer status);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi JOIN oi.goods g WHERE o.isDeleted = false AND g.merchantId = :merchantId AND o.merchantDeleted = false AND o.orderNo LIKE %:orderNo%")
    List<Order> findAllActiveByMerchantIdAndOrderNo(@Param("merchantId") Long merchantId, @Param("orderNo") String orderNo);

    @Query("SELECT COUNT(oi) FROM OrderItem oi JOIN oi.goods g WHERE oi.order.id = :orderId AND g.merchantId = :merchantId")
    long countOrderItemsByOrderIdAndMerchantId(@Param("orderId") Long orderId, @Param("merchantId") Long merchantId);
}
