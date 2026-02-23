package com.virtualtryon.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CookieConfig {

    @Value("${app.cookie-secure:false}")
    private boolean cookieSecure;

    @Bean
    public AuthCookieHelper authCookieHelper() {
        return new AuthCookieHelper(cookieSecure);
    }
}
