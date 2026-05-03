// 3️⃣ WebConfig.java (글로벌 CORS 설정)
package com.market_be.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
 
 @Configuration
 public class WebConfig implements WebMvcConfigurer {
     @Override
     public void addCorsMappings(CorsRegistry registry) {
         registry.addMapping("/**")
                 .allowedOriginPatterns("http://localhost:5173", "https://*.vercel.app")
                 .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                 .allowedHeaders("*")
                 .allowCredentials(true);
     }
 
     @Override
     public void addResourceHandlers(ResourceHandlerRegistry registry) {
         registry.addResourceHandler("/uploads/**")
                 .addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/");
     }
 }

