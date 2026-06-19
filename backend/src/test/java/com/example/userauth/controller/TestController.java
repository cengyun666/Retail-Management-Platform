package com.example.userauth.controller;

import com.example.userauth.dto.ApiResponse;
import com.example.userauth.util.PasswordUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 */
@Api(tags = "测试接口")
@RestController
@RequestMapping("/test")
@CrossOrigin(origins = "*")
public class TestController {
    
    /**
     * 生成密码
     */
    @ApiOperation("生成密码")
    @GetMapping("/generate-password")
    public ApiResponse<Map<String, Object>> generatePassword(@RequestParam(defaultValue = "123456") String password) {
        String encodedPassword = PasswordUtil.encode(password);
        boolean matches = PasswordUtil.matches(password, encodedPassword);
        
        // 验证数据库中的密码
        String dbPassword = "$2a$10$KvALCWs9o.tnj3Q3AMlYu.vwhTJj1lZaaH3P2h/zL9DOB45dLu5PC";
        boolean dbMatches = PasswordUtil.matches(password, dbPassword);
        
        Map<String, Object> result = new HashMap<>();
        result.put("originalPassword", password);
        result.put("encodedPassword", encodedPassword);
        result.put("matches", matches);
        result.put("dbPassword", dbPassword);
        result.put("dbMatches", dbMatches);
        
        return ApiResponse.success("生成密码成功", result);
    }
}