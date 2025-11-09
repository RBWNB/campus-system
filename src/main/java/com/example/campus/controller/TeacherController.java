package com.example.campus.controller;

import com.example.campus.entity.LeaveRequest;
import com.example.campus.entity.LeaveStatus;
import com.example.campus.entity.Teacher;
import com.example.campus.repository.LeaveRepository;
import com.example.campus.repository.ScheduleRepository;
import com.example.campus.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

// 自定义DTO：用于返回教师用户的核心信息（避免直接返回实体暴露过多字段）
class TeacherUserDTO {
    private Long userId; // User 实体的 ID
    private String name; // User 实体的姓名
    private String username; // User 实体的用户名

    // 构造方法：通过 Teacher 实体和关联的 User 组装DTO
    public TeacherUserDTO(Teacher teacher) {
        this.userId = teacher.getUser().getId();
        this.name = teacher.getUser().getName(); // 假设 User 有 name 字段（真实姓名）
        this.username = teacher.getUser().getUsername(); // 用户名
    }

    // getter
    public Long getUserId() { return userId; }
    public String getName() { return name; }
    public String getUsername() { return username; }
}

@RestController
@RequestMapping("/api")
public class TeacherController {

    @Autowired
    private ScheduleRepository scheduleRepo;

    @Autowired
    private LeaveRepository leaveRepo;

    @Autowired
    private TeacherRepository teacherRepository;

    // 原有接口不变...
    @GetMapping("/teacher/schedule")
    public List<com.example.campus.entity.Schedule> getTeacherSchedule(@RequestParam(defaultValue = "1") Integer weekday,
                                                                       @AuthenticationPrincipal UserDetails user) {
        return scheduleRepo.findByTeacherAndWeekday(user.getUsername(), weekday);
    }

    @GetMapping("/teacher/leaves/pending")
    public List<LeaveRequest> getPendingLeaves() {
        return leaveRepo.findByStatus(LeaveStatus.PENDING);
    }

    @GetMapping("/teacher/leaves/approved")
    public List<LeaveRequest> getApprovedLeaves() {
        return leaveRepo.findByStatusNot(LeaveStatus.PENDING);
    }

    @PostMapping("/teacher/leaves/{id}/review")
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

    // 核心修改：不依赖 roles 字段，通过 Teacher 关联的 User 获取教师信息
    @GetMapping("/admin/all-teachers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TeacherUserDTO>> getAllTeachersForAdmin() {
        // 1. 查询所有 Teacher 实体（每个 Teacher 都关联了一个 User）
        List<Teacher> allTeachers = teacherRepository.findAll();

        // 2. 转换为 TeacherUserDTO（仅保留 User 的核心信息：id、姓名、用户名）
        List<TeacherUserDTO> teacherUsers = allTeachers.stream()
                .filter(teacher -> teacher.getUser() != null) // 过滤没有关联 User 的无效教师
                .map(TeacherUserDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(teacherUsers);
    }

    // 请假审批请求DTO（原有不变）
    public static class LeaveReviewRequest {
        private String status;
        private String comment;

        public LeaveReviewRequest() {}

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
    }
}