package com.example.campus.repository;

import com.example.campus.entity.Course;
import com.example.campus.entity.Grade;
import com.example.campus.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findByStudent(Student student);

    // 添加新方法：根据学生、课程和成绩类型查找成绩
    Grade findByStudentAndCourseAndGradeType(Student student, Course course, String gradeType);

    // 添加新方法：根据课程查找所有成绩
    List<Grade> findByCourse(Course course);

    // 添加新方法：根据课程ID查找选修该课程的学生
    @Query("SELECT DISTINCT g.student FROM Grade g WHERE g.course.id = :courseId")
    List<Student> findStudentsByCourseId(@Param("courseId") Long courseId);
}