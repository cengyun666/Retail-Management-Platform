package com.example.userauth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class DatabaseResetController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/table-structure")
    public String getTableStructure() {
        try {
            List<String> columns = jdbcTemplate.query(
                "DESCRIBE goods",
                (rs, rowNum) -> rs.getString(1) + " | " + rs.getString(2) + " | " + rs.getString(3) + " | " + rs.getString(4) + " | " + rs.getString(5) + " | " + rs.getString(6)
            );
            StringBuilder result = new StringBuilder("Table structure for goods:\n");
            result.append("Column | Type | Null | Key | Default | Extra\n");
            result.append("----------------------------------------\n");
            for (String column : columns) {
                result.append(column).append("\n");
            }
            return result.toString();
        } catch (Exception e) {
            return "Error getting table structure: " + e.getMessage();
        }
    }

    @GetMapping("/categories")
    public String getCategories() {
        try {
            List<String> categories = jdbcTemplate.query(
                "SELECT id, name FROM categories",
                (rs, rowNum) -> "ID: " + rs.getInt("id") + ", Name: " + rs.getString("name")
            );
            StringBuilder result = new StringBuilder("Categories:\n");
            for (String category : categories) {
                result.append(category).append("\n");
            }
            return result.toString();
        } catch (Exception e) {
            return "Error getting categories: " + e.getMessage();
        }
    }

    @GetMapping("/goods-count")
    public String getGoodsCount() {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM goods", Integer.class);
            return "Total goods count: " + count;
        } catch (Exception e) {
            return "Error getting goods count: " + e.getMessage();
        }
    }

    @GetMapping("/goods-list")
    public String getGoodsList() {
        try {
            List<String> goods = jdbcTemplate.query(
                "SELECT id, name, category_id, price, stock, status FROM goods",
                (rs, rowNum) -> "ID: " + rs.getInt("id") + 
                               ", Name: " + rs.getString("name") + 
                               ", Category: " + rs.getInt("category_id") + 
                               ", Price: " + rs.getBigDecimal("price") + 
                               ", Stock: " + rs.getInt("stock") + 
                               ", Status: " + rs.getInt("status")
            );
            StringBuilder result = new StringBuilder("Goods List:\n");
            for (String item : goods) {
                result.append(item).append("\n");
            }
            return result.toString();
        } catch (Exception e) {
            return "Error getting goods list: " + e.getMessage();
        }
    }

    @PostMapping("/reset-goods")
    public String resetGoods() {
        try {
            // Start transaction
            jdbcTemplate.execute("START TRANSACTION");
            
            // Read and execute SQL script
            String scriptPath = "reset_and_add_goods.sql";
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(scriptPath), StandardCharsets.UTF_8))) {
                String line;
                StringBuilder currentStatement = new StringBuilder();
                List<String> statements = new ArrayList<>();
                
                while ((line = reader.readLine()) != null) {
                    // Skip comments and empty lines
                    if (!line.trim().isEmpty() && !line.trim().startsWith("--")) {
                        currentStatement.append(line).append(" ");
                        
                        // If line ends with semicolon, it's a complete statement
                        if (line.trim().endsWith(";")) {
                            statements.add(currentStatement.toString().trim());
                            currentStatement = new StringBuilder();
                        }
                    }
                }
                
                // Execute each statement
                for (String statement : statements) {
                    if (!statement.trim().isEmpty()) {
                        try {
                            jdbcTemplate.execute(statement);
                        } catch (Exception e) {
                            jdbcTemplate.execute("ROLLBACK");
                            return "Error executing SQL statement: " + statement + "\nError: " + e.getMessage();
                        }
                    }
                }
            } catch (IOException e) {
                jdbcTemplate.execute("ROLLBACK");
                return "Error reading SQL script: " + e.getMessage();
            }
            
            // Commit transaction
            jdbcTemplate.execute("COMMIT");
            return "Goods reset and added successfully!";
            
        } catch (Exception e) {
            try {
                jdbcTemplate.execute("ROLLBACK");
            } catch (Exception rollbackEx) {
                // Ignore rollback errors
            }
            return "Error resetting goods: " + e.getMessage();
        }
    }
}