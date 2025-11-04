package com.example.campus.controller;

import com.example.campus.entity.Course;
import com.example.campus.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
    @Autowired
    private CourseRepository courseRepository;

    @GetMapping
    public List<Course> list(@RequestParam(value="q", required=false) String q) {
        if (q == null || q.trim().isEmpty()) {
            return courseRepository.findAll();
        }
        String keyword = q.trim().toLowerCase();
        return courseRepository.findAll().stream()
                .filter(c -> (c.getName()!=null && c.getName().toLowerCase().contains(keyword)) ||
                             (c.getCode()!=null && c.getCode().toLowerCase().contains(keyword)))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> get(@PathVariable Long id) {
        return courseRepository.findById(id)
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
