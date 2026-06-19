package com.example.userauth;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class ResetUsersData {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/user_auth_db?useSSL=false&serverTimezone=UTC";
        String username = "root";
        String password = "root";
        
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            System.out.println("连接到数据库成功！");
            
            // 禁用外键检查
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SET FOREIGN_KEY_CHECKS = 0;");
                System.out.println("已禁用外键检查");
            }
            
            // 删除所有用户数据
            try (Statement stmt = conn.createStatement()) {
                int rowsDeleted = stmt.executeUpdate("DELETE FROM users");
                System.out.println("已删除 " + rowsDeleted + " 条用户记录");
            }
            
            // 重置自增ID
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("ALTER TABLE users AUTO_INCREMENT = 1");
                System.out.println("已重置用户表自增ID");
            }
            
            // 插入新用户数据，管理员排在第一位
            String[] usernames = {"admin", "merchant001", "user001", "user002", "user003", "user004", "user005"};
            String[] passwords = {"$2a$10$KvALCWs9o.tnj3Q3AMlYu.vwhTJj1lZaaH3P2h/zL9DOB45dLu5PC", "$2a$10$KvALCWs9o.tnj3Q3AMlYu.vwhTJj1lZaaH3P2h/zL9DOB45dLu5PC", "$2a$10$KvALCWs9o.tnj3Q3AMlYu.vwhTJj1lZaaH3P2h/zL9DOB45dLu5PC", "$2a$10$KvALCWs9o.tnj3Q3AMlYu.vwhTJj1lZaaH3P2h/zL9DOB45dLu5PC", "$2a$10$KvALCWs9o.tnj3Q3AMlYu.vwhTJj1lZaaH3P2h/zL9DOB45dLu5PC", "$2a$10$KvALCWs9o.tnj3Q3AMlYu.vwhTJj1lZaaH3P2h/zL9DOB45dLu5PC", "$2a$10$KvALCWs9o.tnj3Q3AMlYu.vwhTJj1lZaaH3P2h/zL9DOB45dLu5PC"};
            String[] names = {"系统管理员", "张老板", "张三", "李四", "王五", "赵六", "钱七"};
            String[] emails = {"admin@example.com", "merchant001@example.com", "user001@example.com", "user002@example.com", "user003@example.com", "user004@example.com", "user005@example.com"};
            String[] phones = {"13800138000", "13800138001", "13800138002", "13800138003", "13800138004", "13800138005", "13800138006"};
            String[] addresses = {"北京市朝阳区建国路88号", "上海市浦东新区陆家嘴金融中心", "北京市海淀区中关村大街1号", "上海市徐汇区淮海中路100号", "广州市越秀区中山二路48号", "深圳市福田区深南大道1000号", "成都市高新区天府大道"};
            String[] roles = {"ADMIN", "MERCHANT", "USER", "USER", "USER", "USER", "USER"};
            Integer[] statuses = {1, 1, 1, 1, 1, 1, 1};
            Boolean[] isDeleted = {false, false, false, false, false, false, false};
            
            String insertSql = "INSERT INTO users (username, password, name, email, phone, address, role, status, is_deleted, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
            
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                for (int i = 0; i < usernames.length; i++) {
                    pstmt.setString(1, usernames[i]);
                    pstmt.setString(2, passwords[i]);
                    pstmt.setString(3, names[i]);
                    pstmt.setString(4, emails[i]);
                    pstmt.setString(5, phones[i]);
                    pstmt.setString(6, addresses[i]);
                    pstmt.setString(7, roles[i]);
                    pstmt.setInt(8, statuses[i]);
                    pstmt.setBoolean(9, isDeleted[i]);
                    pstmt.executeUpdate();
                    System.out.println("已插入用户: " + usernames[i] + " (角色: " + roles[i] + ")");
                }
            }
            
            // 重新启用外键检查
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SET FOREIGN_KEY_CHECKS = 1;");
                System.out.println("已启用外键检查");
            }
            
            System.out.println("用户数据重置完成！");
            
            // 查看插入结果
            try (Statement stmt = conn.createStatement()) {
                var rs = stmt.executeQuery("SELECT id, username, role FROM users ORDER BY id");
                System.out.println("插入的用户列表:");
                while (rs.next()) {
                    System.out.println("ID: " + rs.getInt("id") + ", 用户名: " + rs.getString("username") + ", 角色: " + rs.getString("role"));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("数据库操作失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}