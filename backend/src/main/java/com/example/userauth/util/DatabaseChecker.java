package com.example.userauth.util;

import com.example.userauth.entity.Order;
import com.example.userauth.entity.User;
import com.example.userauth.repository.OrderRepository;
import com.example.userauth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

// @Component
public class DatabaseChecker implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private FixMerchantId fixMerchantId;

    @Override
    public void run(String... args) throws Exception {
        // 只在特定参数下运行，避免每次启动都执行
        boolean shouldRun = false;
        for (String arg : args) {
            if ("checkdb".equals(arg)) {
                shouldRun = true;
                break;
            }
        }
        
        if (!shouldRun) {
            System.out.println("DatabaseChecker skipped. Use 'checkdb' argument to enable.");
            return;
        }
        
        System.out.println("=== Database Checker ===");
        
        // 先修复商品的merchant_id
        fixMerchantId.run();

        // 查找商家用户
        List<User> merchants = userRepository.findWithFilters(null, "merchant", null);
        System.out.println("\nMerchants found: " + merchants.size());
        for (User merchant : merchants) {
            System.out.println("Merchant: " + merchant.getUsername() + " (ID: " + merchant.getId() + ")");
        }

        // 使用JDBC直接查询商家商品数量
        System.out.println("\nGoods by merchant (using JDBC):");
        for (User merchant : merchants) {
            String sql = "SELECT COUNT(*) FROM goods WHERE merchant_id = ? AND is_deleted = 0";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, merchant.getId());
            System.out.println("Merchant " + merchant.getUsername() + " has " + count + " goods");
        }

        // 查找所有订单
        List<Order> allOrders = orderRepository.findAll();
        System.out.println("\nAll orders found: " + allOrders.size());
        for (Order order : allOrders) {
            System.out.println("Order: " + order.getOrderNo() + " (ID: " + order.getId() + "), Status: " + order.getStatus());
        }

        // 使用JDBC直接查询订单和订单项关联
        System.out.println("\nOrder items and merchant associations (using JDBC):");
        String sql = "SELECT oi.order_id, oi.goods_id, g.merchant_id FROM order_items oi JOIN goods g ON oi.goods_id = g.id";
        jdbcTemplate.query(sql, (rs, rowNum) -> {
            Long orderId = rs.getLong(1);
            Long goodsId = rs.getLong(2);
            Long merchantId = rs.getLong(3);
            System.out.println("Order ID: " + orderId + ", Goods ID: " + goodsId + ", Merchant ID: " + merchantId);
            return null;
        });

        // 检查商家订单关联
        System.out.println("\nChecking merchant order associations:");
        for (User merchant : merchants) {
            List<Order> merchantOrders = orderRepository.findAllActiveByMerchantId(merchant.getId());
            System.out.println("Merchant " + merchant.getUsername() + " has " + merchantOrders.size() + " orders via repository method");
        }

        System.out.println("\n=== Database Checker Completed ===");
    }
}