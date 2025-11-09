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

    @Query("SELECT s FROM Student s JOIN CourseSelection cs ON s.id = cs.student.id WHERE cs.course.id = :courseId")
    List<Student> findByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT COUNT(cs) > 0 FROM CourseSelection cs WHERE cs.student.id = :studentId AND cs.course.id = :courseId")
    boolean existsByStudentIdAndCourseId(@Param("studentId") Long studentId,
                                         @Param("courseId") Long courseId);
    Optional<Student> findByUserUsername(String username);

    // 原有JPA查询（保留，用于兼容）
    @Query("SELECT COALESCE(MAX(CAST(s.studentNo AS long)), '') FROM Student s")
    String findMaxStudentNo();

    // 新增：原生SQL查询最大学号（解决字符串排序问题）
    @Query(value = "SELECT COALESCE(MAX(CAST(student_no AS UNSIGNED)), '') FROM students", nativeQuery = true)
    String findMaxStudentNoNative();
}