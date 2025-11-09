// ClassroomRepository.java
package com.example.campus.repository;

import com.example.campus.entity.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    // 按名称或位置模糊查询（忽略大小写）
    List<Classroom> findByNameContainingIgnoreCaseOrLocationContainingIgnoreCase(String name, String location);

    // 原有的按名称精确查询
    Classroom findByName(String name);
}
