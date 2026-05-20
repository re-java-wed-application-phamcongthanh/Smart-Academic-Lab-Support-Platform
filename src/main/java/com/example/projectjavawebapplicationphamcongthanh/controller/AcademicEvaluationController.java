package com.example.projectjavawebapplicationphamcongthanh.controller;

import com.example.projectjavawebapplicationphamcongthanh.entity.MentoringSession;
import com.example.projectjavawebapplicationphamcongthanh.exception.CustomValidationException;
import com.example.projectjavawebapplicationphamcongthanh.repository.EquipmentRepository;
import com.example.projectjavawebapplicationphamcongthanh.service.AcademicEvaluationService;
import com.example.projectjavawebapplicationphamcongthanh.service.MentoringSessionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/lecturer/mentoring")
public class AcademicEvaluationController {

    private final AcademicEvaluationService academicEvaluationService;
    private final MentoringSessionService mentoringSessionService;
    private final EquipmentRepository equipmentRepository;

    public AcademicEvaluationController(AcademicEvaluationService academicEvaluationService,
                                        MentoringSessionService mentoringSessionService,
                                        EquipmentRepository equipmentRepository) {
        this.academicEvaluationService = academicEvaluationService;
        this.mentoringSessionService = mentoringSessionService;
        this.equipmentRepository = equipmentRepository;
    }

    @GetMapping("/evaluate/{id}")
    public String showEvaluationForm(@PathVariable("id") Long id, Model model) {
        MentoringSession session = mentoringSessionService.getById(id);
        if (!"APPROVED".equals(session.getStatus())) {
            return "redirect:/lecturer/mentoring?error=" + java.net.URLEncoder.encode("Chỉ có thể đánh giá buổi hẹn đã phê duyệt (APPROVED)!", java.nio.charset.StandardCharsets.UTF_8);
        }
        model.addAttribute("mentoringSession", session);
        model.addAttribute("equipments", equipmentRepository.findAll());
        return "lecturer/evaluation-form";
    }

    @PostMapping("/evaluate/{id}")
    public String saveEvaluation(@PathVariable("id") Long id,
                                 @RequestParam("comments") String comments,
                                 @RequestParam("score") Integer score,
                                 @RequestParam(value = "equipmentIds", required = false) List<Long> equipmentIds,
                                 @RequestParam(value = "quantities", required = false) List<Integer> quantities,
                                 Model model) {
        try {
            academicEvaluationService.evaluateSession(id, comments, score, equipmentIds, quantities);
        } catch (CustomValidationException e) {
            MentoringSession session = mentoringSessionService.getById(id);
            model.addAttribute("mentoringSession", session);
            model.addAttribute("equipments", equipmentRepository.findAll());
            model.addAttribute("error", e.getMessage());
            return "lecturer/evaluation-form";
        }
        return "redirect:/lecturer/mentoring?success_evaluate";
    }
}
