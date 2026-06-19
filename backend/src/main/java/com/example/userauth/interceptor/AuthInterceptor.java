package com.example.userauth.interceptor;

import com.example.userauth.service.UserService;
import com.example.userauth.dto.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 认证拦截器
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {
    
    @Autowired
    private UserService userService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 对于需要认证的请求，验证token
        String token = request.getHeader("Authorization");
        
        // 如果没有token，允许继续执行（简化处理，实际项目中应该返回401）
        if (token == null || token.isEmpty()) {
            return true;
        }
        
        // 简单的token验证（实际项目中应该使用JWT等更安全的方式）
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
            
            // 如果是模拟token格式，提取用户名
            if (token.startsWith("generated-token-")) {
                // 这里简化处理，实际项目中应该解析JWT获取用户信息
                // 由于我们的token是模拟的，这里不做验证，直接允许通过
                return true;
            }
        }
        
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 不需要实现
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 不需要实现
    }
}