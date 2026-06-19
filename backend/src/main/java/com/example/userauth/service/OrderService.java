package com.example.userauth.service;

import com.example.userauth.entity.Order;
import com.example.userauth.entity.OrderItem;
import com.example.userauth.dto.ApiResponse;
import com.example.userauth.dto.UserInfo;
import java.util.List;
import java.util.Map;

public interface OrderService {
    // 获取订单列表（带权限控制）
    Map<String, Object> getOrdersList(Map<String, Object> params, UserInfo currentUser);

    // 根据ID获取订单详情（带权限控制）
    Map<String, Object> getOrderById(Long id, UserInfo currentUser);

    // 根据订单编号获取订单
    Order getOrderByOrderNo(String orderNo);

    // 创建订单
    Order createOrder(Order order);

    // 更新订单状态
    Order updateOrderStatus(Long id, Integer status, Long merchantId);

    // 确认收货（带权限控制）
    Order confirmReceipt(Long id, UserInfo currentUser);

    // 取消订单（带权限控制）
    Order cancelOrder(Long id, UserInfo currentUser);

    // 删除订单（软删除，带权限控制）
    void deleteOrder(Long id, UserInfo currentUser);

    // 获取订单状态列表
    List<Map<String, Object>> getOrderStatusList();

    // 获取用户订单列表
    Map<String, Object> getUserOrdersList(Long userId, Map<String, Object> params);
    
    // 获取商家订单列表
    Map<String, Object> getMerchantOrdersList(Long merchantId, Map<String, Object> params);
}
