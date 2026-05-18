package com.example.projectjavawebapplicationphamcongthanh.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcademicProfileDTO {
    private String studentName;
    private String phone;
    private String departmentName;
    private Long totalCompletedSessions;
    private Double averageScore;
}
