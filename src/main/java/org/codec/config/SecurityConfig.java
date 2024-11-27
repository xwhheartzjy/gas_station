package org.codec.config;

import org.codec.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collections;

/**
 * @Author cross
 */
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                // 登录接口开放
                .antMatchers("/login").permitAll()
                .anyRequest().authenticated()      // 其他接口需要认证
                .and()
                .exceptionHandling()
                .authenticationEntryPoint((request, response, authException) -> {
                    // 未认证时返回 401 错误
                    response.setContentType("application/json");
                    response.setStatus(401);
                    response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Access is denied\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    // 权限不足时返回 403 错误
                    response.setContentType("application/json");
                    response.setStatus(403);
                    response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"Access is denied\"}");
                })
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 密码加密
    }

    @Bean
    protected UserDetailsService userDetailsService() {
        return username -> {
            // 自定义用户加载逻辑
            if ("admin".equals(username)) {
                return new User("admin",
                        new BCryptPasswordEncoder().encode("123456"),
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
            }
            throw new UsernameNotFoundException("User not found");
        };
    }
}
