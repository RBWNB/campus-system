package com.example.campus.dto;

import lombok.Data;

@Data
public class ScheduleDTO {
    private Long courseId;
    private Long classroomId;
    private String teacher;
    private Integer weekday;
    private String startTime;
    private String endTime;
    private String term;
}
