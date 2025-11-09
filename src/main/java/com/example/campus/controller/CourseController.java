package com.example.campus.controller;

import com.example.campus.entity.Course;
import com.example.campus.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
    @Autowired
    private CourseRepository courseRepository;

    // 教师端：获取当前教师的课程
    @GetMapping
    public List<Course> list(@RequestParam(value="q", required=false) String q,
                             @AuthenticationPrincipal UserDetails user) {
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

    // 管理员端：获取所有课程（供排课选择）
    @GetMapping("/admin/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<Course>> getAllCourses() {
        List<Course> courses = courseRepository.findAll();
        return ResponseEntity.ok(courses);
    }

    // 获取单个课程详情
    @GetMapping("/{id}")
    public ResponseEntity<Course> get(@PathVariable Long id,
                                      @AuthenticationPrincipal UserDetails user) {
        String teacherUsername = user.getUsername();
        return courseRepository.findByIdAndTeacherUsername(id, teacherUsername)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 管理员端：更新课程信息（排课关联更新）
    @PatchMapping("/admin/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id,
                                               @RequestBody Map<String, Object> updateData) {
        Optional<Course> courseOpt = courseRepository.findById(id);
        if (!courseOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Course course = courseOpt.get();
        // 更新排课关联信息（根据前端传递的字段动态更新）
        if (updateData.containsKey("teacherId")) {
            // 假设Teacher实体已存在，这里简化处理，实际应通过TeacherRepository查询
            // Teacher teacher = teacherRepository.findById(Long.parseLong(updateData.get("teacherId").toString())).orElse(null);
            // course.setTeacher(teacher);
        }
        // 可根据需要添加更多字段更新逻辑

        Course updatedCourse = courseRepository.save(course);
        return ResponseEntity.ok(updatedCourse);
    }

    // 管理员端：删除课程
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        if (!courseRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        courseRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}