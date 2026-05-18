package com.example.projectjavawebapplicationphamcongthanh.controller;

import com.example.projectjavawebapplicationphamcongthanh.dto.BorrowItemDTO;
import com.example.projectjavawebapplicationphamcongthanh.dto.BorrowRequestDTO;
import com.example.projectjavawebapplicationphamcongthanh.entity.User;
import com.example.projectjavawebapplicationphamcongthanh.exception.CustomValidationException;
import com.example.projectjavawebapplicationphamcongthanh.repository.EquipmentRepository;
import com.example.projectjavawebapplicationphamcongthanh.repository.UserRepository;
import com.example.projectjavawebapplicationphamcongthanh.service.BorrowingService;
import com.example.projectjavawebapplicationphamcongthanh.service.MentoringSessionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@Controller
public class BorrowingController {

    private final BorrowingService borrowingService;
    private final MentoringSessionService mentoringSessionService;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;

    public BorrowingController(BorrowingService borrowingService,
                               MentoringSessionService mentoringSessionService,
                               EquipmentRepository equipmentRepository,
                               UserRepository userRepository) {
        this.borrowingService = borrowingService;
        this.mentoringSessionService = mentoringSessionService;
        this.equipmentRepository = equipmentRepository;
        this.userRepository = userRepository;
    }

    // --- STUDENT VIEW & BORROWING ---

    @GetMapping("/student/borrow")
    public String listStudentBorrows(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User student = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên"));
        model.addAttribute("records", borrowingService.getByStudent(student.getId()));
        return "student/borrow-list";
    }

    @GetMapping("/student/borrow/new")
    public String showBorrowForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User student = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên"));

        // Học viên có thể mượn thiết bị cho các buổi mentoring được duyệt (APPROVED hoặc COMPLETED)
        model.addAttribute("sessions", mentoringSessionService.getByStudent(student.getId()));
        model.addAttribute("equipments", equipmentRepository.findAll());

        BorrowRequestDTO dto = new BorrowRequestDTO();
        dto.setItems(new ArrayList<>());
        // Thêm sẵn 3 slot thiết bị trống để điền trên giao diện
        for (int i = 0; i < 3; i++) {
            dto.getItems().add(new BorrowItemDTO());
        }
        model.addAttribute("borrowRequest", dto);
        return "student/borrow-form";
    }

    @PostMapping("/student/borrow/save")
    public String saveBorrow(@ModelAttribute("borrowRequest") BorrowRequestDTO dto,
                             Model model,
                             @AuthenticationPrincipal UserDetails userDetails) {
        User student = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên"));

        // Lọc bỏ các dòng chọn thiết bị trống (không chọn thiết bị hoặc số lượng trống/bằng 0)
        if (dto.getItems() != null) {
            dto.getItems().removeIf(item -> item.getEquipmentId() == null || item.getQuantity() == null || item.getQuantity() <= 0);
        }

        try {
            borrowingService.createBorrowing(dto);
        } catch (CustomValidationException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("sessions", mentoringSessionService.getByStudent(student.getId()));
            model.addAttribute("equipments", equipmentRepository.findAll());
            return "student/borrow-form";
        }

        return "redirect:/student/borrow?success";
    }

    // --- ADMIN / LECTURER VIEWS ---

    @GetMapping("/admin/borrow")
    public String listAllBorrows(Model model) {
        model.addAttribute("records", borrowingService.getAll());
        return "admin/borrow-list";
    }

    @GetMapping("/admin/borrow/return/{id}")
    public String returnRecord(@PathVariable("id") Long id) {
        try {
            borrowingService.returnEquipment(id);
        } catch (CustomValidationException e) {
            return "redirect:/admin/borrow?error=" + e.getMessage();
        }
        return "redirect:/admin/borrow?success_return";
    }
}
