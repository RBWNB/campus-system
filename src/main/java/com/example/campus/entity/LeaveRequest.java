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
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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
}
