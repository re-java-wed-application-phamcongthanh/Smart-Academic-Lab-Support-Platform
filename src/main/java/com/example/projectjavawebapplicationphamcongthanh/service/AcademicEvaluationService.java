package com.example.projectjavawebapplicationphamcongthanh.service;

import com.example.projectjavawebapplicationphamcongthanh.entity.AcademicEvaluation;
import com.example.projectjavawebapplicationphamcongthanh.entity.MentoringSession;
import com.example.projectjavawebapplicationphamcongthanh.entity.BorrowingRecord;
import com.example.projectjavawebapplicationphamcongthanh.entity.BorrowingDetail;
import com.example.projectjavawebapplicationphamcongthanh.entity.Equipment;
import com.example.projectjavawebapplicationphamcongthanh.exception.CustomValidationException;
import com.example.projectjavawebapplicationphamcongthanh.repository.AcademicEvaluationRepository;
import com.example.projectjavawebapplicationphamcongthanh.repository.BorrowingRecordRepository;
import com.example.projectjavawebapplicationphamcongthanh.repository.EquipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AcademicEvaluationService {

    private final AcademicEvaluationRepository academicEvaluationRepository;
    private final MentoringSessionService mentoringSessionService;
    private final BorrowingRecordRepository borrowingRecordRepository;
    private final EquipmentRepository equipmentRepository;

    public AcademicEvaluationService(AcademicEvaluationRepository academicEvaluationRepository,
                                     MentoringSessionService mentoringSessionService,
                                     BorrowingRecordRepository borrowingRecordRepository,
                                     EquipmentRepository equipmentRepository) {
        this.academicEvaluationRepository = academicEvaluationRepository;
        this.mentoringSessionService = mentoringSessionService;
        this.borrowingRecordRepository = borrowingRecordRepository;
        this.equipmentRepository = equipmentRepository;
    }

    @Transactional
    public AcademicEvaluation evaluateSession(Long sessionId, String comments, Integer score, List<Long> equipmentIds, List<Integer> quantities) {
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

        AcademicEvaluation savedEval = academicEvaluationRepository.save(evaluation);

        // 3. Nếu có chỉ định thiết bị cần cấp phát, tạo phiếu mượn WAITING_FOR_ISSUANCE
        if (equipmentIds != null && !equipmentIds.isEmpty()) {
            // Kiểm tra xem đã có phiếu mượn nào cho session này chưa
            if (borrowingRecordRepository.findBySessionId(sessionId).isPresent()) {
                throw new CustomValidationException("Buổi mentoring này đã được cấp hoặc đang chờ cấp thiết bị!");
            }

            BorrowingRecord record = BorrowingRecord.builder()
                    .session(session)
                    .status("WAITING_FOR_ISSUANCE")
                    .requestDate(LocalDateTime.now())
                    .details(new ArrayList<>())
                    .build();

            for (int i = 0; i < equipmentIds.size(); i++) {
                Long equipId = equipmentIds.get(i);
                Integer qty = (quantities != null && quantities.size() > i) ? quantities.get(i) : null;
                if (equipId == null || qty == null || qty <= 0) {
                    continue;
                }

                Equipment equipment = equipmentRepository.findById(equipId)
                        .orElseThrow(() -> new CustomValidationException("Thiết bị không tồn tại với ID: " + equipId));

                BorrowingDetail detail = BorrowingDetail.builder()
                        .borrowingRecord(record)
                        .equipment(equipment)
                        .quantity(qty)
                        .build();

                record.getDetails().add(detail);
            }

            if (!record.getDetails().isEmpty()) {
                borrowingRecordRepository.save(record);
            }
        }

        return savedEval;
    }

    public Optional<AcademicEvaluation> getBySessionId(Long sessionId) {
        return academicEvaluationRepository.findBySessionId(sessionId);
    }
}
