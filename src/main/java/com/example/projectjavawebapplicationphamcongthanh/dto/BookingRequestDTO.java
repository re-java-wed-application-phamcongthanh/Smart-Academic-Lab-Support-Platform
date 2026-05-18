package com.example.projectjavawebapplicationphamcongthanh.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class BookingRequestDTO {
    @NotNull(message = "Giảng viên không được để trống")
    private Long lecturerId;

    @NotNull(message = "Ngày hẹn không được để trống")
    @Future(message = "Ngày hẹn phải là một ngày trong tương lai")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @NotNull(message = "Giờ bắt đầu không được để trống")
    private LocalTime startTime;

    @NotNull(message = "Giờ kết thúc không được để trống")
    private LocalTime endTime;
}
