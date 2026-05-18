package com.example.projectjavawebapplicationphamcongthanh.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BorrowRequestDTO {
    @NotNull(message = "Buổi mentoring liên kết không được để trống")
    private Long sessionId;

    private List<BorrowItemDTO> items;
}
