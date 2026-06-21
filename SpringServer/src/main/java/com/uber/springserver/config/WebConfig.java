package com.uber.springserver.config;

import com.uber.springserver.utils.EnvUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> origins = EnvUtil.getCsv("ALLOWED_ORIGINS", "http://localhost:5173");
        registry.addMapping("/**")
                .allowedOrigins(origins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Authorization")
                .allowCredentials(true);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns(
                        "/api/user/profile",
                        "/api/user/logout",
                        "/api/captain/profile",
                        "/api/captain/logout",
                        "/api/maps/**",
                        "/api/ride/**",
                        "/api/rides/**"
                )
                .excludePathPatterns(
                        "/api/user/register",
                        "/api/user/login",
                        "/api/user/refresh",
                        "/api/captain/register",
                        "/api/captain/login",
                        "/api/captain/refresh"
                );
    }
}
