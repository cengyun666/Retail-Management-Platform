package com.example.userauth.service;

import com.example.userauth.dto.LoginRequest;
import com.example.userauth.dto.LoginResponse;
import com.example.userauth.dto.RegisterRequest;
import com.example.userauth.dto.UserInfo;
import com.example.userauth.entity.User;
import com.example.userauth.enums.UserStatus;
import com.example.userauth.exception.BusinessException;
import com.example.userauth.repository.UserRepository;
import com.example.userauth.service.impl.UserServiceImpl;
import com.example.userauth.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 用户服务测试类
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserServiceImpl userService;
    
    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    
    @BeforeEach
    void setUp() {
        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        // 使用PasswordUtil生成正确的密码哈希
        testUser.setPassword(PasswordUtil.encodePassword("password"));
        testUser.setName("测试用户");
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138000");
        testUser.setAddress("测试地址");
        testUser.setRole("user");
        testUser.setStatus(UserStatus.ENABLED.getCode());
        testUser.setIsDeleted(false);
        
        // 创建注册请求
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setName("新用户");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPhone("13900139000");
        registerRequest.setAddress("新用户地址");
        
        // 创建登录请求
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");
    }
    
    @Test
    void testRegisterSuccess() {
        // 模拟用户名和邮箱不存在
        when(userRepository.existsByUsernameAndIsDeletedFalse("newuser")).thenReturn(false);
        when(userRepository.existsByEmailAndIsDeletedFalse("newuser@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // 调用注册方法
        UserInfo result = userService.register(registerRequest);
        
        // 验证结果
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("测试用户", result.getName());
        
        // 验证方法调用
        verify(userRepository).existsByUsernameAndIsDeletedFalse("newuser");
        verify(userRepository).existsByEmailAndIsDeletedFalse("newuser@example.com");
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    void testRegisterWithExistingUsername() {
        // 模拟用户名已存在
        when(userRepository.existsByUsernameAndIsDeletedFalse("newuser")).thenReturn(true);
        
        // 调用注册方法并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.register(registerRequest);
        });
        
        assertEquals("用户名已存在", exception.getMessage());
        
        // 验证方法调用
        verify(userRepository).existsByUsernameAndIsDeletedFalse("newuser");
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void testRegisterWithExistingEmail() {
        // 模拟用户名不存在，但邮箱已存在
        when(userRepository.existsByUsernameAndIsDeletedFalse("newuser")).thenReturn(false);
        when(userRepository.existsByEmailAndIsDeletedFalse("newuser@example.com")).thenReturn(true);
        
        // 调用注册方法并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.register(registerRequest);
        });
        
        assertEquals("邮箱已被使用", exception.getMessage());
        
        // 验证方法调用
        verify(userRepository).existsByUsernameAndIsDeletedFalse("newuser");
        verify(userRepository).existsByEmailAndIsDeletedFalse("newuser@example.com");
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void testLoginSuccess() {
        // 确保测试用户状态为启用
        testUser.setStatus(UserStatus.ENABLED.getCode());
        
        // 模拟用户存在且密码正确
        when(userRepository.findByUsernameAndIsDeletedFalse("testuser")).thenReturn(testUser);
        
        // 调用登录方法
        LoginResponse result = userService.login(loginRequest);
        
        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getUserInfo());
        assertEquals("testuser", result.getUserInfo().getUsername());
        
        // 验证方法调用
        verify(userRepository).findByUsernameAndIsDeletedFalse("testuser");
    }
    
    @Test
    void testLoginWithInvalidUsername() {
        // 模拟用户不存在
        when(userRepository.findByUsernameAndIsDeletedFalse("testuser")).thenReturn(null);
        
        // 调用登录方法并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.login(loginRequest);
        });
        
        assertEquals("用户名或密码错误", exception.getMessage());
        
        // 验证方法调用
        verify(userRepository).findByUsernameAndIsDeletedFalse("testuser");
    }
    
    @Test
    void testLoginWithInvalidPassword() {
        // 修改登录请求中的密码
        loginRequest.setPassword("wrongpassword");
        
        // 模拟用户存在
        when(userRepository.findByUsernameAndIsDeletedFalse("testuser")).thenReturn(testUser);
        
        // 调用登录方法并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.login(loginRequest);
        });
        
        assertEquals("用户名或密码错误", exception.getMessage());
        
        // 验证方法调用
        verify(userRepository).findByUsernameAndIsDeletedFalse("testuser");
    }
    
    @Test
    void testLoginWithDisabledUser() {
        // 修改测试用户状态为禁用
        testUser.setStatus(UserStatus.DISABLED.getCode());
        
        // 模拟用户存在但已被禁用
        when(userRepository.findByUsernameAndIsDeletedFalse("testuser")).thenReturn(testUser);
        
        // 调用登录方法并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.login(loginRequest);
        });
        
        assertEquals("账号已被禁用，请联系管理员", exception.getMessage());
        
        // 验证方法调用
        verify(userRepository).findByUsernameAndIsDeletedFalse("testuser");
    }
    
    @Test
    void testGetUserInfoWithNonExistentUser() {
        // 模拟用户不存在
        when(userRepository.findByUsernameAndIsDeletedFalse("nonexistent")).thenReturn(null);
        
        // 调用获取用户信息方法并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.getUserInfo("nonexistent");
        });
        
        assertEquals("用户不存在", exception.getMessage());
        
        // 验证方法调用
        verify(userRepository).findByUsernameAndIsDeletedFalse("nonexistent");
    }
}