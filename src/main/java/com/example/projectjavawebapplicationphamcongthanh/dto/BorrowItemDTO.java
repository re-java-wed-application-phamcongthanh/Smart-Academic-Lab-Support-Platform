package com.example.projectjavawebapplicationphamcongthanh.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BorrowItemDTO {
    @NotNull(message = "Thiết bị không được để trống")
    private Long equipmentId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng mượn tối thiểu là 1")
    private Integer quantity;
}
