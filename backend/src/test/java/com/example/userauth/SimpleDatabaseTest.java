package com.example.userauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 简单数据库测试类
 */
//@SpringBootApplication
@ComponentScan("com.example.userauth")
public class SimpleDatabaseTest {

    public static void main(String[] args) {
        SpringApplication.run(SimpleDatabaseTest.class, args);
    }

    @Bean
    public CommandLineRunner testSimpleDatabase(@Autowired DataSource dataSource) {
        return args -> {
            System.out.println("开始简单数据库测试...");

            try (Connection connection = dataSource.getConnection()) {
                System.out.println("数据库连接成功！");
                
                // 检查表是否存在
                try (Statement statement = connection.createStatement()) {
                    // 检查category表
                    try {
                        ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM category");
                        if (rs.next()) {
                            int count = rs.getInt(1);
                            System.out.println("category表存在，记录数: " + count);
                        }
                    } catch (SQLException e) {
                        System.out.println("category表不存在或查询失败: " + e.getMessage());
                    }
                    
                    // 检查goods表
                    try {
                        ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM goods");
                        if (rs.next()) {
                            int count = rs.getInt(1);
                            System.out.println("goods表存在，记录数: " + count);
                        }
                    } catch (SQLException e) {
                        System.out.println("goods表不存在或查询失败: " + e.getMessage());
                    }
                }
                
            } catch (SQLException e) {
                System.err.println("数据库连接失败: " + e.getMessage());
                e.printStackTrace();
            }

            System.out.println("简单数据库测试完成！");
        };
    }
}