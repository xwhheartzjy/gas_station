package org.codec.interceptor;

import org.codec.common.RequestContext;
import org.codec.entity.SysUser;
import org.codec.util.JwtTokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class TokenInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtTokenUtils jwtTokenUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            // 解析 Token 并存储用户信息到上下文中
            SysUser user = jwtTokenUtils.getAuthenticationUser(token);
            RequestContext.setCurrentUser(user); // 自定义上下文存储
        }
        return true;
    }
}