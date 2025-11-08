package com.example.campus.controller;

import com.example.campus.dto.GradeDTO;
import com.example.campus.entity.Student;
import com.example.campus.entity.Grade;  // 正确的导入
import com.example.campus.service.GradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
public class TeacherGradeController {

    @Autowired
    private GradeService gradeService;

    @Autowired
    private com.example.campus.repository.StudentRepository studentRepository;

    @Autowired
    private com.example.campus.repository.CourseRepository courseRepository;

    // 获取教师所教课程的学生列表
    @GetMapping("/students")
    public ResponseEntity<?> getCourseStudents(@RequestParam Long courseId,
                                               @AuthenticationPrincipal UserDetails user) {
        try {
            String teacherUsername = user.getUsername();

            // 验证课程是否属于当前教师
            boolean isTeacherCourse = courseRepository.existsByIdAndTeacherUsername(courseId, teacherUsername);
            if (!isTeacherCourse) {
                return ResponseEntity.badRequest().body("无权访问该课程的学生信息");
            }

            // 获取选修该课程的学生列表
            List<Student> students = studentRepository.findByCourseId(courseId);
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取学生列表失败: " + e.getMessage());
        }
    }

    // 录入成绩
    @PostMapping("/grades")
    public ResponseEntity<?> saveGrade(@RequestBody GradeDTO gradeDTO,
                                       @AuthenticationPrincipal UserDetails user) {
        try {
            String teacherUsername = user.getUsername();

            // 验证课程是否属于当前教师
            boolean isTeacherCourse = courseRepository.existsByIdAndTeacherUsername(gradeDTO.getCourseId(), teacherUsername);
            if (!isTeacherCourse) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "无权为该课程录入成绩");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // 验证学生是否选修该课程
            boolean isStudentInCourse = studentRepository.existsByStudentIdAndCourseId(gradeDTO.getStudentId(), gradeDTO.getCourseId());
            if (!isStudentInCourse) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "该学生未选修此课程");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            Grade savedGrade = gradeService.saveGrade(gradeDTO);

            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("success", true);
            successResponse.put("message", "成绩保存成功");
            successResponse.put("data", savedGrade);
            return ResponseEntity.ok(successResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "保存成绩失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}