package com.example.userauth.controller;

import com.example.userauth.dto.ApiResponse;
import com.example.userauth.dto.LoginRequest;
import com.example.userauth.dto.LoginResponse;
import com.example.userauth.dto.RegisterRequest;
import com.example.userauth.dto.UserInfo;
import com.example.userauth.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 用户控制器
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/users")
@Validated
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取用户列表
     */
    @ApiOperation("获取用户列表")
    @GetMapping("/list")
    public ApiResponse<List<UserInfo>> getUserList(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status) {
        List<UserInfo> userList = userService.getFilteredUsers(search, role, status);
        return ApiResponse.success("获取用户列表成功", userList);
    }
    
    /**
     * 用户注册
     */
    @ApiOperation("用户注册")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserInfo> register(@Valid @RequestBody RegisterRequest registerRequest) {
        UserInfo userInfo = userService.register(registerRequest);
        return ApiResponse.success("注册成功", userInfo);
    }
    
    /**
     * 用户登录
     */
    @ApiOperation("用户登录")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = userService.login(loginRequest);
        return ApiResponse.success("登录成功", loginResponse);
    }
    
    /**
     * 获取用户信息
     */
    @ApiOperation("获取用户信息")
    @GetMapping("/{username}")
    public ApiResponse<UserInfo> getUserInfo(@PathVariable String username) {
        UserInfo userInfo = userService.getUserInfo(username);
        return ApiResponse.success("获取用户信息成功", userInfo);
    }
    
    /**
     * 根据token获取用户信息
     */
    @ApiOperation("根据token获取用户信息")
    @GetMapping("/token")
    public ApiResponse<UserInfo> getUserInfoByToken(@RequestHeader("Authorization") String authorization) {
        // 移除Bearer前缀
        String token = authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
        UserInfo userInfo = userService.getUserInfoByToken(token);
        return ApiResponse.success("获取用户信息成功", userInfo);
    }
    
    /**
     * 根据ID获取用户信息
     */
    @ApiOperation("根据ID获取用户信息")
    @GetMapping("/id/{id}")
    public ApiResponse<UserInfo> getUserInfoById(@PathVariable Long id) {
        UserInfo userInfo = userService.getUserInfoById(id);
        return ApiResponse.success("获取用户信息成功", userInfo);
    }
    
    /**
     * 删除用户
     */
    @ApiOperation("删除用户")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success("删除用户成功", null);
    }
    
    /**
     * 更新用户信息
     */
    @ApiOperation("更新用户信息")
    @PutMapping("/{id}")
    public ApiResponse<UserInfo> updateUser(@PathVariable Long id, @RequestBody UserInfo userInfo) {
        UserInfo updatedUser = userService.updateUser(id, userInfo);
        return ApiResponse.success("更新用户成功", updatedUser);
    }
}