package com.example.campus.dto;

import lombok.Data;

@Data
public class ScheduleDTO {
    private Long courseId;
    private Long classroomId;
    private Long teacherId; // 新增教师ID字段
    private String teacher;
    private Integer weekday;
    private String startTime;
    private String endTime;
    private String term;

    private String courseName;
    private String classroomName;
}