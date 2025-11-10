package com.example.campus.repository;

import com.example.campus.entity.User;
import com.example.campus.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchByKeyword(@Param("keyword") String keyword);

    List<User> findByRole(Role role);

    @Query("SELECT u FROM User u WHERE " +
            "u.role = :role AND (" +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
            ")")
    List<User> searchByKeywordAndRole(@Param("keyword") String keyword, @Param("role") Role role);

    // 新增：查询教师用户并关联 Teacher 实体（避免懒加载问题）
    @Query("SELECT u FROM User u JOIN FETCH u.teacher WHERE u.role = :role")
    List<User> findTeacherUsersWithTeacher(@Param("role") Role role);

    // 新增：根据教师ID查询关联的用户（用于排课详情查询）
    @Query("SELECT u FROM User u JOIN FETCH u.teacher WHERE u.teacher.id = :teacherId")
    Optional<User> findByTeacherId(@Param("teacherId") Long teacherId);
}