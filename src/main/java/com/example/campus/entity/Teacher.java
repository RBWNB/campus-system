package com.example.campus.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "teachers")
@Data
@NoArgsConstructor
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 与 User 一对一关联：user_id 为外键，非空且唯一
    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(name = "teacher_no", unique = true, nullable = false)
    private String teacherNo;

    private String title;
    private String department;
    private String phone;
    private String office;

    // 新增 name 字段（存储教师姓名，用于排课显示）
    private String name;
}