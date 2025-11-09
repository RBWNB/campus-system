// AdminController.java
package com.example.campus.controller;

import com.example.campus.entity.*;
import com.example.campus.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired private UserRepository userRepo;
    @Autowired private CourseRepository courseRepo;
    @Autowired private ClassroomRepository classroomRepo;
    @Autowired private ScheduleRepository scheduleRepo;
    @Autowired private StudentRepository studentRepo;
    @Autowired private PasswordEncoder passwordEncoder;

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
    public List<User> listUsers() {
        return userRepo.findAll();
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        // 使用 findById 查找用户，如果找到则返回 200 OK，否则返回 404 Not Found
        return userRepo.findById(id)
                .map(ResponseEntity::ok)
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

            // 3. 教师专属信息处理 (如果角色是教师)
            if (existingUser.getRole() == Role.TEACHER && u.getTeacher() != null) {
                // 这里假设您有一个 TeacherService 或类似的逻辑来处理关联的 Teacher 实体
                // 如果 User 实体直接关联 Teacher 实体，您可能需要加载/更新该实体
                // 简单示例（需要根据您的实际Teacher实体逻辑调整）：
                // existingUser.setTeacher(u.getTeacher());
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
    public ResponseEntity<Void> deleteClassroom(@PathVariable Long id) {
        if (!classroomRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        classroomRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
