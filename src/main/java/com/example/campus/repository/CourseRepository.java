package com.example.campus.repository;

import com.example.campus.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCode(String code);
    Course findByName(String name);

    // 查询教师所教的课程
    @Query("SELECT c FROM Course c WHERE c.teacher.user.username = :teacherUsername")
    List<Course> findByTeacherUsername(@Param("teacherUsername") String teacherUsername);

    // 根据ID和教师用户名查询课程
    @Query("SELECT c FROM Course c WHERE c.id = :courseId AND c.teacher.user.username = :teacherUsername")
    Optional<Course> findByIdAndTeacherUsername(@Param("courseId") Long courseId,
                                                @Param("teacherUsername") String teacherUsername);

    // 检查课程是否属于指定教师
    @Query("SELECT COUNT(c) > 0 FROM Course c WHERE c.id = :courseId AND c.teacher.user.username = :teacherUsername")
    boolean existsByIdAndTeacherUsername(@Param("courseId") Long courseId,
                                         @Param("teacherUsername") String teacherUsername);
}