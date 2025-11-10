package com.example.campus.controller;

import com.example.campus.entity.Classroom;
import com.example.campus.repository.ClassroomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class ClassroomController {

    @Autowired
    private ClassroomRepository classroomRepository;

    // 管理员获取所有教室列表（供排课下拉选择）
    @GetMapping("/all-classrooms")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Classroom>> getAllClassrooms() {
        List<Classroom> classrooms = classroomRepository.findAll();
        return ResponseEntity.ok(classrooms);
    }
}