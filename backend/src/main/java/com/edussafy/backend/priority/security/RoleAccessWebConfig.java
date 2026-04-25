package com.edussafy.backend.priority.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnProperty(prefix = "edussafy.auth.interceptor", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RoleAccessWebConfig implements WebMvcConfigurer {

    private final RoleAccessInterceptor roleAccessInterceptor;

    public RoleAccessWebConfig(RoleAccessInterceptor roleAccessInterceptor) {
        this.roleAccessInterceptor = roleAccessInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(roleAccessInterceptor).addPathPatterns("/api/**");
    }
}
