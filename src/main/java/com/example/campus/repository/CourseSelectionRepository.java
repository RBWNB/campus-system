package com.example.campus.repository;

import com.example.campus.entity.CourseSelection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CourseSelectionRepository extends JpaRepository<CourseSelection, Long> {
    // 查找学生已选的所有课程
    List<CourseSelection> findByStudent_User_Username(String username);

    // 检查学生是否已选某门课程
    Optional<CourseSelection> findByStudent_User_UsernameAndCourse_Id(String username, Long courseId);

    // 通过ID删除选课记录（用于退课）
    void deleteByStudent_User_UsernameAndCourse_Id(String username, Long courseId);
}