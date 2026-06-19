package com.example.dbtest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Pure database connection test class
 */
//@SpringBootApplication
public class DatabaseConnectionTest {

    public static void main(String[] args) {
        SpringApplication.run(DatabaseConnectionTest.class, args);
    }

    @Bean
    public CommandLineRunner testConnection(@Autowired DataSource dataSource) {
        return args -> {
            System.out.println("Starting database connection test...");

            try (Connection connection = dataSource.getConnection()) {
                System.out.println("Database connection successful!");
                
                DatabaseMetaData metaData = connection.getMetaData();
                System.out.println("Database product name: " + metaData.getDatabaseProductName());
                System.out.println("Database product version: " + metaData.getDatabaseProductVersion());
                System.out.println("Database username: " + metaData.getUserName());
                
                System.out.println("Database connection test completed!");
                System.exit(0); // Normal exit
            } catch (SQLException e) {
                System.err.println("Database connection failed: " + e.getMessage());
                e.printStackTrace();
                System.exit(1); // Exception exit
            }
        };
    }
}