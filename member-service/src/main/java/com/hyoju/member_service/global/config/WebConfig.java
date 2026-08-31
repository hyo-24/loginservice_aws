package com.hyoju.member_service.global.config;

import com.hyoju.member_service.global.interceptor.LoginCheckInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoginCheckInterceptor interceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor)
                .order(1)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/", "/index.html", "/*.html",
                        "/members/join",
                        "/members/login",
                        "/internal/**",
                        "/css/**", "/js/**", "/images/**", "/fonts/**",
                        "/vendor/**", "/*.ico", "/favicon.ico", "/error"
                );
    }
}