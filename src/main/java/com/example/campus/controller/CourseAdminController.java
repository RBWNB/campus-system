package com.example.campus.controller;

import com.example.campus.entity.Course;
import com.example.campus.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/course-management")  // 修改路径
public class CourseAdminController {

    @Autowired
    private CourseService courseService;

    @GetMapping
    public ResponseEntity<?> getCourses(@RequestParam(required = false) String q) {
        try {
            List<Course> courses;
            if (q != null && !q.trim().isEmpty()) {
                courses = courseService.searchCourses(q);
            } else {
                courses = courseService.getAllCourses();
            }
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "获取课程列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCourseById(@PathVariable Long id) {
        try {
            Optional<Course> course = courseService.getCourseById(id);
            if (course.isPresent()) {
                return ResponseEntity.ok(course.get());
            } else {
                Map<String, String> response = new HashMap<>();
                response.put("error", "课程不存在");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "获取课程失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/codes")
    public ResponseEntity<?> getAllCourseCodes() {
        try {
            List<String> courseCodes = courseService.getAllCourseCodes();
            return ResponseEntity.ok(courseCodes);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "获取课程代码列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<?> getCourseByCode(@PathVariable String code) {
        try {
            Optional<Course> course = courseService.getCourseInfoByCode(code);
            if (course.isPresent()) {
                return ResponseEntity.ok(course.get());
            } else {
                Map<String, String> response = new HashMap<>();
                response.put("error", "课程不存在");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "获取课程失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping
    public ResponseEntity<?> createCourse(@RequestBody Course course) {
        try {
            Course savedCourse = courseService.saveCourse(course);
            return ResponseEntity.ok(savedCourse);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "创建课程失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCourse(@PathVariable Long id, @RequestBody Course course) {
        try {
            course.setId(id);
            Course updatedCourse = courseService.saveCourse(course);
            return ResponseEntity.ok(updatedCourse);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "更新课程失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id) {
        try {
            courseService.deleteCourse(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "课程删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "删除课程失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}