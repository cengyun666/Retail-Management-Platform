package com.example.userauth.service;

import com.example.userauth.dto.LoginRequest;
import com.example.userauth.dto.LoginResponse;
import com.example.userauth.dto.RegisterRequest;
import com.example.userauth.dto.UserInfo;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 获取所有用户列表
     */
    List<UserInfo> getAllUsers();
    
    /**
     * 根据条件获取过滤后的用户列表
     */
    List<UserInfo> getFilteredUsers(String search, String role, Integer status);
    
    /**
     * 用户注册
     */
    UserInfo register(RegisterRequest registerRequest);
    
    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest loginRequest);
    
    /**
     * 根据用户名获取用户信息
     */
    UserInfo getUserInfo(String username);
    
    /**
     * 根据token获取用户信息
     */
    UserInfo getUserInfoByToken(String token);
    
    /**
     * 根据ID获取用户信息
     */
    UserInfo getUserInfoById(Long id);
    
    /**
     * 删除用户
     */
    void deleteUser(Long id);
    
    /**
     * 更新用户信息
     */
    UserInfo updateUser(Long id, UserInfo userInfo);
}