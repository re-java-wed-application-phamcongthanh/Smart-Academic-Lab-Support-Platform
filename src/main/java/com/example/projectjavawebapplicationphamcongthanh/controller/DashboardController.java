package com.example.projectjavawebapplicationphamcongthanh.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import com.example.projectjavawebapplicationphamcongthanh.repository.UserRepository;
import com.example.projectjavawebapplicationphamcongthanh.entity.User;
import com.example.projectjavawebapplicationphamcongthanh.dto.AcademicProfileDTO;

@Controller
public class DashboardController {

    private final UserRepository userRepository;

    public DashboardController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isLecturer = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_LECTURER"));
            boolean isStudent = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));

            if (isAdmin) {
                return "redirect:/admin/dashboard";
            } else if (isLecturer) {
                return "redirect:/lecturer/dashboard";
            } else if (isStudent) {
                return "redirect:/student/dashboard";
            }
        }
        return "redirect:/login";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/lecturer/dashboard")
    public String lecturerDashboard() {
        return "lecturer/dashboard";
    }

    @GetMapping("/student/dashboard")
    public String studentDashboard() {
        return "student/dashboard";
    }

    @GetMapping("/student/profile")
    public String studentProfile(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User student = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên"));
        AcademicProfileDTO profileDTO = userRepository.getAcademicProfile(student.getId());
        model.addAttribute("profile", profileDTO);
        return "student/profile";
    }
}
