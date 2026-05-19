package com.example.projectjavawebapplicationphamcongthanh.controller;

import com.example.projectjavawebapplicationphamcongthanh.dto.BookingRequestDTO;
import com.example.projectjavawebapplicationphamcongthanh.entity.Role;
import com.example.projectjavawebapplicationphamcongthanh.entity.User;
import com.example.projectjavawebapplicationphamcongthanh.exception.CustomValidationException;
import com.example.projectjavawebapplicationphamcongthanh.repository.UserRepository;
import com.example.projectjavawebapplicationphamcongthanh.service.MentoringSessionService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class MentoringSessionController {

    private final MentoringSessionService mentoringSessionService;
    private final UserRepository userRepository;

    public MentoringSessionController(MentoringSessionService mentoringSessionService, UserRepository userRepository) {
        this.mentoringSessionService = mentoringSessionService;
        this.userRepository = userRepository;
    }

    // --- STUDENT VIEW & BOOKING ---

    @GetMapping("/student/mentoring")
    public String listStudentSessions(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User student = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên"));
        model.addAttribute("sessions", mentoringSessionService.getByStudent(student.getId()));
        return "student/mentoring-list";
    }

    @GetMapping("/student/mentoring/new")
    public String showBookingForm(Model model) {
        model.addAttribute("booking", new BookingRequestDTO());
        model.addAttribute("lecturers", userRepository.findByRole(Role.LECTURER));
        return "student/booking-form";
    }

    @PostMapping("/student/mentoring/save")
    public String saveBooking(@Valid @ModelAttribute("booking") BookingRequestDTO dto,
                              BindingResult result,
                              Model model,
                              @AuthenticationPrincipal UserDetails userDetails) {
        if (result.hasErrors()) {
            model.addAttribute("lecturers", userRepository.findByRole(Role.LECTURER));
            return "student/booking-form";
        }
        User student = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên"));
        try {
            mentoringSessionService.createSession(student.getId(), dto);
        } catch (CustomValidationException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("lecturers", userRepository.findByRole(Role.LECTURER));
            return "student/booking-form";
        }
        return "redirect:/student/mentoring?success";
    }

    @GetMapping("/student/mentoring/cancel/{id}")
    public String cancel(@PathVariable("id") Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User student = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên"));
        try {
            mentoringSessionService.cancelSession(id, student.getId());
        } catch (CustomValidationException e) {
            return "redirect:/student/mentoring?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }
        return "redirect:/student/mentoring?success_cancel";
    }

    // --- LECTURER VIEW & APPROVAL ---

    @GetMapping("/lecturer/mentoring")
    public String listLecturerSessions(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User lecturer = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giảng viên"));
        model.addAttribute("sessions", mentoringSessionService.getByLecturer(lecturer.getId()));
        return "lecturer/mentoring-list";
    }

    @GetMapping("/lecturer/mentoring/approve/{id}")
    public String approve(@PathVariable("id") Long id) {
        try {
            mentoringSessionService.approveSession(id);
        } catch (CustomValidationException e) {
            return "redirect:/lecturer/mentoring?error=" + e.getMessage();
        }
        return "redirect:/lecturer/mentoring?success_approve";
    }

    @GetMapping("/lecturer/mentoring/reject/{id}")
    public String reject(@PathVariable("id") Long id) {
        try {
            mentoringSessionService.rejectSession(id);
        } catch (CustomValidationException e) {
            return "redirect:/lecturer/mentoring?error=" + e.getMessage();
        }
        return "redirect:/lecturer/mentoring?success_reject";
    }
}
