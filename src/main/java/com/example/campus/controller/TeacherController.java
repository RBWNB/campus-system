package com.example.campus.controller;

import com.example.campus.entity.Schedule;
import com.example.campus.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    @Autowired
    private ScheduleRepository scheduleRepo;

    // 获取教师的课程表
    @GetMapping("/schedule")
    public List<Schedule> getTeacherSchedule(@RequestParam(defaultValue = "1") Integer weekday,
                                             @AuthenticationPrincipal UserDetails user) {
        // 根据登录用户获取教师信息
        // 这里需要根据实际的用户-教师关联关系来实现
        return scheduleRepo.findByTeacherAndWeekday(user.getUsername(), weekday);
    }
}
