package com.example.campus.controller;

import com.example.campus.dto.ScheduleDTO;
import com.example.campus.entity.Classroom;
import com.example.campus.entity.Course;
import com.example.campus.entity.Schedule;
import com.example.campus.entity.Teacher;
import com.example.campus.repository.ClassroomRepository;
import com.example.campus.repository.CourseRepository;
import com.example.campus.repository.ScheduleRepository;
import com.example.campus.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/schedules")
@PreAuthorize("hasAuthority('ADMIN')")
public class ScheduleAdminController {

    @Autowired private ScheduleRepository scheduleRepo;
    @Autowired private CourseRepository courseRepo;
    @Autowired private ClassroomRepository classroomRepo;
    @Autowired private TeacherRepository teacherRepo;

    // 新增排课（同步更新课程表）
    @PostMapping
    public ResponseEntity<?> addSchedule(@RequestBody ScheduleDTO dto) {
        // 1. 验证课程和教师存在
        Optional<Course> courseOpt = courseRepo.findById(dto.getCourseId());
        Optional<Teacher> teacherOpt = teacherRepo.findById(dto.getTeacherId());
        if (!courseOpt.isPresent()) {
            return ResponseEntity.badRequest().body("课程不存在");
        }
        if (!teacherOpt.isPresent()) {
            return ResponseEntity.badRequest().body("教师不存在");
        }

        Course course = courseOpt.get();
        Teacher teacher = teacherOpt.get();
        Classroom room = null;

        // 2. 处理教室（存在则查询，不存在则创建）
        if (dto.getClassroomName() != null && !dto.getClassroomName().trim().isEmpty()) {
            room = classroomRepo.findByName(dto.getClassroomName().trim());
            if (room == null) {
                room = new Classroom();
                room.setName(dto.getClassroomName().trim());
                room.setLocation("未知");
                room = classroomRepo.save(room);
            }
        } else {
            return ResponseEntity.badRequest().body("教室名称不能为空");
        }

        // 3. 验证时间合法性
        LocalTime s = LocalTime.parse(dto.getStartTime());
        LocalTime e = LocalTime.parse(dto.getEndTime());
        if (!e.isAfter(s)) {
            return ResponseEntity.badRequest().body("结束时间必须晚于开始时间");
        }

        // 4. 检查时间冲突
        List<Schedule> exist = scheduleRepo.findByClassroomIdAndWeekday(room.getId(), dto.getWeekday());
        for (Schedule ex : exist) {
            LocalTime exS = ex.getStartTime();
            LocalTime exE = ex.getEndTime();
            boolean overlap = !(e.isBefore(exS) || s.isAfter(exE) || e.equals(exS) || s.equals(exE));
            if (overlap) {
                return ResponseEntity.badRequest().body("时间冲突：与教室已有排课冲突 (id=" + ex.getId() + ")");
            }
        }

        // 5. 创建排课记录
        Schedule sch = new Schedule();
        sch.setCourse(course);
        sch.setClassroom(room);
        // 优先显示User的真实姓名，无则用用户名
        sch.setTeacher(teacher.getUser() != null ?
                (teacher.getUser().getName() != null && !teacher.getUser().getName().trim().isEmpty()
                        ? teacher.getUser().getName()
                        : teacher.getUser().getUsername())
                : "未知教师");
        sch.setTeacherUser(teacher.getUser()); // 关联教师用户
        sch.setWeekday(dto.getWeekday());
        sch.setStartTime(s);
        sch.setEndTime(e);
        sch.setTerm(dto.getTerm());
        scheduleRepo.save(sch);

        // 6. 更新课程表中的教师关联
        course.setTeacher(teacher);
        courseRepo.save(course);

        return ResponseEntity.ok(sch);
    }

