package com.example.campus.config;

import com.example.campus.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus; // 导入 HttpStatus
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

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
                .csrf().disable() // 禁用 CSRF 保护
                .authorizeRequests()
                // 允许访问的公共资源
                .antMatchers("/", "/login.html", "/css/**", "/js/**", "/api/auth/register", "/api/announcements", "/static/announcements.html").permitAll()
                // 授权访问
                .antMatchers("/api/admin/**").hasAuthority("ADMIN")
                .antMatchers("/api/teacher/**").hasAuthority("TEACHER")
                .antMatchers("/api/student/**").hasAnyAuthority("STUDENT", "ADMIN")
                // 登出接口需要认证
                .antMatchers("/api/auth/logout").authenticated()
                // 所有其他请求都需要认证
                .anyRequest().authenticated()
                .and()
                // 配置 HTTP Basic 认证
                .httpBasic()
                .and()
                // 配置退出（Logout）
                .logout()
                .logoutUrl("/api/auth/logout") // Spring Security 会处理 POST 到此 URL 的请求
                // *** 修改这里：使用 HttpStatus.OK ***
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK))
                .deleteCookies("JSESSIONID") // 尝试删除 Session Cookie
                .clearAuthentication(true) // 清除认证信息
                .invalidateHttpSession(true); // 使 HttpSession 无效
    }
}
