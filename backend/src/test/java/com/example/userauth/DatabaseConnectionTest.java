package com.example.userauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据库连接测试类
 */
//@SpringBootApplication
@ComponentScan("com.example.userauth")
public class DatabaseConnectionTest {

    public static void main(String[] args) {
        SpringApplication.run(DatabaseConnectionTest.class, args);
    }

    @Bean
    public CommandLineRunner testDatabaseConnection(@Autowired DataSource dataSource) {
        return args -> {
            System.out.println("开始测试数据库连接...");

            try (Connection connection = dataSource.getConnection()) {
                System.out.println("数据库连接成功！");
                System.out.println("数据库URL: " + connection.getMetaData().getURL());
                System.out.println("数据库用户名: " + connection.getMetaData().getUserName());
                System.out.println("数据库产品名称: " + connection.getMetaData().getDatabaseProductName());
                System.out.println("数据库产品版本: " + connection.getMetaData().getDatabaseProductVersion());
            } catch (SQLException e) {
                System.err.println("数据库连接失败: " + e.getMessage());
                e.printStackTrace();
            }

            System.out.println("数据库连接测试完成！");
        };
    }
}