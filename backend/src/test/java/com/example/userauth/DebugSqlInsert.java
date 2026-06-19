package com.example.userauth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
public class DebugSqlInsert {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Test
    public void testInsertOrder() {
        // 创建订单
        String insertOrderSql = "INSERT INTO orders (order_no, user_id, total_amount, pay_amount, status, address, remark, create_time, update_time, is_deleted) " +
                               "VALUES ('TESTORD20251207001', 3, 299.99, 299.99, 3, '测试地址', '测试订单', NOW(), NOW(), false);";
        
        jdbcTemplate.update(insertOrderSql);
        
        // 获取刚插入的订单ID
        Long orderId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID();", Long.class);
        
        System.out.println("插入订单ID: " + orderId);
        
        // 添加订单项
        String insertItemSql = "INSERT INTO order_items (order_id, goods_id, goods_name, price, quantity, create_time, update_time, is_deleted) " +
                              "VALUES (?, 5, '进口牛奶', 299.99, 1, NOW(), NOW(), false);";
        
        jdbcTemplate.update(insertItemSql, orderId);
        
        System.out.println("插入订单项成功");
        
        // 查询订单
        String orderSql = "SELECT * FROM orders WHERE id = ?;";
        jdbcTemplate.query(orderSql, new Object[]{orderId}, (rs, rowNum) -> {
            System.out.println("订单详情:");
            System.out.println("ID: " + rs.getLong("id"));
            System.out.println("订单号: " + rs.getString("order_no"));
            System.out.println("用户ID: " + rs.getLong("user_id"));
            System.out.println("总金额: " + rs.getBigDecimal("total_amount"));
            System.out.println("状态: " + rs.getInt("status"));
            return null;
        });
        
        // 查询订单项
        String itemSql = "SELECT * FROM order_items WHERE order_id = ?;";
        jdbcTemplate.query(itemSql, new Object[]{orderId}, (rs, rowNum) -> {
            System.out.println("\n订单项详情:");
            System.out.println("ID: " + rs.getLong("id"));
            System.out.println("订单ID: " + rs.getLong("order_id"));
            System.out.println("商品ID: " + rs.getLong("goods_id"));
            System.out.println("商品名称: " + rs.getString("goods_name"));
            System.out.println("价格: " + rs.getBigDecimal("price"));
            System.out.println("数量: " + rs.getInt("quantity"));
            return null;
        });
    }
}