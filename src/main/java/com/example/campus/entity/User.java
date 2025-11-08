package com.example.campus.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    // 枚举字段：添加 Jackson 转换注解，确保字符串与枚举正确映射
    @Enumerated(EnumType.STRING)
    private Role role;

    private String name;
    private String email;

    @Column(name = "created_at")
    private Timestamp createdAt;

    // 与 Teacher 双向关联：级联保存/删除
    // 添加 @JsonIgnore 避免递归序列化，同时保留所有数据库关联功能
    @JsonIgnore
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Teacher teacher;

    // 若 Role 是独立类，删除 User 内部的枚举定义（保持独立枚举）
    // 以下是独立 Role 枚举的 Jackson 转换辅助方法（如果独立枚举中没有，可添加到 Role 类）
    // （如果已在 Role 类中添加，此处无需重复）
}