package com.example.projectjavawebapplicationphamcongthanh.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;

import com.example.projectjavawebapplicationphamcongthanh.repository.UserRepository;
import com.example.projectjavawebapplicationphamcongthanh.repository.UserProfileRepository;
import com.example.projectjavawebapplicationphamcongthanh.repository.MentoringSessionRepository;
import com.example.projectjavawebapplicationphamcongthanh.repository.AcademicEvaluationRepository;
import com.example.projectjavawebapplicationphamcongthanh.repository.BorrowingRecordRepository;
import com.example.projectjavawebapplicationphamcongthanh.entity.User;
import com.example.projectjavawebapplicationphamcongthanh.entity.UserProfile;
import com.example.projectjavawebapplicationphamcongthanh.entity.MentoringSession;
import com.example.projectjavawebapplicationphamcongthanh.entity.AcademicEvaluation;
import com.example.projectjavawebapplicationphamcongthanh.entity.BorrowingRecord;
import com.example.projectjavawebapplicationphamcongthanh.entity.BorrowingDetail;
import com.example.projectjavawebapplicationphamcongthanh.dto.AcademicProfileDTO;
import com.example.projectjavawebapplicationphamcongthanh.dto.StudentEvaluationHistoryDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class DashboardController {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final MentoringSessionRepository mentoringSessionRepository;
    private final AcademicEvaluationRepository academicEvaluationRepository;
    private final BorrowingRecordRepository borrowingRecordRepository;

    public DashboardController(UserRepository userRepository,
                               UserProfileRepository userProfileRepository,
                               MentoringSessionRepository mentoringSessionRepository,
                               AcademicEvaluationRepository academicEvaluationRepository,
                               BorrowingRecordRepository borrowingRecordRepository) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.mentoringSessionRepository = mentoringSessionRepository;
        this.academicEvaluationRepository = academicEvaluationRepository;
        this.borrowingRecordRepository = borrowingRecordRepository;
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

        // 1. Thống kê GPA và tổng số buổi học
        AcademicProfileDTO profileDTO = userRepository.getAcademicProfile(student.getId());
        if (profileDTO == null) {
            profileDTO = new AcademicProfileDTO();
            profileDTO.setStudentName(student.getProfile() != null ? student.getProfile().getFullName() : student.getUsername());
            profileDTO.setPhone(student.getProfile() != null ? student.getProfile().getPhone() : "");
            profileDTO.setDepartmentName(student.getDepartment() != null ? student.getDepartment().getName() : "Chung");
            profileDTO.setTotalCompletedSessions(0L);
            profileDTO.setAverageScore(null);
        } else {
            if (profileDTO.getStudentName() == null) {
                profileDTO.setStudentName(student.getProfile() != null ? student.getProfile().getFullName() : student.getUsername());
            }
            if (profileDTO.getDepartmentName() == null) {
                profileDTO.setDepartmentName(student.getDepartment() != null ? student.getDepartment().getName() : "Chung");
            }
            if (profileDTO.getTotalCompletedSessions() == null) {
                profileDTO.setTotalCompletedSessions(0L);
            }
        }
        model.addAttribute("profile", profileDTO);

        // 2. Thông tin profile thô phục vụ cập nhật
        model.addAttribute("rawProfile", student.getProfile());

        // 3. Danh sách học bạ chi tiết (JOIN lịch sử học tập)
        List<MentoringSession> completedSessions = mentoringSessionRepository.findByStudentIdAndStatus(student.getId(), "COMPLETED");
        List<StudentEvaluationHistoryDTO> historyList = new ArrayList<>();

        for (MentoringSession s : completedSessions) {
            Optional<AcademicEvaluation> evalOpt = academicEvaluationRepository.findBySessionId(s.getId());
            Optional<BorrowingRecord> borrowOpt = borrowingRecordRepository.findBySessionId(s.getId());

            List<String> borrowed = new ArrayList<>();
            if (borrowOpt.isPresent()) {
                BorrowingRecord record = borrowOpt.get();
                if (record.getDetails() != null) {
                    for (BorrowingDetail detail : record.getDetails()) {
                        borrowed.add(detail.getEquipment().getName() + " (x" + detail.getQuantity() + ")");
                    }
                }
            }

            StudentEvaluationHistoryDTO historyDTO = StudentEvaluationHistoryDTO.builder()
                    .sessionId(s.getId())
                    .lecturerName(s.getLecturer().getProfile() != null ? s.getLecturer().getProfile().getFullName() : s.getLecturer().getUsername())
                    .date(s.getDate())
                    .startTime(s.getStartTime().toString())
                    .endTime(s.getEndTime().toString())
                    .comments(evalOpt.isPresent() ? evalOpt.get().getComments() : "Không có nhận xét")
                    .score(evalOpt.isPresent() ? evalOpt.get().getScore() : 0)
                    .borrowedEquipments(borrowed)
                    .build();

            historyList.add(historyDTO);
        }

        model.addAttribute("historyList", historyList);
        return "student/profile";
    }

    @PostMapping("/student/profile/update")
    @Transactional
    public String studentProfileUpdate(@RequestParam("fullName") String fullName,
                                       @RequestParam("email") String email,
                                       @RequestParam("phone") String phone,
                                       @RequestParam("address") String address,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        User student = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên"));

        UserProfile profile = student.getProfile();
        if (profile == null) {
            profile = UserProfile.builder().user(student).build();
        }

        profile.setFullName(fullName);
        profile.setEmail(email);
        profile.setPhone(phone);
        profile.setAddress(address);

        userProfileRepository.save(profile);
        return "redirect:/student/profile?success_update";
    }
}
