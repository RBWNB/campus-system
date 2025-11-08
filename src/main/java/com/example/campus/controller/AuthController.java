package com.example.campus.controller;

import com.example.campus.dto.LoginRequest;
import com.example.campus.entity.Role; // 🔥 必须导入独立的 Role 枚举
import com.example.campus.entity.Teacher;
import com.example.campus.entity.User;
import com.example.campus.repository.TeacherRepository;
import com.example.campus.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
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
    private TeacherRepository teacherRepository; // 注入教师仓库

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
        u.setRole(Role.STUDENT); // 🔥 使用独立的 Role 枚举，无前缀
        u.setEmail(req.getEmail());
        u.setCreatedAt(Timestamp.from(Instant.now()));
        userRepository.save(u);
        return ResponseEntity.ok("注册成功");
    }

    @PostMapping("/admin/register")
    public ResponseEntity<?> adminRegister(@RequestBody AdminRegisterRequest req,
                                           @AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser) {
        // 验证权限：注意这里判断的是字符串 "ADMIN"（和枚举名一致）
        if (currentUser == null || !currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(Role.ADMIN.name()))) { // 🔥 用枚举名避免硬编码
            return ResponseEntity.status(403).body("权限不足");
        }

        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("用户名已存在");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(req.getRole()); // 🔥 确保 AdminRegisterRequest 的 role 字段类型是 Role
        user.setEmail(req.getEmail());
        user.setName(req.getName()); // 补充姓名赋值
        user.setCreatedAt(Timestamp.from(Instant.now()));

        // 学生角色判断：使用独立的 Role 枚举（保持原有逻辑）
        if (Role.STUDENT.equals(req.getRole()) && req.getStudent() != null) { // 🔥 无前缀，直接用 Role 枚举
            // 处理学生信息保存逻辑（如果需要，可参考教师关联逻辑）
        }

        // 新增：教师角色处理 - 创建关联的 Teacher 实体
        if (Role.TEACHER.equals(req.getRole()) && req.getTeacher() != null) {
            // 校验教师必填字段
            if (req.getTeacher().getTeacherNo() == null || req.getTeacher().getTeacherNo().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("教师编号不能为空");
            }
            // 校验教师编号唯一性
            if (teacherRepository.findByTeacherNo(req.getTeacher().getTeacherNo()).isPresent()) {
                return ResponseEntity.badRequest().body("教师编号已存在");
            }

            // 先保存用户（获取主键ID用于关联）
            User savedUser = userRepository.save(user);

            // 创建教师实体并关联用户
            Teacher teacher = new Teacher();
            teacher.setUser(savedUser);
            teacher.setTeacherNo(req.getTeacher().getTeacherNo().trim());
            teacher.setTitle(req.getTeacher().getTitle() != null ? req.getTeacher().getTitle().trim() : "");
            teacher.setDepartment(req.getTeacher().getDepartment() != null ? req.getTeacher().getDepartment().trim() : "");
            teacher.setPhone(req.getTeacher().getPhone() != null ? req.getTeacher().getPhone().trim() : "");
            teacher.setOffice(req.getTeacher().getOffice() != null ? req.getTeacher().getOffice().trim() : "");
            teacherRepository.save(teacher);

            return ResponseEntity.ok("注册成功");
        }

        // 非教师角色直接保存用户
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