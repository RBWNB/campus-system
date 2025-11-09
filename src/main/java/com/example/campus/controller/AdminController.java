// AdminController.java
package com.example.campus.controller;

import com.example.campus.entity.Classroom;
import com.example.campus.entity.Course;
import com.example.campus.entity.User;
import com.example.campus.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
