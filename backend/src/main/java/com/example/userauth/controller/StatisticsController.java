package com.example.userauth.controller;

import com.example.userauth.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计数据控制器
 */
@RestController
@RequestMapping("/statistics")
public class StatisticsController {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> getDashboardStatistics() {
        // 创建响应数据
        Map<String, Object> responseData = new HashMap<>();
        
        // 获取统计数据
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("userCount", getUserCount());
        statistics.put("goodsCount", getGoodsCount());
        statistics.put("orderCount", getOrderCount());
        statistics.put("totalSales", getTotalSales());
        
        responseData.put("statistics", statistics);
        responseData.put("monthlyOrderTrend", getMonthlyOrderTrend());
        responseData.put("categoryOrderData", getCategoryOrderData());
        
        return ApiResponse.success("获取仪表盘统计数据成功", responseData);
    }
    
    /**
     * 获取用户总数
     */
    private long getUserCount() {
        String sql = "SELECT COUNT(*) FROM users WHERE is_deleted = 0";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }
    
    /**
     * 获取商品总数
     */
    private long getGoodsCount() {
        String sql = "SELECT COUNT(*) FROM goods WHERE is_deleted = 0";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }
    
    /**
     * 获取订单总数
     */
    private long getOrderCount() {
        String sql = "SELECT COUNT(*) FROM orders WHERE is_deleted = 0";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }
    
    /**
     * 获取总销售额
     */
    private double getTotalSales() {
        String sql = "SELECT IFNULL(SUM(total_amount), 0) FROM orders WHERE is_deleted = 0 AND status = 4";
        return jdbcTemplate.queryForObject(sql, Double.class);
    }
    
    /**
     * 获取月度订单趋势数据
     */
    private List<Map<String, Object>> getMonthlyOrderTrend() {
        // 为了分别统计所有订单数量和已完成订单的销售额，需要使用子查询
        String sql = "SELECT " +
                "DATE_FORMAT(create_time, '%c月') as month, " +
                "COUNT(CASE WHEN status = 4 THEN 1 ELSE NULL END) as orderCount, " +
                "SUM(CASE WHEN status = 4 THEN total_amount ELSE 0 END) as sales " +
                "FROM orders " +
                "WHERE is_deleted = 0 " +
                "GROUP BY DATE_FORMAT(create_time, '%Y-%m'), DATE_FORMAT(create_time, '%c月') " +
                "ORDER BY DATE_FORMAT(create_time, '%Y-%m') ASC";
        
        return jdbcTemplate.query(sql, new RowMapper<Map<String, Object>>() {
            @Override
            public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
                Map<String, Object> map = new HashMap<>();
                map.put("month", rs.getString("month"));
                map.put("orderCount", rs.getLong("orderCount"));
                map.put("sales", rs.getDouble("sales"));
                return map;
            }
        });
    }

    /**
     * 获取商品类别销售数据
     */
    private List<Map<String, Object>> getCategoryOrderData() {
        String sql = "SELECT " +
                "c.name as category, " +
                "SUM(oi.price * oi.quantity) as sales " +
                "FROM order_items oi " +
                "INNER JOIN goods g ON oi.goods_id = g.id " +
                "INNER JOIN categories c ON g.category_id = c.id " +
                "INNER JOIN orders o ON oi.order_id = o.id " +
                "WHERE oi.is_deleted = 0 AND g.is_deleted = 0 AND c.is_deleted = 0 AND o.status = 4 " +
                "GROUP BY c.id, c.name " +
                "ORDER BY sales DESC";
        
        return jdbcTemplate.query(sql, new RowMapper<Map<String, Object>>() {
            @Override
            public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
                Map<String, Object> map = new HashMap<>();
                map.put("category", rs.getString("category"));
                map.put("sales", rs.getDouble("sales"));
                return map;
            }
        });
    }
}