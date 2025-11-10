package com.example.campus.controller;

import com.example.campus.dto.CourseSelectionDto;
import com.example.campus.service.CourseSelectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
// 确保只有学生能访问这些接口
@PreAuthorize("hasAuthority('STUDENT')")
public class CourseSelectionController {

    @Autowired
    private CourseSelectionService selectionService;

    /**
     * 1. GET /api/courses/selection-list
     * 获取所有可选课程列表，包含学生的选课状态。
     */
    @GetMapping("/courses/selection-list")
    public List<CourseSelectionDto> getCoursesWithSelectionStatus(
            Principal principal,
            @RequestParam(required = false) String q) {

        String username = principal.getName();
        return selectionService.getSelectionList(username, q);
    }

    /**
     * 2. POST /api/selection/{courseId}
     * 执行选课操作。
     */
    @PostMapping("/selection/{courseId}")
    public ResponseEntity<?> selectCourse(
            Principal principal,
            @PathVariable Long courseId) {

        try {
            selectionService.selectCourse(principal.getName(), courseId);
            return ResponseEntity.ok(Map.of("message", "选课成功"));
        } catch (IllegalStateException e) {
            // 如果 Service 抛出 "已选该课程" 或 "课程已满" 异常，返回 409 Conflict
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            // 捕获其他运行时异常（如课程/学生不存在）
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * 3. DELETE /api/selection/{courseId}
     * 执行退课操作。
     */
    @DeleteMapping("/selection/{courseId}")
    public ResponseEntity<?> dropCourse(
            Principal principal,
            @PathVariable Long courseId) {

        try {
            selectionService.dropCourse(principal.getName(), courseId);
            return ResponseEntity.ok(Map.of("message", "退课成功"));
        } catch (IllegalStateException e) {
            // 捕获 "未选该课程" 异常，返回 400 Bad Request
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
}