    // 获取所有排课（支持搜索课程、教室、教师）
    @GetMapping
    public List<Schedule> listAll(@RequestParam(value="q", required=false) String q) {
        List<Schedule> allSchedules = scheduleRepo.findAll();
        if (q == null || q.trim().isEmpty()) {
            return allSchedules;
        }

        String keyword = q.trim().toLowerCase();
        return allSchedules.stream()
                .filter(s ->
                        (s.getCourse() != null && s.getCourse().getName().toLowerCase().contains(keyword)) ||
                                (s.getClassroom() != null && s.getClassroom().getName().toLowerCase().contains(keyword)) ||
                                (s.getTeacher() != null && s.getTeacher().toLowerCase().contains(keyword))
                )
                .collect(java.util.stream.Collectors.toList());
    }

    // 获取单个排课详情（用于编辑）
    @GetMapping("/{id}")
    public ResponseEntity<Schedule> getSchedule(@PathVariable Long id) {
        return scheduleRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 编辑排课（同步更新课程表）
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSchedule(@PathVariable Long id, @RequestBody ScheduleDTO dto) {
        // 1. 验证排课存在
        Optional<Schedule> scheduleOpt = scheduleRepo.findById(id);
        if (!scheduleOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        // 2. 验证课程和教师存在
        Optional<Course> courseOpt = courseRepo.findById(dto.getCourseId());
        Optional<Teacher> teacherOpt = teacherRepo.findById(dto.getTeacherId());
        if (!courseOpt.isPresent()) {
            return ResponseEntity.badRequest().body("课程不存在");
        }
        if (!teacherOpt.isPresent()) {
            return ResponseEntity.badRequest().body("教师不存在");
        }

        Course course = courseOpt.get();
        Teacher teacher = teacherOpt.get();
        Schedule sch = scheduleOpt.get();
        Classroom room = null;

        // 3. 处理教室
        if (dto.getClassroomName() != null && !dto.getClassroomName().trim().isEmpty()) {
            room = classroomRepo.findByName(dto.getClassroomName().trim());
            if (room == null) {
                room = new Classroom();
                room.setName(dto.getClassroomName().trim());
                room.setLocation("未知");
                room = classroomRepo.save(room);
            }
        } else {
            return ResponseEntity.badRequest().body("教室名称不能为空");
        }

        // 4. 验证时间合法性
        LocalTime s = LocalTime.parse(dto.getStartTime());
        LocalTime e = LocalTime.parse(dto.getEndTime());
        if (!e.isAfter(s)) {
            return ResponseEntity.badRequest().body("结束时间必须晚于开始时间");
        }

        // 5. 检查时间冲突（排除当前排课）
        List<Schedule> exist = scheduleRepo.findByClassroomIdAndWeekday(room.getId(), dto.getWeekday());
        for (Schedule ex : exist) {
            if (ex.getId().equals(id)) continue; // 跳过当前排课
            LocalTime exS = ex.getStartTime();
            LocalTime exE = ex.getEndTime();
            boolean overlap = !(e.isBefore(exS) || s.isAfter(exE) || e.equals(exS) || s.equals(exE));
            if (overlap) {
                return ResponseEntity.badRequest().body("时间冲突：与教室已有排课冲突 (id=" + ex.getId() + ")");
            }
        }

        // 6. 更新排课记录
        sch.setCourse(course);
        sch.setClassroom(room);
        sch.setTeacher(teacher.getUser() != null ?
                (teacher.getUser().getName() != null && !teacher.getUser().getName().trim().isEmpty()
                        ? teacher.getUser().getName()
                        : teacher.getUser().getUsername())
                : "未知教师");
        sch.setTeacherUser(teacher.getUser());
        sch.setWeekday(dto.getWeekday());
        sch.setStartTime(s);
        sch.setEndTime(e);
        sch.setTerm(dto.getTerm());
        scheduleRepo.save(sch);

        // 7. 更新课程表中的教师关联
        course.setTeacher(teacher);
        courseRepo.save(course);

        return ResponseEntity.ok(sch);
    }

    // 删除排课
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        if (!scheduleRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        scheduleRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}