package com.example.userauth.service.impl;

import com.example.userauth.entity.Order;
import com.example.userauth.entity.OrderItem;
import com.example.userauth.entity.Goods;
import com.example.userauth.entity.User;
import com.example.userauth.repository.OrderRepository;
import com.example.userauth.repository.OrderItemRepository;
import com.example.userauth.service.OrderService;
import com.example.userauth.service.UserService;
import com.example.userauth.service.GoodsService;
import com.example.userauth.dto.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserService userService;
    
    @Autowired
    private GoodsService goodsService;

    // 订单状态列表
    private static final List<Map<String, Object>> ORDER_STATUS_LIST;
    static {
        ORDER_STATUS_LIST = Arrays.asList(
                createStatusMap(1, "待付款"),
                createStatusMap(2, "待发货"),
                createStatusMap(3, "待收货"),
                createStatusMap(4, "已完成"),
                createStatusMap(5, "已取消")
        );
    }

    private static Map<String, Object> createStatusMap(int value, String label) {
        Map<String, Object> map = new HashMap<>();
        map.put("value", value);
        map.put("label", label);
        return map;
    }

    @Override
    public Map<String, Object> getOrdersList(Map<String, Object> params, UserInfo currentUser) {
        // 支持从0或1开始的页码
        int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        // 如果页码是0，则转换为1（因为手动分页从1开始计算）
        if (page <= 0) {
            page = 1;
        }
        int pageSize = params.get("pageSize") != null ? Integer.parseInt(params.get("pageSize").toString()) : 10;
        String orderNo = params.get("orderNo") != null ? params.get("orderNo").toString() : null;
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;

        List<Order> orders = new ArrayList<>();

        // 根据用户角色进行权限控制
        if (currentUser == null) {
            throw new RuntimeException("Please login to view orders");
        }

        String userRole = currentUser.getRole() != null ? currentUser.getRole().toLowerCase() : "";

        if ("user".equals(userRole)) {
            // 普通用户只能查看自己的订单
            if (orderNo != null && !orderNo.isEmpty()) {
                // 使用带订单编号过滤的活跃订单查询
                orders = orderRepository.findAllActiveByUserIdAndOrderNo(currentUser.getId(), orderNo);
            } else if (status != null) {
                // 使用带状态过滤的活跃订单查询，避免手动过滤
                orders = orderRepository.findAllActiveByUserIdAndStatus(currentUser.getId(), status);
            } else {
                // 直接使用查询活跃订单的方法，避免手动过滤
                orders = orderRepository.findAllActiveByUserId(currentUser.getId());
            }
        } else if ("merchant".equals(userRole) || "admin".equals(userRole)) {
            // 商家和管理员都可以查看所有订单
            if (orderNo != null && !orderNo.isEmpty()) {
                List<Order> tempOrders = orderRepository.findAllActiveByOrderNo(orderNo);
                if (tempOrders != null) {
                    orders = tempOrders;
                }
            } else if (status != null) {
                List<Order> tempOrders = orderRepository.findAllActiveByStatus(status);
                if (tempOrders != null) {
                    orders = tempOrders;
                }
            } else {
                List<Order> tempOrders = orderRepository.findAllActive();
                if (tempOrders != null) {
                    orders = tempOrders;
                }
            }
        } else {
            // 其他角色默认不允许访问
            throw new RuntimeException("You don't have permission to view orders");
        }

        // 手动分页
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, orders.size());
        List<Order> pagedOrders = (start < end) ? orders.subList(start, end) : new ArrayList<>();
        
        // 为每个订单添加用户名信息
        List<Map<String, Object>> ordersWithUserName = new ArrayList<>();
        for (Order order : pagedOrders) {
            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("id", order.getId());
            orderMap.put("orderNo", order.getOrderNo());
            orderMap.put("userId", order.getUserId());
            // 将BigDecimal转换为Double返回给前端
            orderMap.put("totalAmount", order.getTotalAmount().doubleValue());
            orderMap.put("payAmount", order.getPayAmount().doubleValue());
            orderMap.put("status", order.getStatus());
            orderMap.put("address", order.getAddress());
            orderMap.put("remark", order.getRemark());
            orderMap.put("createTime", order.getCreateTime());
            orderMap.put("updateTime", order.getUpdateTime());
            orderMap.put("isDeleted", order.getIsDeleted());
            
            // 获取用户名信息
            try {
                UserInfo userInfo = userService.getUserInfoById(order.getUserId());
                orderMap.put("userName", userInfo.getName() != null ? userInfo.getName() : userInfo.getUsername());
            } catch (Exception e) {
                orderMap.put("userName", "未知用户");
            }
            
            ordersWithUserName.add(orderMap);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", ordersWithUserName);
        result.put("total", orders.size());
        result.put("page", page);
        result.put("pageSize", pageSize);

        return result;
    }

    // 权限检查方法
    private void checkOrderPermission(Order order, UserInfo currentUser) {
        if (currentUser == null) {
            throw new RuntimeException("Please login to perform this action");
        }
        
        // 管理员和商家可以操作所有订单
        String userRole = currentUser.getRole() != null ? currentUser.getRole().toLowerCase() : "";
        if (!"admin".equals(userRole) && !"merchant".equals(userRole)) {
            // 普通用户只能操作自己的订单
            if ("user".equals(userRole)) {
                if (!order.getUserId().equals(currentUser.getId())) {
                    throw new RuntimeException("You don't have permission to access this order");
                }
            } else {
                // 其他角色默认不允许访问
                throw new RuntimeException("You don't have permission to perform this action");
            }
        }
    }

    @Override
    public Map<String, Object> getOrderById(Long id, UserInfo currentUser) {
        Optional<Order> orderOptional = orderRepository.findByIdWithItemsAndGoods(id);
        
        Order order = orderOptional
                .filter(o -> o.getIsDeleted() == null || !o.getIsDeleted())
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // 检查用户或商家是否已删除该订单
        if (currentUser != null) {
            String userRole = currentUser.getRole() != null ? currentUser.getRole().toLowerCase() : "";
            if ("user".equals(userRole)) {
                // 普通用户检查自己是否已删除该订单
                if (order.getUserDeleted() != null && order.getUserDeleted()) {
                    throw new RuntimeException("Order not found");
                }
            } else if ("merchant".equals(userRole)) {
                // 商家检查自己是否已删除该订单
                if (order.getMerchantDeleted() != null && order.getMerchantDeleted()) {
                    throw new RuntimeException("Order not found");
                }
            }
        }
        
        // 添加调试信息
        System.out.println("Order found: " + order);
        System.out.println("Order remark: " + order.getRemark());
        System.out.println("Order items count: " + (order.getOrderItems() != null ? order.getOrderItems().size() : 0));
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            for (OrderItem item : order.getOrderItems()) {
                System.out.println("Order item: " + item);
                System.out.println("  Goods: " + item.getGoods());
                System.out.println("  Price: " + item.getPrice());
                System.out.println("  Quantity: " + item.getQuantity());
            }
        }
        
        // 权限检查
        if (currentUser == null) {
            throw new RuntimeException("Please login to view order details");
        }
        
        // 管理员可以查看所有订单
        String userRole = currentUser.getRole() != null ? currentUser.getRole().toLowerCase() : "";
        if (!"admin".equals(userRole)) {
            // 普通用户只能查看自己的订单
            if ("user".equals(userRole)) {
                if (!order.getUserId().equals(currentUser.getId())) {
                    throw new RuntimeException("You don't have permission to view this order");
                }
            } else if ("merchant".equals(userRole)) {
                // 商家只能查看包含自己商品的订单
                boolean hasMerchantGoods = false;
                if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
                    for (OrderItem item : order.getOrderItems()) {
                        if (item.getGoods() != null && item.getGoods().getMerchantId() != null) {
                            if (item.getGoods().getMerchantId().equals(currentUser.getId())) {
                                hasMerchantGoods = true;
                                break;
                            }
                        }
                    }
                }
                if (!hasMerchantGoods) {
                    throw new RuntimeException("You don't have permission to view this order");
                }
            } else {
                // 其他角色默认不允许访问
                throw new RuntimeException("You don't have permission to view this order");
            }
        }
        
        // 构造返回的订单Map对象
        Map<String, Object> orderMap = new HashMap<>();
        orderMap.put("id", order.getId());
        orderMap.put("orderNo", order.getOrderNo());
        orderMap.put("userId", order.getUserId());
        // 将BigDecimal转换为Double返回给前端，并进行null检查
        orderMap.put("totalAmount", order.getTotalAmount() != null ? order.getTotalAmount().doubleValue() : 0.0);
        orderMap.put("payAmount", order.getPayAmount() != null ? order.getPayAmount().doubleValue() : 0.0);
        orderMap.put("status", order.getStatus());
        orderMap.put("address", order.getAddress());
        orderMap.put("remark", order.getRemark());
        orderMap.put("createTime", order.getCreateTime());
        orderMap.put("updateTime", order.getUpdateTime());
        orderMap.put("isDeleted", order.getIsDeleted());
        
        // 获取用户名信息
        try {
            UserInfo userInfo = userService.getUserInfoById(order.getUserId());
            orderMap.put("userName", userInfo.getName() != null ? userInfo.getName() : userInfo.getUsername());
        } catch (Exception e) {
            orderMap.put("userName", "未知用户");
        }
        
        // 添加订单项信息
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            List<Map<String, Object>> orderItemsList = new ArrayList<>();
            for (OrderItem item : order.getOrderItems()) {
                Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("goodsId", item.getGoodsId());
            itemMap.put("goodsName", item.getGoodsName());
            itemMap.put("image", item.getImage());
            
            // 始终使用订单项保存的价格，确保订单价格的一致性
            BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
            Integer quantity = item.getQuantity() != null ? item.getQuantity() : 0;
                
                // 计算精确的小计金额
                BigDecimal subtotal = price.multiply(new BigDecimal(quantity));
                
                itemMap.put("price", price.doubleValue());
                itemMap.put("quantity", quantity);
                itemMap.put("subtotal", subtotal.doubleValue());
                
                // 添加商品信息，包括商家ID，用于前端权限检查
                if (item.getGoods() != null) {
                    Map<String, Object> goodsMap = new HashMap<>();
                    goodsMap.put("merchantId", item.getGoods().getMerchantId());
                    goodsMap.put("id", item.getGoods().getId());
                    goodsMap.put("name", item.getGoodsName()); // 使用订单项保存的商品名称
                    itemMap.put("goods", goodsMap);
                }
                orderItemsList.add(itemMap);
            }
            orderMap.put("orderItems", orderItemsList);
        } else {
            orderMap.put("orderItems", new ArrayList<>());
        }
        
        return orderMap;
    }

    @Override
    public Order getOrderByOrderNo(String orderNo) {
        return orderRepository.findByOrderNo(orderNo)
                .filter(order -> order.getIsDeleted() == null || !order.getIsDeleted())
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Override
    @Transactional
    public Order createOrder(Order order) {
        // 生成订单号
        String orderNo = generateOrderNo();
        order.setOrderNo(orderNo);

        // 设置初始状态为待付款
        order.setStatus(1);

        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        order.setCreateTime(now);
        order.setUpdateTime(now);

        // 重新计算订单总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            for (OrderItem item : order.getOrderItems()) {
                item.setOrder(order);  // 设置order引用为当前Order对象
                item.setCreateTime(now);
                item.setUpdateTime(now);
                
                // 从商品信息中获取实际价格和图片，强制使用后端数据，不依赖前端传入
                Goods goods = goodsService.getGoodsById(item.getGoodsId());
                item.setGoods(goods);  // 设置商品对象，建立关联关系
                item.setPrice(goods.getPrice());
                item.setImage(goods.getImage());
                
                // 确保quantity字段不为null
                if (item.getQuantity() == null) {
                    item.setQuantity(0);
                }
                
                // 计算单个订单项的金额并累加到总金额
                BigDecimal itemAmount = item.getPrice().multiply(new BigDecimal(item.getQuantity()));
                totalAmount = totalAmount.add(itemAmount);
                
                // 扣减商品库存
                goodsService.decreaseStock(item.getGoodsId(), item.getQuantity());
            }
        }
        
        // 设置重新计算的总金额
        order.setTotalAmount(totalAmount);
        
        // 确保支付金额与总金额一致
        order.setPayAmount(totalAmount);

        // 保存订单（级联保存OrderItems）
        Order savedOrder = orderRepository.save(order);

        return savedOrder;
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Long id, Integer status, Long merchantId) {
        // 1. 查找订单，使用自定义查询加载订单项和商品信息
        Order order = orderRepository.findByIdWithItemsAndGoods(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // 2. 检查状态是否为null
        if (status == null) {
            throw new RuntimeException("Order status cannot be null");
        }
        
        // 3. 检查状态是否有效
        List<Integer> validStatuses = Arrays.asList(1, 2, 3, 4, 5);
        if (!validStatuses.contains(status)) {
            throw new RuntimeException("Invalid order status");
        }

        // 3. 如果订单状态为已取消(5)或已完成(4)，则不允许修改
        if (order.getStatus() == 4 || order.getStatus() == 5) {
            throw new RuntimeException("Cannot update status of completed or cancelled orders");
        }

        // 4. 根据用户需求修改：让商家拥有与管理员相同的订单管理权限
        // 不再检查商家是否有该订单的商品，允许商家更新所有订单的状态

        // 5. 更新订单状态
        order.setStatus(status);

        // 6. 更新时间戳
        order.setUpdateTime(LocalDateTime.now());

        // 7. 保存订单
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order confirmReceipt(Long id, UserInfo currentUser) {
        Optional<Order> orderOptional = orderRepository.findByIdWithItemsAndGoods(id);
        
        Order order = orderOptional
                .filter(o -> o.getIsDeleted() == null || !o.getIsDeleted())
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // 添加日志记录
        logger.info("User {} is confirming receipt for order {}", currentUser.getId(), id);
        
        // 权限检查
        if (currentUser == null) {
            throw new RuntimeException("Please login to confirm receipt");
        }
        
        // 管理员可以处理所有订单
        String userRole = currentUser.getRole() != null ? currentUser.getRole().toLowerCase() : "";
        if (!"admin".equals(userRole)) {
            // 普通用户只能处理自己的订单
            if ("user".equals(userRole)) {
                if (!order.getUserId().equals(currentUser.getId())) {
                    throw new RuntimeException("You don't have permission to confirm receipt for this order");
                }
            } else if ("merchant".equals(userRole)) {
                // 商家只能处理包含自己商品的订单
                boolean hasMerchantGoods = false;
                if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
                    for (OrderItem item : order.getOrderItems()) {
                        if (item.getGoods() != null && item.getGoods().getMerchantId() != null) {
                            if (item.getGoods().getMerchantId().equals(currentUser.getId())) {
                                hasMerchantGoods = true;
                                break;
                            }
                        }
                    }
                }
                if (!hasMerchantGoods) {
                    throw new RuntimeException("You don't have permission to confirm receipt for this order");
                }
            } else {
                // 其他角色默认不允许访问
                throw new RuntimeException("You don't have permission to perform this action");
            }
        }
        
        // 只有待收货状态的订单才能确认收货
        if (order.getStatus() != 3) {
            throw new RuntimeException("Only orders in 'to be received' status can be confirmed");
        }
        
        // 更新订单状态为已完成
        order.setStatus(4); // 已完成
        order.setUpdateTime(LocalDateTime.now());
        
        // 保存订单
        Order savedOrder = orderRepository.save(order);
        
        // 添加日志记录
        logger.info("Order {} status updated to COMPLETED", savedOrder.getId());
        
        return savedOrder;
    }

    @Override
    @Transactional
    public Order cancelOrder(Long id, UserInfo currentUser) {
        // 直接查询Order对象，确保加载订单项和商品信息
        Optional<Order> orderOptional = orderRepository.findByIdWithItemsAndGoods(id);
        
        Order order = orderOptional
                .filter(o -> o.getIsDeleted() == null || !o.getIsDeleted())
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // 权限检查
        if (currentUser == null) {
            throw new RuntimeException("Please login to cancel order");
        }
        
        String userRole = currentUser.getRole() != null ? currentUser.getRole().toLowerCase() : "";
        if (!"admin".equals(userRole)) {
            if ("user".equals(userRole)) {
                if (!order.getUserId().equals(currentUser.getId())) {
                    throw new RuntimeException("You don't have permission to cancel this order");
                }
            } else if ("merchant".equals(userRole)) {
                boolean hasMerchantGoods = false;
                if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
                    long count = orderRepository.countOrderItemsByOrderIdAndMerchantId(id, currentUser.getId());
                    if (count == 0) {
                        throw new RuntimeException("You don't have permission to cancel this order");
                    }
                }
            } else {
                throw new RuntimeException("You don't have permission to cancel this order");
            }
        }
        
        // 已完成的订单不能取消
        if (order.getStatus() == 4) {
            throw new RuntimeException("Completed orders cannot be canceled");
        }
        // 已经是取消状态的订单不能再次取消
        if (order.getStatus() == 5) {
            throw new RuntimeException("Order is already canceled");
        }
        
        // 只有待付款状态的订单取消时需要恢复库存
        if (order.getStatus() == 1) {
            // 恢复商品库存
            if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
                for (OrderItem item : order.getOrderItems()) {
                    goodsService.increaseStock(item.getGoodsId(), item.getQuantity());
                }
            }
        }
        
        order.setStatus(5); // 已取消
        order.setUpdateTime(LocalDateTime.now());
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id, UserInfo currentUser) {
        // 查找订单，确保加载订单项和商品信息
        Optional<Order> orderOptional = orderRepository.findByIdWithItemsAndGoods(id);
        Order order = orderOptional
                .filter(o -> o.getIsDeleted() == null || !o.getIsDeleted())
                .orElseThrow(() -> new RuntimeException("Order not found"));
                
        // 检查订单状态，只有已取消(5)和已完成(4)状态的订单才能删除
        if (order.getStatus() != 4 && order.getStatus() != 5) {
            throw new RuntimeException("Only completed or cancelled orders can be deleted");
        }
                
        // 权限检查
        checkOrderPermission(order, currentUser);
        
        String userRole = currentUser.getRole() != null ? currentUser.getRole().toLowerCase() : "";
        
        // 根据用户角色设置不同的删除字段
        if ("user".equals(userRole)) {
            order.setUserDeleted(true);
        } else if ("merchant".equals(userRole)) {
            order.setMerchantDeleted(true);
        } else {
            // 管理员仍然可以硬删除（设置isDeleted）
            order.setIsDeleted(true);
        }
        
        order.setUpdateTime(LocalDateTime.now());
        
        orderRepository.save(order);
    }

    @Override
    public List<Map<String, Object>> getOrderStatusList() {
        return ORDER_STATUS_LIST;
    }

    @Override
    public Map<String, Object> getUserOrdersList(Long userId, Map<String, Object> params) {
        int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        int pageSize = params.get("pageSize") != null ? Integer.parseInt(params.get("pageSize").toString()) : 10;
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;

        List<Order> orders = new ArrayList<>();
        if (status != null) {
            List<Order> tempOrders = orderRepository.findByUserIdAndStatus(userId, status);
            if (tempOrders != null) {
                orders = tempOrders;
            }
        } else {
            List<Order> tempOrders = orderRepository.findByUserId(userId);
            if (tempOrders != null) {
                orders = tempOrders;
            }
        }

        // 过滤已删除的订单
        orders = orders.stream()
                .filter(order -> !order.getIsDeleted())
                .collect(java.util.stream.Collectors.toList());

        // 手动分页
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, orders.size());
        List<Order> pagedOrders = (start < end) ? orders.subList(start, end) : new ArrayList<>();

        Map<String, Object> result = new HashMap<>();
        result.put("list", pagedOrders);
        result.put("total", orders.size());
        result.put("page", page);
        result.put("pageSize", pageSize);

        return result;
    }

    @Override
    public Map<String, Object> getMerchantOrdersList(Long merchantId, Map<String, Object> params) {
        int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        int pageSize = params.get("pageSize") != null ? Integer.parseInt(params.get("pageSize").toString()) : 10;
        String orderNo = params.get("orderNo") != null ? params.get("orderNo").toString() : null;
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;

        List<Order> orders = new ArrayList<>();
        if (orderNo != null && !orderNo.isEmpty()) {
            List<Order> tempOrders = orderRepository.findAllActiveByMerchantIdAndOrderNo(merchantId, orderNo);
            if (tempOrders != null) {
                orders = tempOrders;
            }
        } else if (status != null) {
            List<Order> tempOrders = orderRepository.findAllActiveByMerchantIdAndStatus(merchantId, status);
            if (tempOrders != null) {
                orders = tempOrders;
            }
        } else {
            List<Order> tempOrders = orderRepository.findAllActiveByMerchantId(merchantId);
            if (tempOrders != null) {
                orders = tempOrders;
            }
        }

        // 手动分页
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, orders.size());
        List<Order> pagedOrders = (start < end) ? orders.subList(start, end) : new ArrayList<>();

        Map<String, Object> result = new HashMap<>();
        result.put("list", pagedOrders);
        result.put("total", orders.size());
        result.put("page", page);
        result.put("pageSize", pageSize);

        return result;
    }

    // 生成唯一订单号
    private String generateOrderNo() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        String timePart = now.format(formatter);
        String randomPart = String.format("%04d", new Random().nextInt(10000));
        return "ORD" + timePart + randomPart;
    }

    // 辅助类，用于手动创建Page对象
    private static class CustomPageImpl<T> extends org.springframework.data.domain.PageImpl<T> {
        public CustomPageImpl(List<T> content, Pageable pageable, long total) {
            super(content, pageable, total);
        }
    }
}