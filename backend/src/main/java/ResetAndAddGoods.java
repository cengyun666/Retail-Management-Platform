import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ResetAndAddGoods {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/supermarket";
        String username = "root";
        String password = "123456";
        
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
            return;
        }
        
        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement();
             BufferedReader reader = new BufferedReader(new FileReader("reset_and_add_goods.sql"))) {
            
            // Disable auto-commit to execute multiple SQL statements
            conn.setAutoCommit(false);
            
            StringBuilder sqlBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip comments and empty lines
                if (line.trim().startsWith("--") || line.trim().isEmpty()) {
                    continue;
                }
                sqlBuilder.append(line).append("\n");
                
                // If we encounter a semicolon, it means an SQL statement ends
                if (line.trim().endsWith(";")) {
                    String sql = sqlBuilder.toString();
                    System.out.println("Executing SQL: " + sql.substring(0, Math.min(50, sql.length())) + "...");
                    
                    try {
                        stmt.execute(sql);
                    } catch (SQLException e) {
                        System.err.println("SQL execution error: " + e.getMessage());
                        System.err.println("SQL statement: " + sql);
                    }
                    
                    sqlBuilder.setLength(0); // Clear StringBuilder
                }
            }
            
            // Commit transaction
            conn.commit();
            System.out.println("All SQL statements executed successfully!");
            
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("File reading error: " + e.getMessage());
        }
    }
}