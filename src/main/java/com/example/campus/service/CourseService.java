package com.example.campus.service;

import com.example.campus.entity.Course;
import com.example.campus.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public List<Course> searchCourses(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllCourses();
        }
        return courseRepository.findByCodeContainingOrNameContaining(keyword, keyword);
    }

    public Optional<Course> getCourseById(Long id) {
        return courseRepository.findById(id);
    }

    public Optional<Course> getCourseByCode(String code) {
        return courseRepository.findByCode(code);
    }

    public Course saveCourse(Course course) {
        // 检查课程代码是否已存在（编辑时排除自身）
        if (course.getId() != null) {
            if (courseRepository.existsByCodeAndIdNot(course.getCode(), course.getId())) {
                throw new RuntimeException("课程代码已存在");
            }
        } else {
            if (courseRepository.existsByCode(course.getCode())) {
                throw new RuntimeException("课程代码已存在");
            }
        }
        return courseRepository.save(course);
    }

    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }

    // 获取所有课程代码（用于下拉选择）
    public List<String> getAllCourseCodes() {
        return courseRepository.findAllCourseCodes();
    }

    // 根据课程代码获取课程信息
    public Optional<Course> getCourseInfoByCode(String code) {
        List<Course> courses = courseRepository.findByCourseCode(code);
        return courses.isEmpty() ? Optional.empty() : Optional.of(courses.get(0));
    }
}