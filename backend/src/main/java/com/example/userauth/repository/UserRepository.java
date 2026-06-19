package com.example.userauth.repository;

import com.example.userauth.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户数据访问层
 */
@Repository
public class UserRepository {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * 根据ID查找用户
     */
    public User findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ? AND is_deleted = 0";
        try {
            return jdbcTemplate.queryForObject(sql, new UserRowMapper(), id);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 根据用户名查找用户
     */
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ? AND is_deleted = 0";
        try {
            return jdbcTemplate.queryForObject(sql, new UserRowMapper(), username);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 根据用户名查找用户（包含已删除）
     */
    public User findByUsernameAndIsDeletedFalse(String username) {
        String sql = "SELECT * FROM users WHERE username = ? AND is_deleted = 0";
        try {
            return jdbcTemplate.queryForObject(sql, new UserRowMapper(), username);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 检查用户名是否存在
     */
    public boolean existsByUsernameAndIsDeletedFalse(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ? AND is_deleted = 0";
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查邮箱是否存在
     */
    public boolean existsByEmailAndIsDeletedFalse(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ? AND is_deleted = 0";
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 查找所有用户
     */
    public List<User> findAll() {
        String sql = "SELECT * FROM users WHERE is_deleted = 0";
        return jdbcTemplate.query(sql, new UserRowMapper());
    }
    
    /**
     * 查找所有未删除的用户
     */
    public List<User> findByIsDeletedFalse() {
        String sql = "SELECT * FROM users WHERE is_deleted = 0 ORDER BY id ASC";
        return jdbcTemplate.query(sql, new UserRowMapper());
    }
    
    /**
     * 根据条件查找用户
     */
    public List<User> findWithFilters(String search, String role, Integer status) {
        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE is_deleted = 0");
        List<Object> params = new ArrayList<>();
        
        // 添加搜索条件（用户名、姓名、邮箱）
        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (username LIKE ? OR name LIKE ? OR email LIKE ?)");
            String searchPattern = "%" + search.trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
        }
        
        // 添加角色筛选
        if (role != null && !role.trim().isEmpty()) {
            sql.append(" AND role = ?");
            params.add(role.trim());
        }
        
        // 添加状态筛选
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        
        // 按创建时间升序排列，使新用户显示在底部
        sql.append(" ORDER BY create_time ASC");
        
        return jdbcTemplate.query(sql.toString(), new UserRowMapper(), params.toArray());
    }
    
    /**
     * 保存用户（新增或更新）
     */
    public User save(User user) {
        if (user.getId() == null) {
            // 新增用户
            String sql = "INSERT INTO users (username, password, name, email, phone, address, role, status, create_time, update_time, is_deleted) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            // 使用GeneratedKeyHolder获取自动生成的ID
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getPassword());
                ps.setString(3, user.getName());
                ps.setString(4, user.getEmail());
                ps.setString(5, user.getPhone());
                ps.setString(6, user.getAddress());
                ps.setString(7, user.getRole());
                ps.setInt(8, user.getStatus());
                ps.setTimestamp(9, java.sql.Timestamp.valueOf(user.getCreateTime()));
                ps.setTimestamp(10, java.sql.Timestamp.valueOf(user.getUpdateTime()));
                ps.setBoolean(11, user.getIsDeleted());
                return ps;
            }, keyHolder);
            
            // 设置自动生成的ID到User对象
            Long generatedId = keyHolder.getKey().longValue();
            user.setId(generatedId);
        } else {
            // 更新用户
            String sql = "UPDATE users SET username = ?, password = ?, name = ?, email = ?, phone = ?, address = ?, " +
                         "role = ?, status = ?, update_time = ? WHERE id = ?";
            jdbcTemplate.update(sql, user.getUsername(), user.getPassword(), user.getName(), 
                              user.getEmail(), user.getPhone(), user.getAddress(), user.getRole(), user.getStatus(),
                              user.getUpdateTime(), user.getId());
        }
        return user;
    }
    
    /**
     * 根据ID删除用户（逻辑删除）
     */
    public int deleteById(Long id) {
        String sql = "UPDATE users SET is_deleted = 1, update_time = NOW() WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }
    
    /**
     * 根据用户名删除用户（逻辑删除）
     */
    public int deleteByUsername(String username) {
        String sql = "UPDATE users SET is_deleted = 1, update_time = NOW() WHERE username = ?";
        return jdbcTemplate.update(sql, username);
    }
    
    /**
     * 用户行映射器
     */
    private static final class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setName(rs.getString("name"));
            user.setEmail(rs.getString("email"));
            user.setPhone(rs.getString("phone"));
            user.setAddress(rs.getString("address"));
            user.setRole(rs.getString("role"));
            user.setStatus(rs.getInt("status"));
            // 转换Timestamp为LocalDateTime
            if (rs.getTimestamp("create_time") != null) {
                user.setCreateTime(rs.getTimestamp("create_time").toLocalDateTime());
            }
            if (rs.getTimestamp("update_time") != null) {
                user.setUpdateTime(rs.getTimestamp("update_time").toLocalDateTime());
            }
            user.setIsDeleted(rs.getBoolean("is_deleted"));
            return user;
        }
    }
}