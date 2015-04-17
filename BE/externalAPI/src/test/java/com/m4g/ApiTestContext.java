package com.m4g;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by MaxG on 17-Apr-15.
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {"com.m4g.controllers"})
public class ApiTestContext extends WebMvcConfigurerAdapter {
}
