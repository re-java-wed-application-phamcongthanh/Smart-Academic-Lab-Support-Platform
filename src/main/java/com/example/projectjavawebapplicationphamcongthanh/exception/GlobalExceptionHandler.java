package com.example.projectjavawebapplicationphamcongthanh.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomValidationException.class)
    public String handleCustomValidation(CustomValidationException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/generic";
    }

    @ExceptionHandler(Exception.class)
    public String handleAllExceptions(Exception ex, Model model) {
        model.addAttribute("errorMessage", "Đã xảy ra lỗi hệ thống: " + ex.getMessage());
        ex.printStackTrace(); // Hữu ích để debug trong logs
        return "error/generic";
    }
}
