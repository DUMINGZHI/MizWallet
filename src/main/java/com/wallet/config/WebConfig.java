package com.wallet.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")                      // 允许所有路径的跨域请求
                .allowedOrigins("http://152.32.219.36:86","http://localhost:86")  // 允许的前端地址（如 Vue 前端地址）
                .allowedMethods("GET", "POST", "PUT", "DELETE")  // 允许的 HTTP 方法
                .allowedHeaders("*")                        // 允许的请求头
                .allowCredentials(true);                    // 是否允许发送 Cookie
    }
}
