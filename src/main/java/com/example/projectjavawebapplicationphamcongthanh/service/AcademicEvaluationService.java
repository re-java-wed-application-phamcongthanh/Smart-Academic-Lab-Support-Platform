package com.example.projectjavawebapplicationphamcongthanh.service;

import com.example.projectjavawebapplicationphamcongthanh.entity.AcademicEvaluation;
import com.example.projectjavawebapplicationphamcongthanh.entity.MentoringSession;
import com.example.projectjavawebapplicationphamcongthanh.exception.CustomValidationException;
import com.example.projectjavawebapplicationphamcongthanh.repository.AcademicEvaluationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AcademicEvaluationService {

    private final AcademicEvaluationRepository academicEvaluationRepository;
    private final MentoringSessionService mentoringSessionService;

    public AcademicEvaluationService(AcademicEvaluationRepository academicEvaluationRepository, MentoringSessionService mentoringSessionService) {
        this.academicEvaluationRepository = academicEvaluationRepository;
        this.mentoringSessionService = mentoringSessionService;
    }

    @Transactional
    public AcademicEvaluation evaluateSession(Long sessionId, String comments, Integer score) {
        if (score == null || score < 0 || score > 10) {
            throw new CustomValidationException("Điểm số phải nằm trong khoảng từ 0 đến 10!");
        }

        MentoringSession session = mentoringSessionService.getById(sessionId);

        // 1. Chuyển trạng thái buổi mentoring sang COMPLETED
        mentoringSessionService.completeSession(sessionId);

        // 2. Tạo đánh giá học thuật
        AcademicEvaluation evaluation = AcademicEvaluation.builder()
                .session(session)
                .comments(comments)
                .score(score)
                .build();

        return academicEvaluationRepository.save(evaluation);
    }

    public Optional<AcademicEvaluation> getBySessionId(Long sessionId) {
        return academicEvaluationRepository.findBySessionId(sessionId);
    }
}
