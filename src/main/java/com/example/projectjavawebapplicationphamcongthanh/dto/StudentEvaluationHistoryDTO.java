package com.example.projectjavawebapplicationphamcongthanh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentEvaluationHistoryDTO {
    private Long sessionId;
    private String lecturerName;
    private LocalDate date;
    private String startTime;
    private String endTime;
    private String comments;
    private Integer score;
    private List<String> borrowedEquipments; // e.g. ["Thiết bị A (x2)", "Thiết bị B (x1)"]
}
