package com.example.campus.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
public class Course {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true)
    private String code;

    private String name;
    private BigDecimal credit;

    @Lob
    private String description;

    @Column(name="created_at")
    private Timestamp createdAt;
}
