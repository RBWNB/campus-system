package com.example.campus.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalTime;

// 导入 Jackson 注解
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "schedule")
@Data
@NoArgsConstructor
public class Schedule {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 解决 Jackson 序列化 Lazy 关系时的代理对象错误
    @ManyToOne
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "schedules"})
    private Course course;

    // 解决 Jackson 序列化 Lazy 关系时的代理对象错误
    @ManyToOne
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "schedules"})
    private Classroom classroom;

    // 原逻辑存储的是教师用户名，需替换为**教师真实姓名**
    private String teacher; // 现在存储教师真实姓名
    private Integer weekday;
    private LocalTime startTime;
    private LocalTime endTime;
    private String term;

    // 解决 Jackson 序列化 Lazy 关系时的代理对象错误
    @ManyToOne
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "teachers", "students"})
    private User teacherUser; // 关联教师用户（用于校验）
}