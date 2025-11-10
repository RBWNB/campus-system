package com.example.campus.controller;

import com.example.campus.entity.*;
import com.example.campus.repository.GradeRepository;
import com.example.campus.repository.LeaveRepository;
import com.example.campus.repository.StudentRepository;
import com.example.campus.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired private UserRepository userRepo;
    @Autowired private StudentRepository studentRepo;
    @Autowired private GradeRepository gradeRepo;
    @Autowired private LeaveRepository leaveRepo;

    @GetMapping("/profile")
    public ResponseEntity<?> profile(@AuthenticationPrincipal UserDetails ud) {
        if (ud == null) return ResponseEntity.status(401).body("未登录");
        User u = userRepo.findByUsername(ud.getUsername()).orElse(null);
        if (u == null) return ResponseEntity.notFound().build();
        Student s = studentRepo.findByUserId(u.getId()).orElse(null);
        return ResponseEntity.ok(s != null ? s : u);
    }

    @PostMapping("/info")
    public ResponseEntity<?> updateInfo(@AuthenticationPrincipal UserDetails ud, @RequestBody Student info) {
        if (ud == null) return ResponseEntity.status(401).body("未登录");
        User u = userRepo.findByUsername(ud.getUsername()).orElse(null);
        if (u == null) return ResponseEntity.notFound().build();

        // 更新用户信息（姓名和邮箱）
        if (info.getUser() != null) {
            if (info.getUser().getName() != null && !info.getUser().getName().trim().isEmpty()) {
                u.setName(info.getUser().getName());
            }
            if (info.getUser().getEmail() != null && !info.getUser().getEmail().trim().isEmpty()) {
                u.setEmail(info.getUser().getEmail());
            }
            userRepo.save(u);
        }

        Student s = studentRepo.findByUserId(u.getId()).orElseThrow(() -> new RuntimeException("学生信息不存在"));
        s.setUser(u);
        s.setMajor(info.getMajor());
        s.setGrade(info.getGrade());
        s.setPhone(info.getPhone());
        s.setAddress(info.getAddress());
        studentRepo.save(s);

        return ResponseEntity.ok(s);
    }

    @GetMapping("/grades")
    public ResponseEntity<?> myGrades(@AuthenticationPrincipal UserDetails ud) {
        if (ud == null) return ResponseEntity.status(401).body("未登录");
        User u = userRepo.findByUsername(ud.getUsername()).orElse(null);
        if (u == null) return ResponseEntity.notFound().build();
        Student s = studentRepo.findByUserId(u.getId()).orElse(null);
        if (s == null) return ResponseEntity.badRequest().body("学生信息未录入");
        List<Grade> grades = gradeRepo.findByStudent(s);
        return ResponseEntity.ok(grades);
    }

    @PostMapping("/leaves")
    public ResponseEntity<?> applyLeave(@AuthenticationPrincipal UserDetails ud, @RequestBody LeaveRequest req) {
        if (ud == null) return ResponseEntity.status(401).body("未登录");
        User u = userRepo.findByUsername(ud.getUsername()).orElse(null);
        Student s = studentRepo.findByUserId(u.getId()).orElse(null);
        if (s == null) return ResponseEntity.badRequest().body("学生信息未录入");

        req.setStudent(s);
        req.setStatus(LeaveStatus.PENDING);
        req.setAppliedAt(Timestamp.from(Instant.now()));
        leaveRepo.save(req);
        return ResponseEntity.ok(req);
    }

    @GetMapping("/leaves")
    public ResponseEntity<?> myLeaves(@AuthenticationPrincipal UserDetails ud) {
        if (ud == null) return ResponseEntity.status(401).body("未登录");
        User u = userRepo.findByUsername(ud.getUsername()).orElse(null);
        Student s = studentRepo.findByUserId(u.getId()).orElse(null);
        if (s == null) return ResponseEntity.badRequest().body("学生信息未录入");
        return ResponseEntity.ok(leaveRepo.findByStudent_Id(s.getId()));
    }
}