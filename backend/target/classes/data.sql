-- 插入初始用户数据
INSERT INTO users (username, password, name, email, role, status, create_time, update_time, is_deleted) VALUES
('admin', '$2a$10$KvALCWs9o.tnj3Q3AMlYu.vwhTJj1lZaaH3P2h/zL9DOB45dLu5PC', '\u7ba1\u7406\u5458', 'admin@example.com', 'admin', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false);