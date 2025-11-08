package com.example.campus.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "schedule")
@Data
@NoArgsConstructor
public class Schedule {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Course course;

    @ManyToOne
    private Classroom classroom;

    private String teacher;
    private Integer weekday;
    private LocalTime startTime;
    private LocalTime endTime;
    private String term;

    @ManyToOne
    private User teacherUser;
}
