package com.example.campus.controller;

import com.example.campus.entity.Course;
import com.example.campus.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
    @Autowired
    private CourseRepository courseRepository;

    @GetMapping
    public List<Course> list(@RequestParam(value="q", required=false) String q,
                             @AuthenticationPrincipal UserDetails user) {
        // 获取当前登录教师用户名
        String teacherUsername = user.getUsername();

        List<Course> teacherCourses = courseRepository.findByTeacherUsername(teacherUsername);

        if (q == null || q.trim().isEmpty()) {
            return teacherCourses;
        }

        String keyword = q.trim().toLowerCase();
        return teacherCourses.stream()
                .filter(c -> (c.getName()!=null && c.getName().toLowerCase().contains(keyword)) ||
                        (c.getCode()!=null && c.getCode().toLowerCase().contains(keyword)))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> get(@PathVariable Long id,
                                      @AuthenticationPrincipal UserDetails user) {
        String teacherUsername = user.getUsername();
        return courseRepository.findByIdAndTeacherUsername(id, teacherUsername)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}