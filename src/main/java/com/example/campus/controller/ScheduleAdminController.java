package com.example.campus.controller;

import com.example.campus.dto.ScheduleDTO;
import com.example.campus.entity.*;
import com.example.campus.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/schedules") // 修改路径匹配前端请求
public class ScheduleAdminController {

    @Autowired private ScheduleRepository scheduleRepo;
    @Autowired private CourseRepository courseRepo;
    @Autowired private ClassroomRepository classroomRepo;

    @PostMapping
    public ResponseEntity<?> addSchedule(@RequestBody ScheduleDTO dto) {
        Course course = null;
        Classroom room = null;

        // 如果提供了ID，则使用ID查找
        if (dto.getCourseId() != null) {
            course = courseRepo.findById(dto.getCourseId()).orElse(null);
        }
        // 如果提供了名称，则按名称查找或创建新课程
        else if (dto.getCourseName() != null && !dto.getCourseName().trim().isEmpty()) {
            course = courseRepo.findByName(dto.getCourseName().trim());
            if (course == null) {
                course = new Course();
                course.setName(dto.getCourseName().trim());
                course.setCode(dto.getCourseName().trim().toUpperCase().replace(" ", "_"));
                course = courseRepo.save(course);
            }
        }

        // 教室处理同理
        if (dto.getClassroomId() != null) {
            room = classroomRepo.findById(dto.getClassroomId()).orElse(null);
        } else if (dto.getClassroomName() != null && !dto.getClassroomName().trim().isEmpty()) {
            room = classroomRepo.findByName(dto.getClassroomName().trim());
            if (room == null) {
                room = new Classroom();
                room.setName(dto.getClassroomName().trim());
                room.setLocation("未知");
                room = classroomRepo.save(room);
            }
        }

        if (course == null || room == null) {
            return ResponseEntity.badRequest().body("课程或教室不存在");
        }

        LocalTime s = LocalTime.parse(dto.getStartTime());
        LocalTime e = LocalTime.parse(dto.getEndTime());
        if (!e.isAfter(s)) {
            return ResponseEntity.badRequest().body("结束时间必须晚于开始时间");
        }

        List<Schedule> exist = scheduleRepo.findByClassroomIdAndWeekday(room.getId(), dto.getWeekday());
        for (Schedule ex : exist) {
            LocalTime exS = ex.getStartTime();
            LocalTime exE = ex.getEndTime();
            boolean overlap = !(e.isBefore(exS) || s.isAfter(exE) || e.equals(exS) || s.equals(exE));
            if (overlap) {
                return ResponseEntity.badRequest().body("时间冲突：与教室已有排课冲突 (id=" + ex.getId() + ")");
            }
        }

        Schedule sch = new Schedule();
        sch.setCourse(course);
        sch.setClassroom(room);
        sch.setTeacher(dto.getTeacher());
        sch.setWeekday(dto.getWeekday());
        sch.setStartTime(s);
        sch.setEndTime(e);
        sch.setTerm(dto.getTerm());
        scheduleRepo.save(sch);
        return ResponseEntity.ok(sch);
    }

    @GetMapping
    public List<Schedule> listAll() {
        return scheduleRepo.findAll();
    }

}
