package com.example.campus.dto;

import com.example.campus.entity.Course;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseSelectionDto {
    private Long id;
    private String code;
    private String name;
    private BigDecimal credit;
    private String description;

    // 关键字段：学生是否已选此课程
    private boolean isSelected;

    public CourseSelectionDto(Course course, boolean isSelected) {
        this.id = course.getId();
        this.code = course.getCode();
        this.name = course.getName();
        this.credit = course.getCredit();
        this.description = course.getDescription();
        this.isSelected = isSelected;
    }
}