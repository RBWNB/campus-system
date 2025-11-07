package com.example.campus.controller;

import com.example.campus.dto.LoginRequest;
import com.example.campus.entity.User;
import com.example.campus.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.example.campus.dto.AdminRegisterRequest;

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

    @PostMapping("/admin/register")
    public ResponseEntity<?> adminRegister(@RequestBody AdminRegisterRequest req,
                                           @AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser) {
        // 验证当前用户权限（需要是管理员）
        if (currentUser == null || !currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ADMIN"))) {
            return ResponseEntity.status(403).body("权限不足");
        }

        // 检查用户名是否已存在
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("用户名已存在");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(req.getRole()); // 可以指定角色
        user.setEmail(req.getEmail());
        user.setCreatedAt(Timestamp.from(Instant.now()));

        // 如果是学生角色，还需要处理学生额外信息
        if (req.getRole() == com.example.campus.entity.Role.STUDENT && req.getStudent() != null) {
            // 处理学生信息保存逻辑
        }

        userRepository.save(user);
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
