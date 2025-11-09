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

// 自定义DTO：返回 Teachers 表的 teacherId 和 User 的姓名/用户名
class TeacherUserDTO {
    private Long teacherId; // Teachers 表的主键ID（前端需要用这个关联排课）
    private Long userId;    // User 实体的 ID
    private String name;    // User 实体的真实姓名
    private String username;// User 实体的用户名

    // 构造方法：赋值所有字段
    public TeacherUserDTO(Teacher teacher) {
        this.teacherId = teacher.getId(); // Teachers 表主键ID
        this.userId = teacher.getUser().getId();
        this.name = teacher.getUser().getName(); // 优先显示真实姓名
        this.username = teacher.getUser().getUsername(); // 用户名备用
    }

    // getter方法（前端需要通过这些方法获取字段值）
    public Long getTeacherId() { return teacherId; }
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

    // 原有接口不变：教师查询自己的排课
    @GetMapping("/teacher/schedule")
    public List<com.example.campus.entity.Schedule> getTeacherSchedule(@RequestParam(defaultValue = "1") Integer weekday,
                                                                       @AuthenticationPrincipal UserDetails user) {
        return scheduleRepo.findByTeacherAndWeekday(user.getUsername(), weekday);
    }

    // 原有接口不变：查询待审批请假
    @GetMapping("/teacher/leaves/pending")
    public List<LeaveRequest> getPendingLeaves() {
        return leaveRepo.findByStatus(LeaveStatus.PENDING);
    }

    // 原有接口不变：查询已审批请假
    @GetMapping("/teacher/leaves/approved")
    public List<LeaveRequest> getApprovedLeaves() {
        return leaveRepo.findByStatusNot(LeaveStatus.PENDING);
    }

    // 原有接口不变：审批请假
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

    // 核心接口：管理员获取所有教师列表（供排课使用）
    @GetMapping("/admin/all-teachers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TeacherUserDTO>> getAllTeachersForAdmin() {
        // 查询所有关联了User的有效教师
        List<Teacher> allTeachers = teacherRepository.findAll();
        List<TeacherUserDTO> teacherUsers = allTeachers.stream()
                .filter(teacher -> teacher.getUser() != null) // 过滤无效数据
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