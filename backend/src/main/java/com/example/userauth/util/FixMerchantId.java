package com.example.userauth.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class FixMerchantId {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void run() {
        System.out.println("=== Fixing Merchant IDs ===");
        
        // 将所有商品的merchant_id修改为2（商家用户ID）
        String updateSql = "UPDATE goods SET merchant_id = ?";
        int updated = jdbcTemplate.update(updateSql, 2);
        
        System.out.println("Updated " + updated + " goods to have merchant_id = 2");
        
        // 验证修改结果
        String countSql = "SELECT COUNT(*) FROM goods WHERE merchant_id = ?";
        Integer count = jdbcTemplate.queryForObject(countSql, Integer.class, 2);
        
        System.out.println("Now there are " + count + " goods with merchant_id = 2");
        
        // 再次检查订单ID=10的商品
        String orderSql = "SELECT g.id, g.name, g.merchant_id FROM goods g JOIN order_items oi ON g.id = oi.goods_id JOIN orders o ON oi.order_id = o.id WHERE o.id = ?";
        List<Map<String, Object>> orderGoods = jdbcTemplate.queryForList(orderSql, 10);
        
        System.out.println("Order 10 goods after fix:");
        for (Map<String, Object> row : orderGoods) {
            System.out.println("Goods ID: " + row.get("id") + ", Name: " + row.get("name") + ", Merchant ID: " + row.get("merchant_id"));
        }
        
        System.out.println("=== Fixing Merchant IDs Completed ===");
    }
}
