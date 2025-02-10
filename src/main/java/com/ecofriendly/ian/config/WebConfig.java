package com.ecofriendly.ian.config;

import com.ecofriendly.ian.interceptor.AuthenticationInterceptor;
import com.ecofriendly.ian.interceptor.RateLimitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AuthenticationInterceptor authenticationInterceptor;
    private final RateLimitInterceptor rateLimitInterceptor;

    @Autowired
    public WebConfig(AuthenticationInterceptor authenticationInterceptor, RateLimitInterceptor rateLimitInterceptor) {
        this.authenticationInterceptor = authenticationInterceptor;
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/**");  // Apply to everything

        registry.addInterceptor(authenticationInterceptor)
                .addPathPatterns("/api/**")  // Protect all API endpoints
                .addPathPatterns("/home", "/createVehicle")
                .addPathPatterns("/user", "/edit")
                .excludePathPatterns(
                        "/api/login",           // Public endpoints
                        "/api/addCustomer"
                );
    }
}
