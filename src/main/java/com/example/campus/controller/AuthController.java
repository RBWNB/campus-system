package com.example.campus.controller;

import com.example.campus.dto.LoginRequest;
import com.example.campus.entity.Role;
import com.example.campus.entity.Student;
import com.example.campus.entity.Teacher;
import com.example.campus.entity.User;
import com.example.campus.repository.StudentRepository;
import com.example.campus.repository.TeacherRepository;
import com.example.campus.repository.UserRepository;
import com.example.campus.dto.AdminRegisterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 学生注册（添加事务+修复学号递增）
    @Transactional
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody LoginRequest req) {
        try {
            // 1. 验证用户名是否已存在
            if (userRepository.findByUsername(req.getUsername()).isPresent()) {
                logger.warn("用户名已存在：{}", req.getUsername());
                return ResponseEntity.badRequest().body("用户名已存在");
            }

            // 2. 创建用户对象（学生角色）
            User u = new User();
            u.setUsername(req.getUsername());
            u.setPassword(passwordEncoder.encode(req.getPassword()));
            u.setRole(Role.STUDENT);
            u.setEmail(req.getEmail());
            u.setCreatedAt(Timestamp.from(Instant.now()));
            User savedUser = userRepository.save(u);
            logger.info("用户创建成功：{}，用户ID：{}", req.getUsername(), savedUser.getId());

            // 3. 生成唯一递增学号
            String studentNo = generateUniqueStudentNo();
            logger.info("为用户 {} 分配学号：{}", req.getUsername(), studentNo);

            // 4. 创建学生记录并关联用户
            Student student = new Student();
            student.setUser(savedUser);
            student.setStudentNo(studentNo);
            student.setMajor("");
            student.setGrade("");
            student.setPhone("");
            student.setAddress("");
            studentRepository.save(student);
            logger.info("学生记录创建成功：学号 {}，关联用户ID：{}", studentNo, savedUser.getId());

            return ResponseEntity.ok("注册成功，自动分配学号：" + studentNo);
        } catch (Exception e) {
            logger.error("学生注册失败！请求参数：{}", req, e);
            return ResponseEntity.status(500).body("注册失败：" + e.getMessage());
        }
    }

    // 管理员注册（保持原有逻辑不变）
    @PostMapping("/admin/register")
    public ResponseEntity<?> adminRegister(@RequestBody AdminRegisterRequest req,
                                           @AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser) {
        try {
            if (currentUser == null || !currentUser.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals(Role.ADMIN.name()))) {
                logger.warn("非管理员尝试注册用户：{}", currentUser);
                return ResponseEntity.status(403).body("权限不足");
            }

            if (userRepository.findByUsername(req.getUsername()).isPresent()) {
                logger.warn("管理员注册：用户名已存在：{}", req.getUsername());
                return ResponseEntity.badRequest().body("用户名已存在");
            }

            User user = new User();
            user.setUsername(req.getUsername());
            user.setPassword(passwordEncoder.encode(req.getPassword()));
            user.setRole(req.getRole());
            user.setEmail(req.getEmail());
            user.setName(req.getName());
            user.setCreatedAt(Timestamp.from(Instant.now()));

            if (Role.STUDENT.equals(req.getRole()) && req.getStudent() != null) {
                User savedUser = userRepository.save(user);
                Student student = new Student();
                student.setUser(savedUser);
                student.setStudentNo(req.getStudent().getStudentNo().trim());
                student.setMajor(req.getStudent().getMajor() != null ? req.getStudent().getMajor().trim() : "");
                student.setGrade(req.getStudent().getGrade() != null ? req.getStudent().getGrade().trim() : "");
                student.setPhone(req.getStudent().getPhone() != null ? req.getStudent().getPhone().trim() : "");
                student.setAddress(req.getStudent().getAddress() != null ? req.getStudent().getAddress().trim() : "");
                studentRepository.save(student);
                logger.info("管理员注册学生成功：{}，学号：{}", req.getUsername(), req.getStudent().getStudentNo().trim());
                return ResponseEntity.ok("注册成功");
            }

            if (Role.TEACHER.equals(req.getRole()) && req.getTeacher() != null) {
                if (req.getTeacher().getTeacherNo() == null || req.getTeacher().getTeacherNo().trim().isEmpty()) {
                    return ResponseEntity.badRequest().body("教师编号不能为空");
                }
                if (teacherRepository.findByTeacherNo(req.getTeacher().getTeacherNo()).isPresent()) {
                    return ResponseEntity.badRequest().body("教师编号已存在");
                }

                User savedUser = userRepository.save(user);
                Teacher teacher = new Teacher();
                teacher.setUser(savedUser);
                teacher.setTeacherNo(req.getTeacher().getTeacherNo().trim());
                teacher.setTitle(req.getTeacher().getTitle() != null ? req.getTeacher().getTitle().trim() : "");
                teacher.setDepartment(req.getTeacher().getDepartment() != null ? req.getTeacher().getDepartment().trim() : "");
                teacher.setPhone(req.getTeacher().getPhone() != null ? req.getTeacher().getPhone().trim() : "");
                teacher.setOffice(req.getTeacher().getOffice() != null ? req.getTeacher().getOffice().trim() : "");
                teacherRepository.save(teacher);
                logger.info("管理员注册教师成功：{}，教师编号：{}", req.getUsername(), req.getTeacher().getTeacherNo().trim());
                return ResponseEntity.ok("注册成功");
            }

            userRepository.save(user);
            logger.info("管理员注册普通用户成功：{}", req.getUsername());
            return ResponseEntity.ok("注册成功");
        } catch (Exception e) {
            logger.error("管理员注册失败！请求参数：{}", req, e);
            return ResponseEntity.status(500).body("注册失败：" + e.getMessage());
        }
    }

    // 获取当前登录用户信息（保持原有逻辑不变）
    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        if (user == null) return ResponseEntity.status(401).body("未登录");

        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("role", user.getAuthorities());

        return ResponseEntity.ok(response);
    }

    // 生成唯一递增学号（修复默认值，避免 00000001）
    private String generateUniqueStudentNo() {
        int maxRetry = 5;
        int retryCount = 0;

        while (retryCount < maxRetry) {
            try {
                // 查询最大学号（原生SQL）
                String maxStudentNo = studentRepository.findMaxStudentNoNative();
                logger.info("查询到最大学号：{}", maxStudentNo);

                String newStudentNo;
                // 强化判断：空字符串、0、非数字都视为无有效学号
                if (maxStudentNo == null || maxStudentNo.trim().isEmpty()
                        || !maxStudentNo.matches("\\d+") || Long.parseLong(maxStudentNo.trim()) == 0) {
                    // 无有效学号时，强制返回 20250001（而非 00000001）
                    newStudentNo = "20250001";
                } else {
                    // 有效学号：按数字递增，补零保持8位
                    Long num = Long.parseLong(maxStudentNo.trim());
                    num++;
                    newStudentNo = String.format("%08d", num);
                }

                // 双重校验：确保学号未被使用
                if (studentRepository.findByStudentNo(newStudentNo).isEmpty()) {
                    return newStudentNo;
                }

                retryCount++;
                logger.warn("学号 {} 已存在，第 {} 次重试", newStudentNo, retryCount);
            } catch (Exception e) {
                // 任何异常都返回默认值 20250001
                logger.error("生成学号失败，使用默认值", e);
                String defaultNo = "20250001";
                // 校验默认值是否已存在（避免极端情况）
                if (studentRepository.findByStudentNo(defaultNo).isEmpty()) {
                    return defaultNo;
                } else {
                    // 若默认值已存在，递增一次
                    return "20250002";
                }
            }
        }

        throw new RuntimeException("生成唯一学号失败，请稍后再试");
    }
}