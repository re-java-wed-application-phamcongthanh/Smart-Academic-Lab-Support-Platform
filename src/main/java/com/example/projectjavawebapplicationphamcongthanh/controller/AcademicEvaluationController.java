package com.example.projectjavawebapplicationphamcongthanh.controller;

import com.example.projectjavawebapplicationphamcongthanh.entity.MentoringSession;
import com.example.projectjavawebapplicationphamcongthanh.exception.CustomValidationException;
import com.example.projectjavawebapplicationphamcongthanh.service.AcademicEvaluationService;
import com.example.projectjavawebapplicationphamcongthanh.service.MentoringSessionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/lecturer/mentoring")
public class AcademicEvaluationController {

    private final AcademicEvaluationService academicEvaluationService;
    private final MentoringSessionService mentoringSessionService;

    public AcademicEvaluationController(AcademicEvaluationService academicEvaluationService, MentoringSessionService mentoringSessionService) {
        this.academicEvaluationService = academicEvaluationService;
        this.mentoringSessionService = mentoringSessionService;
    }

    @GetMapping("/evaluate/{id}")
    public String showEvaluationForm(@PathVariable("id") Long id, Model model) {
        MentoringSession session = mentoringSessionService.getById(id);
        if (!"APPROVED".equals(session.getStatus())) {
            return "redirect:/lecturer/mentoring?error=Chỉ có thể đánh giá buổi hẹn đã phê duyệt (APPROVED)!";
        }
        model.addAttribute("session", session);
        return "lecturer/evaluation-form";
    }

    @PostMapping("/evaluate/{id}")
    public String saveEvaluation(@PathVariable("id") Long id,
                                 @RequestParam("comments") String comments,
                                 @RequestParam("score") Integer score,
                                 Model model) {
        try {
            academicEvaluationService.evaluateSession(id, comments, score);
        } catch (CustomValidationException e) {
            MentoringSession session = mentoringSessionService.getById(id);
            model.addAttribute("session", session);
            model.addAttribute("error", e.getMessage());
            return "lecturer/evaluation-form";
        }
        return "redirect:/lecturer/mentoring?success_evaluate";
    }
}
