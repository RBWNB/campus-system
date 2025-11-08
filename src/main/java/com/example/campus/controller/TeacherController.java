// TeacherController.java
package com.example.campus.controller;

import com.example.campus.entity.LeaveRequest;
import com.example.campus.entity.LeaveStatus;
import com.example.campus.repository.LeaveRepository;
import com.example.campus.entity.Schedule;
import com.example.campus.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    @Autowired
    private ScheduleRepository scheduleRepo;

    @Autowired
    private LeaveRepository leaveRepo;

    // 获取教师的课程表
    @GetMapping("/schedule")
    public List<Schedule> getTeacherSchedule(@RequestParam(defaultValue = "1") Integer weekday,
                                             @AuthenticationPrincipal UserDetails user) {
        return scheduleRepo.findByTeacherAndWeekday(user.getUsername(), weekday);
    }

    // 获取待审批的请假申请
    @GetMapping("/leaves/pending")
    public List<LeaveRequest> getPendingLeaves() {
        return leaveRepo.findByStatus(LeaveStatus.PENDING);
    }

    // 获取已审批的请假申请
    @GetMapping("/leaves/approved")
    public List<LeaveRequest> getApprovedLeaves() {
        return leaveRepo.findByStatusNot(LeaveStatus.PENDING);
    }

    // 审批请假申请
    @PostMapping("/leaves/{id}/review")
    public ResponseEntity<?> reviewLeave(@PathVariable Long id,
                                         @RequestBody LeaveReviewRequest request,
                                         @AuthenticationPrincipal UserDetails user) {
        LeaveRequest leave = leaveRepo.findById(id).orElse(null);
        if (leave == null) {
            return ResponseEntity.notFound().build();
        }

        leave.setStatus(LeaveStatus.valueOf(request.getStatus()));
        leave.setReviewedAt(Timestamp.from(Instant.now()));
        leave.setReviewer(user.getUsername());
        leave.setComment(request.getComment());

        LeaveRequest savedLeave = leaveRepo.save(leave);
        return ResponseEntity.ok(savedLeave);
    }

    // 请假审批请求DTO
    public static class LeaveReviewRequest {
        private String status;
        private String comment;

        // 构造函数
        public LeaveReviewRequest() {}

        // getter和setter方法
        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
    }
}
