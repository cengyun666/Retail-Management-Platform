package com.example.userauth.service.impl;

import com.example.userauth.dto.LoginRequest;
import com.example.userauth.dto.LoginResponse;
import com.example.userauth.dto.RegisterRequest;
import com.example.userauth.dto.UserInfo;
import com.example.userauth.entity.User;
import com.example.userauth.exception.BusinessException;
import com.example.userauth.repository.UserRepository;
import com.example.userauth.service.UserService;
import com.example.userauth.util.PasswordUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public List<UserInfo> getAllUsers() {
        List<User> users = userRepository.findByIsDeletedFalse();
        return users.stream()
                .map(this::convertToUserInfo)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<UserInfo> getFilteredUsers(String search, String role, Integer status) {
        List<User> users = userRepository.findWithFilters(search, role, status);
        return users.stream()
                .map(this::convertToUserInfo)
                .collect(Collectors.toList());
    }
    
    @Override
    public UserInfo register(RegisterRequest registerRequest) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsernameAndIsDeletedFalse(registerRequest.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        
        // 检查邮箱是否已存在（仅当邮箱不为null时）
        if (registerRequest.getEmail() != null && !registerRequest.getEmail().isEmpty()) {
            if (userRepository.existsByEmailAndIsDeletedFalse(registerRequest.getEmail())) {
                throw new BusinessException("邮箱已被使用");
            }
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        // 密码加密
        user.setPassword(PasswordUtil.encodePassword(registerRequest.getPassword()));
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setPhone(registerRequest.getPhone());
        user.setAddress(registerRequest.getAddress());
        // 默认角色为普通用户
        user.setRole("user");
        // 默认状态为激活
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setIsDeleted(false);
        
        // 保存用户
        User savedUser = userRepository.save(user);
        
        // 转换为UserInfo并返回（不包含密码）
        return convertToUserInfo(savedUser);
    }
    
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        // 根据用户名查找用户
        User user = userRepository.findByUsernameAndIsDeletedFalse(loginRequest.getUsername());
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 验证密码
        if (!PasswordUtil.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用，请联系管理员");
        }
        
        // 生成登录响应
        LoginResponse loginResponse = new LoginResponse();
        // 在token中包含用户名信息，方便后续解析
        loginResponse.setToken("generated-token-" + user.getUsername() + "-" + System.currentTimeMillis()); // 实际项目中应使用JWT等
        loginResponse.setUserId(user.getId());
        loginResponse.setUsername(user.getUsername());
        loginResponse.setRole(user.getRole());
        // 添加用户信息
        loginResponse.setUserInfo(convertToUserInfo(user));
        
        return loginResponse;
    }
    
    @Override
    public UserInfo getUserInfo(String username) {
        User user = userRepository.findByUsernameAndIsDeletedFalse(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return convertToUserInfo(user);
    }
    
    @Override
    public UserInfo getUserInfoByToken(String token) {
        // 添加日志，查看实际接收到的token
        System.out.println("Received token: " + token);
        // 简单的token验证（实际项目中应该使用JWT等更安全的方式）
        if (token == null || !token.startsWith("generated-token-")) {
            throw new RuntimeException("无效的token");
        }
        
        // 从token中提取用户名信息（token格式：generated-token-{username}-{timestamp}）
        // 正确处理用户名中包含'-'的情况
        int startIndex = "generated-token-".length();
        int endIndex = token.lastIndexOf("-");
        if (endIndex <= startIndex) {
            throw new RuntimeException("无效的token格式");
        }
        String username = token.substring(startIndex, endIndex);
        
        // 根据用户名查询实际用户信息
        User user = userRepository.findByUsernameAndIsDeletedFalse(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        return convertToUserInfo(user);
    }
    
    @Override
    public UserInfo getUserInfoById(Long id) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return convertToUserInfo(user);
    }
    
    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        userRepository.deleteById(id); // 这个方法已经实现了逻辑删除，将is_deleted设置为1
    }
    
    @Override
    public UserInfo updateUser(Long id, UserInfo userInfo) {
        User existingUser = userRepository.findById(id);
        if (existingUser == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 只更新提供的字段，而不是全部字段
        if (userInfo.getName() != null) {
            existingUser.setName(userInfo.getName());
        }
        if (userInfo.getEmail() != null) {
            existingUser.setEmail(userInfo.getEmail());
        }
        if (userInfo.getPhone() != null) {
            existingUser.setPhone(userInfo.getPhone());
        }
        if (userInfo.getRole() != null) {
            existingUser.setRole(userInfo.getRole());
        }
        if (userInfo.getStatus() != null) {
            existingUser.setStatus(userInfo.getStatus());
        }
        
        existingUser.setUpdateTime(LocalDateTime.now());
        
        // 如果提供了新密码，则更新密码
        if (userInfo.getPassword() != null && !userInfo.getPassword().isEmpty()) {
            existingUser.setPassword(PasswordUtil.encodePassword(userInfo.getPassword()));
        }
        
        // 保存更新后的用户
        User updatedUser = userRepository.save(existingUser);
        
        // 转换为UserInfo并返回（不包含密码）
        return convertToUserInfo(updatedUser);
    }
    
    /**
     * 将User实体转换为UserInfo DTO
     */
    private UserInfo convertToUserInfo(User user) {
        UserInfo userInfo = new UserInfo();
        BeanUtils.copyProperties(user, userInfo);
        // 不返回密码
        userInfo.setPassword(null);
        return userInfo;
    }
}