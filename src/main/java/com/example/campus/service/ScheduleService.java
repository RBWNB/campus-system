package com.example.campus.service;

import com.example.campus.dto.ScheduleDTO;
import com.example.campus.entity.Schedule;
import com.example.campus.entity.Course;
import com.example.campus.entity.Classroom;
import com.example.campus.entity.User;
import com.example.campus.entity.CourseSelection;
import com.example.campus.entity.Teacher;
import com.example.campus.repository.ScheduleRepository;
import com.example.campus.repository.CourseRepository;
import com.example.campus.repository.ClassroomRepository;
import com.example.campus.repository.UserRepository;
import com.example.campus.repository.CourseSelectionRepository;
import com.example.campus.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Autowired
    private CourseSelectionRepository courseSelectionRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Transactional
    public Schedule saveSchedule(ScheduleDTO scheduleDTO) {
        // 验证课程是否存在
        Course course = courseRepository.findById(scheduleDTO.getCourseId())
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        // 验证教室是否存在（使用现有精确查询方法）
        Classroom classroom = classroomRepository.findByName(scheduleDTO.getClassroomName());
        if (classroom == null) {
            throw new RuntimeException("教室不存在");
        }

        LocalTime startTime;
        LocalTime endTime;
        try {
            startTime = LocalTime.parse(scheduleDTO.getStartTime());
            endTime = LocalTime.parse(scheduleDTO.getEndTime());
        } catch (DateTimeParseException e) {
            throw new RuntimeException("时间格式不正确，请使用 HH:mm 格式");
        }

        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            throw new RuntimeException("结束时间必须晚于开始时间");
        }

        // 检查时间冲突（使用教室ID）
        if (hasTimeConflict(classroom.getId(), scheduleDTO.getWeekday(), startTime, endTime)) {
            throw new RuntimeException("该教室在该时间段已被占用");
        }

        Schedule schedule = new Schedule();
        schedule.setCourse(course);
        schedule.setClassroom(classroom);

        // 关键修改：通过teacherId查询teachers表，获取教师姓名（核心逻辑）
        if (scheduleDTO.getTeacherId() != null) {
            Optional<Teacher> teacher = teacherRepository.findById(scheduleDTO.getTeacherId());
            // 存储teachers表中的教师姓名（而非用户名）
            schedule.setTeacher(teacher.map(Teacher::getName).orElse("未知教师"));

            // 保持用户关联（兼容原有逻辑）
            Optional<User> teacherUser = userRepository.findByTeacherId(scheduleDTO.getTeacherId());
            teacherUser.ifPresent(schedule::setTeacherUser);
        } else {
            schedule.setTeacher("未知教师");
        }

        schedule.setWeekday(scheduleDTO.getWeekday());
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        schedule.setTerm(scheduleDTO.getTerm());

        return scheduleRepository.save(schedule);
    }

    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    public List<Schedule> getSchedulesByTeacher(String username) {
        return scheduleRepository.findByTeacherUser_Username(username);
    }

    @Transactional
    public Schedule updateSchedule(Long id, ScheduleDTO scheduleDTO) {
        Schedule existingSchedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("排课记录不存在"));

        Course course = courseRepository.findById(scheduleDTO.getCourseId())
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        // 验证教室（使用现有精确查询方法）
        Classroom classroom = classroomRepository.findByName(scheduleDTO.getClassroomName());
        if (classroom == null) {
            throw new RuntimeException("教室不存在");
        }

        LocalTime startTime;
        LocalTime endTime;
        try {
            startTime = LocalTime.parse(scheduleDTO.getStartTime());
            endTime = LocalTime.parse(scheduleDTO.getEndTime());
        } catch (DateTimeParseException e) {
            throw new RuntimeException("时间格式不正确，请使用 HH:mm 格式");
        }

        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            throw new RuntimeException("结束时间必须晚于开始时间");
        }

        // 检查更新时的时间冲突（排除自身）
        if (hasTimeConflictForUpdate(existingSchedule.getId(), classroom.getId(), scheduleDTO.getWeekday(), startTime, endTime)) {
            throw new RuntimeException("该教室在该时间段已被占用");
        }

        // 更新信息
        existingSchedule.setCourse(course);
        existingSchedule.setClassroom(classroom);

        // 关键修改：更新教师姓名（从teachers表查询）
        if (scheduleDTO.getTeacherId() != null) {
            Optional<Teacher> teacher = teacherRepository.findById(scheduleDTO.getTeacherId());
            existingSchedule.setTeacher(teacher.map(Teacher::getName).orElse("未知教师"));

            Optional<User> teacherUser = userRepository.findByTeacherId(scheduleDTO.getTeacherId());
            teacherUser.ifPresent(existingSchedule::setTeacherUser);
        } else {
            existingSchedule.setTeacher("未知教师");
        }

        existingSchedule.setWeekday(scheduleDTO.getWeekday());
        existingSchedule.setStartTime(startTime);
        existingSchedule.setEndTime(endTime);
        existingSchedule.setTerm(scheduleDTO.getTerm());

        return scheduleRepository.save(existingSchedule);
    }

    public void deleteSchedule(Long id) {
        scheduleRepository.deleteById(id);
    }

    public List<Schedule> getSchedulesForStudent(String studentUsername, String term) {
        List<CourseSelection> selections = courseSelectionRepository.findByStudent_User_Username(studentUsername);

        if (selections.isEmpty()) {
            return List.of();
        }

        List<Long> courseIds = selections.stream()
                .map(selection -> selection.getCourse().getId())
                .collect(Collectors.toList());

        if (term != null && !term.trim().isEmpty()) {
            return scheduleRepository.findByCourse_IdInAndTerm(courseIds, term);
        } else {
            return scheduleRepository.findByCourse_IdIn(courseIds);
        }
    }

    // 检查新增排课时的时间冲突
    private boolean hasTimeConflict(Long classroomId, Integer weekday, LocalTime startTime, LocalTime endTime) {
        List<Schedule> existingSchedules = scheduleRepository.findByClassroomIdAndWeekday(classroomId, weekday);

        for (Schedule existing : existingSchedules) {
            if (isTimeOverlap(existing.getStartTime(), existing.getEndTime(), startTime, endTime)) {
                return true;
            }
        }
        return false;
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

    // 判断时间段是否重叠（包含边界情况）
    private boolean isTimeOverlap(LocalTime existingStart, LocalTime existingEnd, LocalTime newStart, LocalTime newEnd) {
        return newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart);
    }
}