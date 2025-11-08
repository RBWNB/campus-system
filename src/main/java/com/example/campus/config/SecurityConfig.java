package com.example.campus.config;

import com.example.campus.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder(){ //密码加密器，加密方式使用 BCrypt
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception { //身份验证器，绑定了数据库用户认证逻辑
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/", "/static/announcements.html", "/login.html", "/css/**", "/js/**", "/api/auth/register").permitAll()
                .antMatchers("/api/admin/**").hasAuthority("ADMIN")
                .antMatchers("/api/teacher/**").hasAuthority("TEACHER")
                .antMatchers("/api/student/**").hasAnyAuthority("STUDENT", "ADMIN")
                .anyRequest().authenticated()
                .and()
                .httpBasic();
    }
}
