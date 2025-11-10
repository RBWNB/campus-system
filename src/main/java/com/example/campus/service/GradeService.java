package com.example.campus.service;

import com.example.campus.dto.GradeDTO;
import com.example.campus.entity.*;
import com.example.campus.repository.CourseRepository;
import com.example.campus.repository.GradeRepository;
import com.example.campus.repository.StudentRepository;
import com.example.campus.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

@Service
public class GradeService {

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Grade saveGrade(GradeDTO gradeInput) {
        // 验证学生是否存在
        Optional<Student> studentOpt = studentRepository.findById(gradeInput.getStudentId());
        if (!studentOpt.isPresent()) {
            throw new RuntimeException("学生不存在");
        }

        // 验证课程是否存在
        Optional<Course> courseOpt = courseRepository.findById(gradeInput.getCourseId());
        if (!courseOpt.isPresent()) {
            throw new RuntimeException("课程不存在");
        }

        // 检查是否已经存在相同类型成绩
        Grade existingGrade = gradeRepository.findByStudentAndCourseAndGradeType(
                studentOpt.get(), courseOpt.get(), gradeInput.getGradeType());

        Grade grade;
        if (existingGrade != null) {
            // 更新现有成绩
            grade = existingGrade;
            grade.setScore(gradeInput.getScore());
            grade.setUpdatedAt(Timestamp.from(Instant.now()));
        } else {
            // 创建新成绩记录
            grade = new Grade();
            grade.setStudent(studentOpt.get());
            grade.setCourse(courseOpt.get());
            grade.setScore(gradeInput.getScore());
            grade.setGradeType(gradeInput.getGradeType());
            grade.setUpdatedAt(Timestamp.from(Instant.now()));
        }

        return gradeRepository.save(grade);
    }
}