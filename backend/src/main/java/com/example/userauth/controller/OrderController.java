package com.example.userauth.controller;

import com.example.userauth.entity.Order;
import com.example.userauth.entity.OrderItem;
import com.example.userauth.service.OrderService;
import com.example.userauth.dto.ApiResponse;
import com.example.userauth.dto.UserInfo;
import com.example.userauth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    
    @Autowired
    private OrderService orderService;
    @Autowired
    private UserService userService;

    // 获取订单列表
    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> getOrdersList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer status,
            @RequestHeader(value = "Authorization", required = false) String token) {

        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("pageSize", pageSize);
        params.put("orderNo", orderNo);
        params.put("status", status);

        // 移除Bearer前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        UserInfo currentUser = userService.getUserInfoByToken(token);
        Map<String, Object> result = orderService.getOrdersList(params, currentUser);
        return ApiResponse.success("获取订单列表成功", result);
    }

    // 获取订单详情
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getOrderById(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        UserInfo currentUser = null;
        
        // 尝试获取用户信息，如果token无效则忽略
        try {
            // 移除Bearer前缀
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            if (token != null) {
                currentUser = userService.getUserInfoByToken(token);
            }
        } catch (Exception e) {
            logger.warn("获取用户信息失败，将使用匿名访问: {}", e.getMessage());
        }
        
        Map<String, Object> order = orderService.getOrderById(id, currentUser);
        return ApiResponse.success("获取订单详情成功", order);
    }

    // 创建订单
    @PostMapping("/create")
    public ApiResponse<Order> createOrder(@RequestBody Map<String, Object> orderData) {
        try {
            logger.debug("Received order creation request: {}", orderData);
            
            // 创建Order对象
            Order order = new Order();
            
            // 空值检查和类型转换
            if (orderData.get("userId") == null) {
                logger.error("User ID is null in order creation request");
                return ApiResponse.error("用户ID不能为空");
            }
            if (orderData.get("totalAmount") == null) {
                logger.error("Total amount is null in order creation request");
                return ApiResponse.error("总金额不能为空");
            }
            
            Long userId = Long.valueOf(orderData.get("userId").toString());
            order.setUserId(userId);
            logger.debug("Order userId set to: {}", userId);
            
            BigDecimal totalAmount = new BigDecimal(orderData.get("totalAmount").toString());
            order.setTotalAmount(totalAmount);
            order.setPayAmount(totalAmount);
            logger.debug("Order amount set to: {}", totalAmount);
            
            order.setAddress(orderData.get("address") != null ? orderData.get("address").toString() : null);
            order.setRemark(orderData.get("remark") != null ? orderData.get("remark").toString() : null);
            logger.debug("Order address: {}, remark: {}", order.getAddress(), order.getRemark());
            
            // 处理订单项数据
            List<Map<String, Object>> goodsList = (List<Map<String, Object>>) orderData.get("goods");
            if (goodsList == null || goodsList.isEmpty()) {
                logger.error("Goods list is null or empty in order creation request");
                return ApiResponse.error("商品列表不能为空");
            }
            
            List<OrderItem> orderItems = new ArrayList<>();
            
            for (Map<String, Object> goods : goodsList) {
                OrderItem orderItem = new OrderItem();
                
                // 订单项空值检查
                if (goods.get("id") == null || goods.get("name") == null || 
                    goods.get("price") == null || goods.get("quantity") == null) {
                    logger.error("Incomplete goods information in order creation request: {}", goods);
                    return ApiResponse.error("商品信息不完整");
                }
                
                Long goodsId = Long.valueOf(goods.get("id").toString());
                String goodsName = goods.get("name").toString();
                BigDecimal price = new BigDecimal(goods.get("price").toString());
                Integer quantity = Integer.valueOf(goods.get("quantity").toString());
                
                orderItem.setGoodsId(goodsId);
                orderItem.setGoodsName(goodsName);
                orderItem.setPrice(price);
                orderItem.setQuantity(quantity);
                orderItem.setOrder(order);
                orderItems.add(orderItem);
                
                logger.debug("Added order item: goodsId={}, goodsName={}, price={}, quantity={}", 
                    goodsId, goodsName, price, quantity);
            }
            
            // 设置订单项
            order.setOrderItems(orderItems);
            logger.debug("Order items list size: {}", orderItems.size());
            
            Order createdOrder = orderService.createOrder(order);
            logger.info("Order created successfully - ID: {}, OrderNo: {}, UserId: {}, TotalAmount: {}, ItemsCount: {}", 
                createdOrder.getId(), createdOrder.getOrderNo(), createdOrder.getUserId(), 
                createdOrder.getTotalAmount(), createdOrder.getOrderItems().size());
            return ApiResponse.success("创建订单成功", createdOrder);
        } catch (Exception e) {
            logger.error("Order creation failed: {}", e.getMessage(), e);
            return ApiResponse.error("订单创建失败：" + e.getMessage());
        }
    }

    // 更新订单状态
    @PutMapping("/{id}/status")
    public ApiResponse<Order> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestMap) {

        // 安全地转换status类型，支持字符串和数字输入
        Integer status = null;
        Object statusObj = requestMap.get("status");
        if (statusObj != null) {
            if (statusObj instanceof Integer) {
                status = (Integer) statusObj;
            } else if (statusObj instanceof String) {
                try {
                    status = Integer.parseInt((String) statusObj);
                } catch (NumberFormatException e) {
                    return ApiResponse.error("无效的订单状态");
                }
            } else if (statusObj instanceof Number) {
                status = ((Number) statusObj).intValue();
            }
        }
        
        // 安全地转换merchantId类型
        Long merchantId = null;
        Object merchantIdObj = requestMap.get("merchantId");
        if (merchantIdObj != null) {
            if (merchantIdObj instanceof Long) {
                merchantId = (Long) merchantIdObj;
            } else if (merchantIdObj instanceof Number) {
                merchantId = ((Number) merchantIdObj).longValue();
            } else if (merchantIdObj instanceof String) {
                try {
                    merchantId = Long.parseLong((String) merchantIdObj);
                } catch (NumberFormatException e) {
                    return ApiResponse.error("无效的商家ID");
                }
            }
        }
        
        Order updatedOrder = orderService.updateOrderStatus(id, status, merchantId);
        return ApiResponse.success("更新订单状态成功", updatedOrder);
    }

    // 删除订单
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteOrder(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        // 移除Bearer前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        UserInfo currentUser = userService.getUserInfoByToken(token);
        orderService.deleteOrder(id, currentUser);
        return ApiResponse.success("删除订单成功");
    }

    // 确认收货
    @PutMapping("/{id}/confirm-receipt")
    public ApiResponse<Order> confirmReceipt(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        // 移除Bearer前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        UserInfo currentUser = userService.getUserInfoByToken(token);
        Order order = orderService.confirmReceipt(id, currentUser);
        return ApiResponse.success("确认收货成功", order);
    }

    // 取消订单
    @PutMapping("/{id}/cancel")
    public ApiResponse<Order> cancelOrder(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        // 移除Bearer前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        UserInfo currentUser = userService.getUserInfoByToken(token);
        Order order = orderService.cancelOrder(id, currentUser);
        return ApiResponse.success("取消订单成功", order);
    }

    // 获取订单状态列表
    @GetMapping("/status-list")
    public ApiResponse<List<Map<String, Object>>> getOrderStatusList() {
        List<Map<String, Object>> statusList = orderService.getOrderStatusList();
        return ApiResponse.success("获取订单状态列表成功", statusList);
    }

    // 获取用户订单列表
    @GetMapping("/user/{userId}")
    public ApiResponse<Map<String, Object>> getUserOrdersList(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {

        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("pageSize", pageSize);
        params.put("status", status);

        Map<String, Object> result = orderService.getUserOrdersList(userId, params);
        return ApiResponse.success("获取用户订单列表成功", result);
    }
    
    // 获取商家订单列表
    @GetMapping("/merchant/{merchantId}")
    public ApiResponse<Map<String, Object>> getMerchantOrdersList(
            @PathVariable Long merchantId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer status) {

        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("pageSize", pageSize);
        params.put("orderNo", orderNo);
        params.put("status", status);

        Map<String, Object> result = orderService.getMerchantOrdersList(merchantId, params);
        return ApiResponse.success("获取商家订单列表成功", result);
    }
}
