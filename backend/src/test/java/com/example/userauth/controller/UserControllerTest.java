package com.example.userauth.controller;

import com.example.userauth.dto.ApiResponse;
import com.example.userauth.dto.LoginRequest;
import com.example.userauth.dto.LoginResponse;
import com.example.userauth.dto.RegisterRequest;
import com.example.userauth.dto.UserInfo;
import com.example.userauth.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 用户控制器测试类
 */
@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private UserService userService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private UserInfo userInfo;
    private LoginResponse loginResponse;
    
    @BeforeEach
    void setUp() {
        
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
        
        // 创建用户信息
        userInfo = new UserInfo();
        userInfo.setId(1L);
        userInfo.setUsername("testuser");
        userInfo.setName("测试用户");
        userInfo.setEmail("test@example.com");
        userInfo.setPhone("13800138000");
        userInfo.setAddress("测试地址");
        userInfo.setRole("user");
        userInfo.setStatus(1);
        
        // 创建登录响应
        loginResponse = new LoginResponse(userInfo);
    }
    
    @Test
    void testRegisterSuccess() throws Exception {
        // 创建返回的用户信息（与注册请求中的用户信息一致）
        UserInfo returnedUserInfo = new UserInfo();
        returnedUserInfo.setId(1L);
        returnedUserInfo.setUsername("newuser");
        returnedUserInfo.setName("新用户");
        returnedUserInfo.setEmail("newuser@example.com");
        returnedUserInfo.setPhone("13900139000");
        returnedUserInfo.setAddress("新用户地址");
        returnedUserInfo.setRole("user");
        returnedUserInfo.setStatus(1);
        
        // 模拟服务层返回
        when(userService.register(any(RegisterRequest.class))).thenReturn(returnedUserInfo);
        
        // 执行请求并验证结果
        mockMvc.perform(post("/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("注册成功"))
                .andExpect(jsonPath("$.data.username").value("newuser"))
                .andExpect(jsonPath("$.data.name").value("新用户"));
    }
    
    @Test
    void testRegisterWithInvalidData() throws Exception {
        // 创建无效的注册请求（缺少用户名）
        RegisterRequest invalidRequest = new RegisterRequest();
        invalidRequest.setPassword("password123");
        invalidRequest.setName("新用户");
        invalidRequest.setEmail("newuser@example.com");
        
        // 执行请求并验证结果
        mockMvc.perform(post("/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("参数校验失败"));
    }
    
    @Test
    void testLoginSuccess() throws Exception {
        // 模拟服务层返回
        when(userService.login(any(LoginRequest.class))).thenReturn(loginResponse);
        
        // 执行请求并验证结果
        mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("登录成功"))
                .andExpect(jsonPath("$.data.userInfo.username").value("testuser"));
    }
    
    @Test
    void testLoginWithInvalidData() throws Exception {
        // 创建无效的登录请求（缺少密码）
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setUsername("testuser");
        
        // 执行请求并验证结果
        mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("参数校验失败"));
    }
    
    @Test
    @WithMockUser
    void testGetUserInfoSuccess() throws Exception {
        // 模拟服务层返回
        when(userService.getUserInfo(anyString())).thenReturn(userInfo);
        
        // 执行请求并验证结果
        mockMvc.perform(get("/users/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取用户信息成功"))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.name").value("测试用户"));
    }
    
    @Test
    @WithMockUser
    void testGetUserInfoByIdSuccess() throws Exception {
        // 模拟服务层返回
        when(userService.getUserInfoById(anyLong())).thenReturn(userInfo);
        
        // 执行请求并验证结果
        mockMvc.perform(get("/users/id/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取用户信息成功"))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.name").value("测试用户"));
    }
}