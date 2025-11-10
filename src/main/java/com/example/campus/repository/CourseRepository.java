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
    List<Course> findByCodeContainingOrNameContaining(String codeQuery, String nameQuery);

    // 新增方法：获取所有课程代码（用于选择）
    @Query("SELECT DISTINCT c.code FROM Course c")
    List<String> findAllCourseCodes();

    // 新增方法：根据课程代码获取课程信息
    @Query("SELECT c FROM Course c WHERE c.code = :code")
    List<Course> findByCourseCode(@Param("code") String code);

    // 新增方法：检查课程代码是否存在
    boolean existsByCode(String code);

    // 新增方法：检查课程代码是否存在（排除当前课程）
    boolean existsByCodeAndIdNot(String code, Long id);
}