package com.ecom.config;

import java.io.File;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer 
{
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) 
    {
        String uploadPath = System.getProperty("user.home") 
                          + File.separator + "ecom_uploads" + File.separator;

        registry.addResourceHandler("/img/product_img/**")
                .addResourceLocations("file:" + uploadPath + "product_img/");

        registry.addResourceHandler("/img/category_img/**")
                .addResourceLocations("file:" + uploadPath + "category_img/");

        registry.addResourceHandler("/img/Profile_img/**")
                .addResourceLocations("file:" + uploadPath + "Profile_img/");
    }
}
