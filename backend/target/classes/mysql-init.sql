-- 创建数据库
CREATE DATABASE IF NOT EXISTS user_auth_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE user_auth_db;

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    address VARCHAR(200),
    role VARCHAR(20) NOT NULL DEFAULT 'user',
    status INT NOT NULL DEFAULT 1,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入初始管理员数据
INSERT INTO users (username, password, name, email, role, status, is_deleted, create_time, update_time) 
VALUES ('admin', '$2a$10$KvALCWs9o.tnj3Q3AMlYu.vwhTJj1lZaaH3P2h/zL9DOB45dLu5PC', '管理员', 'admin@example.com', 'admin', 1, FALSE, NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name), password = VALUES(password);