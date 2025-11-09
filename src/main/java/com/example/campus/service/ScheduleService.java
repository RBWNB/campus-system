package com.example.campus.service;

import com.example.campus.dto.ScheduleDTO;
import com.example.campus.entity.Schedule;
import com.example.campus.entity.Course;
import com.example.campus.entity.Classroom;
import com.example.campus.entity.User;
// 新增导入
import com.example.campus.entity.CourseSelection;
import com.example.campus.repository.ScheduleRepository;
import com.example.campus.repository.CourseRepository;
import com.example.campus.repository.ClassroomRepository;
import com.example.campus.repository.UserRepository;
// 新增导入
import com.example.campus.repository.CourseSelectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // 新增导入

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

    // **********************************************
    // ************ 新增依赖注入 ********************
    // **********************************************
    @Autowired
    private CourseSelectionRepository courseSelectionRepository;

    @Transactional
    public Schedule saveSchedule(ScheduleDTO scheduleDTO) {
        // 验证课程是否存在
        Course course = courseRepository.findById(scheduleDTO.getCourseId())
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        // 验证教室是否存在
        Classroom classroom = classroomRepository.findById(scheduleDTO.getClassroomId())
                .orElseThrow(() -> new RuntimeException("教室不存在"));

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

        // 检查时间冲突
        if (hasTimeConflict(scheduleDTO.getClassroomId(), scheduleDTO.getWeekday(), startTime, endTime)) {
            throw new RuntimeException("该教室在该时间段已被占用");
        }

        Schedule schedule = new Schedule();
        schedule.setCourse(course);
        schedule.setClassroom(classroom);
        schedule.setTeacher(scheduleDTO.getTeacher());
        schedule.setWeekday(scheduleDTO.getWeekday());
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        schedule.setTerm(scheduleDTO.getTerm());

        // 设置教师用户关联
        if (scheduleDTO.getTeacher() != null && !scheduleDTO.getTeacher().trim().isEmpty()) {
            Optional<User> teacherUser = userRepository.findByUsername(scheduleDTO.getTeacher());
            teacherUser.ifPresent(schedule::setTeacherUser);
        }

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

        Classroom classroom = classroomRepository.findById(scheduleDTO.getClassroomId())
                .orElseThrow(() -> new RuntimeException("教室不存在"));

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
        if (hasTimeConflictForUpdate(existingSchedule.getId(), scheduleDTO.getClassroomId(), scheduleDTO.getWeekday(), startTime, endTime)) {
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

    public void deleteSchedule(Long id) {
        scheduleRepository.deleteById(id);
    }

    // **********************************************
    // ************ 新增学生课表查询方法 ************
    // **********************************************
    /**
     * 获取指定学生用户名对应的所有排课记录，并可根据学期过滤。
     * 逻辑流程: 通过用户名直接获取 CourseSelection -> 提取 Course ID -> 查询 Schedule
     * @param studentUsername 学生用户名
     * @param term 学期标识符 (可选)
     * @return 课程表列表
     */
    public List<Schedule> getSchedulesForStudent(String studentUsername, String term) {
        // 1. 根据用户名直接查找学生已选的所有课程记录
        List<CourseSelection> selections = courseSelectionRepository.findByStudent_User_Username(studentUsername);

        if (selections.isEmpty()) {
            // 该学生没有选课，直接返回空列表
            return List.of();
        }

        // 2. 提取所有已选课程的 Course ID
        // 假设 CourseSelection 实体中关联 Course 的字段名为 course
        List<Long> courseIds = selections.stream()
                .map(selection -> selection.getCourse().getId())
                .collect(Collectors.toList());

        // 3. 根据 Course ID 列表和学期查询 Schedule
        if (term != null && !term.trim().isEmpty()) {
            // 需要 ScheduleRepository.findByCourse_IdInAndTerm(List<Long> courseIds, String term)
            return scheduleRepository.findByCourse_IdInAndTerm(courseIds, term);
        } else {
            // 需要 ScheduleRepository.findByCourse_IdIn(List<Long> courseIds)
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
        // [newStart, newEnd) 与 [existingStart, existingEnd) 重叠
        // (newStart < existingEnd) AND (newEnd > existingStart)
        return newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart);
    }
}