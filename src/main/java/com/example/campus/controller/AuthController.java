package com.example.campus.controller;

import com.example.campus.dto.LoginRequest;
import com.example.campus.entity.User;
import com.example.campus.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody LoginRequest req) {
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("用户名已存在");
        }
        User u = new User();
        u.setUsername(req.getUsername());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setRole(com.example.campus.entity.Role.STUDENT); // 默认注册为学生
        u.setEmail(req.getEmail()); // 设置邮箱
        u.setCreatedAt(Timestamp.from(Instant.now()));
        userRepository.save(u);
        return ResponseEntity.ok("注册成功");
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        if (user == null) return ResponseEntity.status(401).body("未登录");

        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("role", user.getAuthorities());

        return ResponseEntity.ok(response);
    }
}
