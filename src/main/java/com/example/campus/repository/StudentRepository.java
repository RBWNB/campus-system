package com.example.campus.repository;

import com.example.campus.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentNo(String studentNo);
    Optional<Student> findByUserId(Long userId);

    // 查询选修某课程的学生 (改为关联 CourseSelection 表)
    @Query("SELECT s FROM Student s JOIN CourseSelection cs ON s.id = cs.student.id WHERE cs.course.id = :courseId")
    List<Student> findByCourseId(@Param("courseId") Long courseId);

    // 检查学生是否选修某课程 (改为关联 CourseSelection 表)
    @Query("SELECT COUNT(cs) > 0 FROM CourseSelection cs WHERE cs.student.id = :studentId AND cs.course.id = :courseId")
    boolean existsByStudentIdAndCourseId(@Param("studentId") Long studentId,
                                         @Param("courseId") Long courseId);
    Optional<Student> findByUserUsername(String username);
}