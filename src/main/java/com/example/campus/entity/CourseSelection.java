package com.example.campus.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "course_selections", uniqueConstraints = {
        // 确保同一个学生不能选择同一门课程两次
        @UniqueConstraint(columnNames = {"student_id", "course_id"})
})
@Data
@NoArgsConstructor
public class CourseSelection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 关联学生实体 (ManyToOne)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // 关联课程实体 (ManyToOne)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    // 选课时间
    @CreationTimestamp
    @Column(name="selected_at", updatable = false)
    private Timestamp selectedAt;

    // 可选：添加课程学年、学期等字段，但通常通过 Course 关联即可获取
}