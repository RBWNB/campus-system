
package com.example.campus.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class GradeDTO {
    private Long courseId;
    private Long studentId;
    private BigDecimal score;
    private String gradeType;
}