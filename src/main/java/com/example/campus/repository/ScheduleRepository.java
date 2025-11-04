package com.example.campus.repository;

import com.example.campus.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByClassroomIdAndWeekday(Long classroomId, Integer weekday);
    List<Schedule> findByCourseId(Long courseId);
}
