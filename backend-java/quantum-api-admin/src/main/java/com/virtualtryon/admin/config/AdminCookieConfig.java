package com.virtualtryon.admin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminCookieConfig {

    @Value("${app.cookie-secure:false}")
    private boolean cookieSecure;

    @Bean
    public AdminCookieHelper adminCookieHelper() {
        return new AdminCookieHelper(cookieSecure);
    }
}
