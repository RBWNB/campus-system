package com.example.campus.repository;

import com.example.campus.entity.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    Classroom findByName(String name);
}
