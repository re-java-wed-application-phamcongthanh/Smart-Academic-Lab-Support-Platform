package com.example.projectjavawebapplicationphamcongthanh.controller;

import com.example.projectjavawebapplicationphamcongthanh.dto.BorrowItemDTO;
import com.example.projectjavawebapplicationphamcongthanh.dto.BorrowRequestDTO;
import com.example.projectjavawebapplicationphamcongthanh.entity.User;
import com.example.projectjavawebapplicationphamcongthanh.exception.CustomValidationException;
import com.example.projectjavawebapplicationphamcongthanh.repository.EquipmentRepository;
import com.example.projectjavawebapplicationphamcongthanh.repository.UserRepository;
import com.example.projectjavawebapplicationphamcongthanh.service.BorrowingService;
import com.example.projectjavawebapplicationphamcongthanh.service.MentoringSessionService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
        java.util.List<com.example.projectjavawebapplicationphamcongthanh.entity.MentoringSession> sessions = mentoringSessionService.getByStudent(student.getId());
        if (sessions == null) {
            sessions = new java.util.ArrayList<>();
        }
        model.addAttribute("sessions", sessions);

        java.util.List<com.example.projectjavawebapplicationphamcongthanh.entity.Equipment> equipments = equipmentRepository.findAll();
        if (equipments == null) {
            equipments = new java.util.ArrayList<>();
        }
        model.addAttribute("equipments", equipments);

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
    public String saveBorrow(@Valid @ModelAttribute("borrowRequest") BorrowRequestDTO dto,
                             BindingResult result,
                             Model model,
                             @AuthenticationPrincipal UserDetails userDetails) {
        User student = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên"));

        // Validate các dòng thiết bị
        boolean hasAtLeastOneRow = false;
        boolean hasRowErrors = false;
        if (dto.getItems() != null) {
            for (int i = 0; i < dto.getItems().size(); i++) {
                BorrowItemDTO item = dto.getItems().get(i);
                boolean hasEq = item.getEquipmentId() != null;
                boolean hasQty = item.getQuantity() != null;

                if (hasEq || hasQty) {
                    if (!hasEq) {
                        result.rejectValue("items[" + i + "].equipmentId", "error.items.equipmentId", "Vui lòng chọn thiết bị");
                        hasRowErrors = true;
                    }
                    if (!hasQty) {
                        result.rejectValue("items[" + i + "].quantity", "error.items.quantity", "Vui lòng nhập số lượng");
                        hasRowErrors = true;
                    } else if (item.getQuantity() <= 0) {
                        result.rejectValue("items[" + i + "].quantity", "error.items.quantity", "Số lượng mượn phải lớn hơn 0");
                        hasRowErrors = true;
                    }

                    if (hasEq && hasQty && item.getQuantity() > 0) {
                        hasAtLeastOneRow = true;
                    }
                }
            }
        }

        if (!hasRowErrors && !hasAtLeastOneRow && !result.hasErrors()) {
            result.rejectValue("items", "error.items", "Danh sách thiết bị mượn không được để trống!");
        }

        if (result.hasErrors()) {
            model.addAttribute("error", "Đăng ký mượn thiết bị không thành công. Vui lòng kiểm tra lại thông tin!");
            
            java.util.List<com.example.projectjavawebapplicationphamcongthanh.entity.MentoringSession> sessions = mentoringSessionService.getByStudent(student.getId());
            if (sessions == null) {
                sessions = new java.util.ArrayList<>();
            }
            model.addAttribute("sessions", sessions);

            java.util.List<com.example.projectjavawebapplicationphamcongthanh.entity.Equipment> equipments = equipmentRepository.findAll();
            if (equipments == null) {
                equipments = new java.util.ArrayList<>();
            }
            model.addAttribute("equipments", equipments);
            
            return "student/borrow-form";
        }

        // Lọc bỏ các dòng chọn thiết bị trống (đã được xác định là hoàn toàn trống ở trên)
        if (dto.getItems() != null) {
            dto.getItems().removeIf(item -> item.getEquipmentId() == null || item.getQuantity() == null || item.getQuantity() <= 0);
        }

        try {
            borrowingService.createBorrowing(dto);
        } catch (CustomValidationException e) {
            String msg = e.getMessage();
            if (msg.contains("Buổi mentoring")) {
                result.rejectValue("sessionId", "error.sessionId", msg);
            } else if (msg.contains("thiết bị mượn")) {
                result.rejectValue("items", "error.items", msg);
            } else {
                model.addAttribute("error", msg);
            }
            
            java.util.List<com.example.projectjavawebapplicationphamcongthanh.entity.MentoringSession> sessions = mentoringSessionService.getByStudent(student.getId());
            if (sessions == null) {
                sessions = new java.util.ArrayList<>();
            }
            model.addAttribute("sessions", sessions);

            java.util.List<com.example.projectjavawebapplicationphamcongthanh.entity.Equipment> equipments = equipmentRepository.findAll();
            if (equipments == null) {
                equipments = new java.util.ArrayList<>();
            }
            model.addAttribute("equipments", equipments);

            // Đảm bảo list items luôn có đủ 3 phần tử để hiển thị lại trên form sau khi đã lọc
            if (dto.getItems() == null) {
                dto.setItems(new ArrayList<>());
            }
            while (dto.getItems().size() < 3) {
                dto.getItems().add(new BorrowItemDTO());
            }

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
            return "redirect:/admin/borrow?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }
        return "redirect:/admin/borrow?success_return";
    }

    @GetMapping("/admin/borrow/issue/{id}")
    public String issueRecord(@PathVariable("id") Long id) {
        try {
            borrowingService.confirmIssuance(id);
        } catch (CustomValidationException e) {
            return "redirect:/admin/borrow?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }
        return "redirect:/admin/borrow?success_issue";
    }
}
