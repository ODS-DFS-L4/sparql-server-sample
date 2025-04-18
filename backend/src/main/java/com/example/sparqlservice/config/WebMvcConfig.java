package com.example.sparqlservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final String APP_VERSION_HEADER_NAME;
    private final String APP_VERSION;

    public WebMvcConfig(@Value("${app.version}") String appVersion,
            @Value("${app.versionHeaderName}") String versionHeaderName) {
        if (appVersion == null || versionHeaderName == null) {
            throw new IllegalArgumentException("Application version and header name must not be null");
        }
        this.APP_VERSION = appVersion;
        this.APP_VERSION_HEADER_NAME = versionHeaderName;
    }

    /**
     * Configures CORS for the application.
     *
     * Allowed for all origins as it is expected to be called from various clients.
     *
     * @param registry the {@link CorsRegistry} to configure CORS mappings
     */
    @SuppressWarnings("null")
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
    }

    /**
     * Adds the Application Version to the Response Header.
     *
     * @param registry
     */
    @Override
    @SuppressWarnings("null")
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request,
                    HttpServletResponse response, Object handler) {
                response.setHeader(APP_VERSION_HEADER_NAME, APP_VERSION);
                return true;
            }
        });
    }
}