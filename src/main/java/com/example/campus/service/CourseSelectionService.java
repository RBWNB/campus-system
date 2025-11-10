package com.example.campus.service;

import com.example.campus.dto.CourseSelectionDto;
import com.example.campus.entity.Course;
import com.example.campus.entity.CourseSelection;
import com.example.campus.entity.Student;
import com.example.campus.repository.CourseRepository;
import com.example.campus.repository.CourseSelectionRepository;
import com.example.campus.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CourseSelectionService {

    @Autowired
    private StudentRepository studentRepository; // 假设您有 StudentRepository

    @Autowired
    private CourseRepository courseRepository; // 假设您有 CourseRepository

    @Autowired
    private CourseSelectionRepository selectionRepository;

    /**
     * 获取所有课程列表及其选课状态
     * @param username 当前登录用户的用户名
     * @param query 搜索关键字
     * @return 包含 isSelected 状态的课程列表
     */
    public List<CourseSelectionDto> getSelectionList(String username, String query) {
        // 1. 获取当前学生已选课程的 ID 集合
        Set<Long> selectedCourseIds = selectionRepository.findByStudent_User_Username(username)
                .stream()
                .map(selection -> selection.getCourse().getId())
                .collect(Collectors.toSet());

        // 2. 获取所有可用课程 (这里需要 CourseRepository 来实现搜索功能)
        // 假设 CourseRepository 中有一个方法来查找所有课程
        List<Course> availableCourses;
        if (query != null && !query.trim().isEmpty()) {
            // 假设 CourseRepository 提供了包含模糊查询的方法
            availableCourses = courseRepository.findByCodeContainingOrNameContaining(query, query);
        } else {
            availableCourses = courseRepository.findAll();
        }

        // 3. 映射为 DTO 并设置 isSelected 标志
        return availableCourses.stream()
                .map(course -> {
                    boolean isSelected = selectedCourseIds.contains(course.getId());
                    return new CourseSelectionDto(course, isSelected);
                })
                .collect(Collectors.toList());
    }

    /**
     * 执行选课操作
     * @param username 当前登录用户的用户名
     * @param courseId 待选课程的ID
     */
    @Transactional
    public void selectCourse(String username, Long courseId) {
        // 1. 查找学生
        Student student = studentRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("学生用户未找到"));

        // 2. 查找课程
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        // 3. 检查是否已选
        if (selectionRepository.findByStudent_User_UsernameAndCourse_Id(username, courseId).isPresent()) {
            // 抛出特定的异常，Controller 捕获后返回 409 Conflict
            throw new IllegalStateException("已选该课程");
        }

        // 4. (可选) 检查课程容量、时间冲突等业务逻辑...
        // if (course.getCurrentCapacity() >= course.getMaxCapacity()) {
        //     throw new IllegalStateException("课程已满");
        // }

        // 5. 保存选课记录
        CourseSelection selection = new CourseSelection();
        selection.setStudent(student);
        selection.setCourse(course);
        selectionRepository.save(selection);
    }

    /**
     * 执行退课操作
     * @param username 当前登录用户的用户名
     * @param courseId 待退课程的ID
     */
    @Transactional
    public void dropCourse(String username, Long courseId) {
        // 1. 查找选课记录是否存在
        if (selectionRepository.findByStudent_User_UsernameAndCourse_Id(username, courseId).isEmpty()) {
            throw new IllegalStateException("未选该课程，无法退课");
        }

        // 2. 删除选课记录 (使用 Repository 中定义的 delete 方法)
        selectionRepository.deleteByStudent_User_UsernameAndCourse_Id(username, courseId);
    }
}