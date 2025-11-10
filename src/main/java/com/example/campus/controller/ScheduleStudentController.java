package com.example.campus.controller;

import com.example.campus.entity.Schedule;
import com.example.campus.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@PreAuthorize("hasAuthority('STUDENT')") // 权限控制：仅限学生角色
public class ScheduleStudentController {

    @Autowired
    private ScheduleService scheduleService;

    /**
     * 获取当前学生的课程表 (对应 schedule.html 中的 /api/student/schedule)
     * @param termId 学期标识符，可选
     * @return 课程表列表
     */
    @GetMapping("/schedule")
    public ResponseEntity<List<Schedule>> getStudentSchedule(
            // 接收前端传递的学期参数
            @RequestParam(value = "termId", required = false) String termId) {

        // 1. 使用 Spring Security 标准方法获取当前登录用户的用户名
        String studentUsername = getCurrentUsernameFromSecurityContext();

        // 2. 调用 Service 层方法查询课表
        List<Schedule> schedules = scheduleService.getSchedulesForStudent(studentUsername, termId);

        return ResponseEntity.ok(schedules);
    }

    /**
     * 从 Spring Security 上下文 (Context) 中获取当前登录用户的用户名
     */
    private String getCurrentUsernameFromSecurityContext() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }

        return principal.toString();
    }
}