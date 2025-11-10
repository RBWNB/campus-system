// LeaveRequest.java
package com.example.campus.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;

@Entity
@Table(name = "leaves")
@Data
@NoArgsConstructor
public class LeaveRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Student student;

    private Date startDate;
    private Date endDate;

    @Lob
    private String reason;

    @Enumerated(EnumType.STRING)
    private LeaveStatus status;

    @Column(name = "applied_at")
    private Timestamp appliedAt;

    @Column(name = "reviewed_at")
    private Timestamp reviewedAt;

    @Column(name = "reviewer")
    private String reviewer;

    @Column(name = "comment")
    private String comment;

    // 添加获取学生姓名的方法
    public String getStudentName() {
        return student != null && student.getUser() != null ? student.getUser().getName() : "";
    }

    // 添加获取学生学号的方法
    public String getStudentId() {
        return student != null ? student.getStudentNo() : "";
    }
}
