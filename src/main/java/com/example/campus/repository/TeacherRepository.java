package com.example.campus.repository;

import com.example.campus.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByTeacherNo(String teacherNo);
    Optional<Teacher> findByUserId(Long userId);
    Optional<Teacher> findByUserUsername(String username);
}