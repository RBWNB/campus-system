package com.example.campus.controller;

import com.example.campus.entity.Classroom;
import com.example.campus.entity.Course;
import com.example.campus.entity.User;
import com.example.campus.entity.Role; // <-- 新增：导入 Role 类
import com.example.campus.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired private UserRepository userRepo;
    @Autowired private CourseRepository courseRepo;
    @Autowired private ClassroomRepository classroomRepo;
    @Autowired private ScheduleRepository scheduleRepo;
    @Autowired private StudentRepository studentRepo;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UserRepository userRepository;

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody User u) {
        if (userRepo.findByUsername(u.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("用户名已存在");
        }
        u.setPassword(passwordEncoder.encode(u.getPassword()));
        u.setCreatedAt(Timestamp.from(Instant.now()));
        userRepo.save(u);
        return ResponseEntity.ok(u);
    }


    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "role", required = false) String roleString // 已修改：接收 role 字符串
    ) {
        List<User> users;
        String trimmedQuery = (query != null) ? query.trim() : null;
        String trimmedRoleString = (roleString != null) ? roleString.trim() : null;

        // --- 核心改动：将字符串角色转换为 Role 枚举 ---
        Role roleEnum = null;
        if (trimmedRoleString != null && !trimmedRoleString.isEmpty()) {
            try {
                // 将传入的字符串（例如 "ADMIN"）转换为 Role.ADMIN 枚举对象
                roleEnum = Role.valueOf(trimmedRoleString.toUpperCase());
            } catch (IllegalArgumentException e) {
                // 如果传入了无效的角色字符串，返回错误
                return ResponseEntity.badRequest().body("无效的角色参数: " + trimmedRoleString);
            }
        }
        // ---------------------------------------------

        // 逻辑判断：
        // 1. 存在角色筛选
        if (roleEnum != null) {
            // 1a. 角色筛选 + 关键词搜索
            if (trimmedQuery != null && !trimmedQuery.isEmpty()) {
                // 传入 Role 枚举
                users = userRepository.searchByKeywordAndRole(trimmedQuery, roleEnum);
            } else {
                // 1b. 仅角色筛选
                // 传入 Role 枚举
                users = userRepository.findByRole(roleEnum);
            }
            // 2. 不存在角色筛选，但存在关键词搜索
        } else if (trimmedQuery != null && !trimmedQuery.isEmpty()) {
            users = userRepository.searchByKeyword(trimmedQuery);
            // 3. 既无筛选也无搜索
        } else {
            users = userRepository.findAll();
        }

        // ⚙️ 构造 JSON（避免 @JsonIgnore 影响）
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (User user : users) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("name", user.getName());
            map.put("email", user.getEmail());
            map.put("role", user.getRole());
            map.put("createdAt", user.getCreatedAt());

            if (user.getStudent() != null) {
                Map<String, Object> studentMap = new HashMap<>();
                studentMap.put("studentNo", user.getStudent().getStudentNo());
                map.put("student", studentMap);
            }

            if (user.getTeacher() != null) {
                Map<String, Object> teacherMap = new HashMap<>();
                teacherMap.put("teacherNo", user.getTeacher().getTeacherNo());
                teacherMap.put("title", user.getTeacher().getTitle());
                teacherMap.put("department", user.getTeacher().getDepartment());
                map.put("teacher", teacherMap);
            }

            resultList.add(map);
        }

        return ResponseEntity.ok(resultList);
    }

    public List<User> listUsers() {
        return userRepo.findAll();
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("id", user.getId());
                    result.put("username", user.getUsername());
                    result.put("name", user.getName());
                    result.put("email", user.getEmail());
                    result.put("role", user.getRole());
                    result.put("createdAt", user.getCreatedAt());

                    // 手动添加学生信息（即使 @JsonIgnore 也能访问）
                    if (user.getStudent() != null) {
                        Map<String, Object> studentMap = new HashMap<>();
                        studentMap.put("studentNo", user.getStudent().getStudentNo());
                        result.put("student", studentMap);
                    }

                    // 你也可以同理加入教师信息（非必须）
                    if (user.getTeacher() != null) {
                        Map<String, Object> teacherMap = new HashMap<>();
                        teacherMap.put("teacherNo", user.getTeacher().getTeacherNo());
                        teacherMap.put("title", user.getTeacher().getTitle());
                        teacherMap.put("department", user.getTeacher().getDepartment());
                        result.put("teacher", teacherMap);
                    }

                    return ResponseEntity.ok(result);
                })
                .orElse(ResponseEntity.notFound().build());
    }


    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/courses")
    public Course createCourse(@RequestBody Course c) {
        c.setCreatedAt(Timestamp.from(Instant.now()));
        return courseRepo.save(c);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User u) {
        return userRepo.findById(id).map(existingUser -> {
            // 1. 更新基本信息 (确保不更新密码，除非明确提供)
            existingUser.setUsername(u.getUsername());
            existingUser.setName(u.getName());
            existingUser.setEmail(u.getEmail());
            existingUser.setRole(u.getRole()); // 角色可以修改

            // 2. 密码更新（如果前端提供了密码）
            if (u.getPassword() != null && !u.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(u.getPassword()));
            }

            userRepo.save(existingUser);
            return ResponseEntity.ok("用户信息更新成功"); // 返回成功消息
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/courses/{id}")
    public ResponseEntity<?> updateCourse(@PathVariable Long id, @RequestBody Course c) {
        return courseRepo.findById(id).map(ex -> {
            ex.setCode(c.getCode());
            ex.setName(c.getName());
            ex.setCredit(c.getCredit());
            ex.setDescription(c.getDescription());
            courseRepo.save(ex);
            return ResponseEntity.ok(ex);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/classrooms")
    public Classroom createClassroom(@RequestBody Classroom r) {
        return classroomRepo.save(r);
    }

    @GetMapping("/classrooms")
    public ResponseEntity<List<Classroom>> getClassrooms(
            @RequestParam(required = false) String q) {
        List<Classroom> classrooms;

        if (q != null && !q.isEmpty()) {
            classrooms = classroomRepo.findByNameContainingIgnoreCaseOrLocationContainingIgnoreCase(q, q);
        } else {
            classrooms = classroomRepo.findAll();
        }

        return ResponseEntity.ok(classrooms);
    }

    @PutMapping("/classrooms/{id}")
    public ResponseEntity<Classroom> updateClassroom(@PathVariable Long id, @RequestBody Classroom classroom) {
        if (!classroomRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        classroom.setId(id);
        Classroom updated = classroomRepo.save(classroom);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/classrooms/{id}")
    public ResponseEntity<String> deleteClassroom(@PathVariable Long id) {
        if (!classroomRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            classroomRepo.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            // 捕获外键约束异常（被排课引用时触发）
            return ResponseEntity.badRequest().body("删除失败：该教室已被排课记录引用，请先删除相关排课");
        } catch (Exception e) {
            // 处理其他可能的异常
            return ResponseEntity.badRequest().body("删除失败：发生未知错误");
        }
    }
}