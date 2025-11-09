package com.example.campus.repository;

import com.example.campus.entity.User;
import com.example.campus.entity.Role; // 确保 Role 枚举类型已导入
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

    // 已修改：现在接收 Role 枚举类型
    List<User> findByRole(Role role);

    // 已修改：现在接收 Role 枚举类型
    @Query("SELECT u FROM User u WHERE " +
            "u.role = :role AND (" +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
            ")")
    List<User> searchByKeywordAndRole(@Param("keyword") String keyword, @Param("role") Role role);
}