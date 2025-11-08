package com.example.campus.service;

import com.example.campus.dto.ScheduleDTO;
import com.example.campus.entity.Schedule;
import com.example.campus.entity.Course;
import com.example.campus.entity.Classroom;
import com.example.campus.entity.User;
import com.example.campus.repository.ScheduleRepository;
import com.example.campus.repository.CourseRepository;
import com.example.campus.repository.ClassroomRepository;
import com.example.campus.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Schedule saveSchedule(ScheduleDTO scheduleDTO) {
        // 验证课程是否存在
        Course course = courseRepository.findById(scheduleDTO.getCourseId())
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        // 验证教室是否存在
        Classroom classroom = classroomRepository.findById(scheduleDTO.getClassroomId())
                .orElseThrow(() -> new RuntimeException("教室不存在"));

        // 转换时间格式
        LocalTime startTime = LocalTime.parse(scheduleDTO.getStartTime());
        LocalTime endTime = LocalTime.parse(scheduleDTO.getEndTime());

        // 检查时间冲突
        if (hasTimeConflict(scheduleDTO.getClassroomId(), scheduleDTO.getWeekday(), startTime, endTime)) {
            throw new RuntimeException("该教室在该时间段已被占用");
        }

        // 创建排课记录
        Schedule schedule = new Schedule();
        schedule.setCourse(course);
        schedule.setClassroom(classroom);
        schedule.setTeacher(scheduleDTO.getTeacher());
        schedule.setWeekday(scheduleDTO.getWeekday());
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        schedule.setTerm(scheduleDTO.getTerm());

        // 根据教师用户名查找教师用户
        if (scheduleDTO.getTeacher() != null && !scheduleDTO.getTeacher().trim().isEmpty()) {
            Optional<User> teacherUser = userRepository.findByUsername(scheduleDTO.getTeacher());
            teacherUser.ifPresent(schedule::setTeacherUser);
        }

        return scheduleRepository.save(schedule);
    }

    // 检查时间冲突
    private boolean hasTimeConflict(Long classroomId, Integer weekday, LocalTime startTime, LocalTime endTime) {
        List<Schedule> existingSchedules = scheduleRepository.findByClassroomIdAndWeekday(classroomId, weekday);

        for (Schedule existing : existingSchedules) {
            if (isTimeOverlap(existing.getStartTime(), existing.getEndTime(), startTime, endTime)) {
                return true;
            }
        }
        return false;
    }

    // 检查时间是否重叠
    private boolean isTimeOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    // 获取教师排课
    public List<Schedule> getSchedulesByTeacher(String teacherUsername) {
        return scheduleRepository.findByTeacherUser_Username(teacherUsername);
    }

    // 获取课程排课
    public List<Schedule> getSchedulesByCourse(Long courseId) {
        return scheduleRepository.findByCourseId(courseId);
    }

    // 获取教室在某天的排课
    public List<Schedule> getSchedulesByClassroomAndWeekday(Long classroomId, Integer weekday) {
        return scheduleRepository.findByClassroomIdAndWeekday(classroomId, weekday);
    }

    // 删除排课
    @Transactional
    public void deleteSchedule(Long scheduleId) {
        if (!scheduleRepository.existsById(scheduleId)) {
            throw new RuntimeException("排课记录不存在");
        }
        scheduleRepository.deleteById(scheduleId);
    }

    // 更新排课
    @Transactional
    public Schedule updateSchedule(Long scheduleId, ScheduleDTO scheduleDTO) {
        Schedule existingSchedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("排课记录不存在"));

        // 验证课程
        Course course = courseRepository.findById(scheduleDTO.getCourseId())
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        // 验证教室
        Classroom classroom = classroomRepository.findById(scheduleDTO.getClassroomId())
                .orElseThrow(() -> new RuntimeException("教室不存在"));

        // 转换时间
        LocalTime startTime = LocalTime.parse(scheduleDTO.getStartTime());
        LocalTime endTime = LocalTime.parse(scheduleDTO.getEndTime());

        // 检查时间冲突（排除自身）
        if (hasTimeConflictForUpdate(scheduleId, scheduleDTO.getClassroomId(), scheduleDTO.getWeekday(), startTime, endTime)) {
            throw new RuntimeException("该教室在该时间段已被占用");
        }

        // 更新信息
        existingSchedule.setCourse(course);
        existingSchedule.setClassroom(classroom);
        existingSchedule.setTeacher(scheduleDTO.getTeacher());
        existingSchedule.setWeekday(scheduleDTO.getWeekday());
        existingSchedule.setStartTime(startTime);
        existingSchedule.setEndTime(endTime);
        existingSchedule.setTerm(scheduleDTO.getTerm());

        // 更新教师用户
        if (scheduleDTO.getTeacher() != null && !scheduleDTO.getTeacher().trim().isEmpty()) {
            Optional<User> teacherUser = userRepository.findByUsername(scheduleDTO.getTeacher());
            teacherUser.ifPresent(existingSchedule::setTeacherUser);
        }

        return scheduleRepository.save(existingSchedule);
    }

    // 检查更新时的时间冲突（排除自身）
    private boolean hasTimeConflictForUpdate(Long scheduleId, Long classroomId, Integer weekday, LocalTime startTime, LocalTime endTime) {
        List<Schedule> existingSchedules = scheduleRepository.findByClassroomIdAndWeekday(classroomId, weekday);

        for (Schedule existing : existingSchedules) {
            if (!existing.getId().equals(scheduleId) &&
                    isTimeOverlap(existing.getStartTime(), existing.getEndTime(), startTime, endTime)) {
                return true;
            }
        }
        return false;
    }
}