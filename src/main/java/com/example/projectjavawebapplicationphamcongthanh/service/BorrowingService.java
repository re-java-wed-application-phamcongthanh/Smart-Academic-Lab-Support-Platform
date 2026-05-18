package com.example.projectjavawebapplicationphamcongthanh.service;

import com.example.projectjavawebapplicationphamcongthanh.dto.BorrowItemDTO;
import com.example.projectjavawebapplicationphamcongthanh.dto.BorrowRequestDTO;
import com.example.projectjavawebapplicationphamcongthanh.entity.BorrowingDetail;
import com.example.projectjavawebapplicationphamcongthanh.entity.BorrowingRecord;
import com.example.projectjavawebapplicationphamcongthanh.entity.Equipment;
import com.example.projectjavawebapplicationphamcongthanh.entity.MentoringSession;
import com.example.projectjavawebapplicationphamcongthanh.exception.CustomValidationException;
import com.example.projectjavawebapplicationphamcongthanh.repository.BorrowingRecordRepository;
import com.example.projectjavawebapplicationphamcongthanh.repository.EquipmentRepository;
import com.example.projectjavawebapplicationphamcongthanh.repository.MentoringSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class BorrowingService {

    private final BorrowingRecordRepository borrowingRecordRepository;
    private final EquipmentRepository equipmentRepository;
    private final MentoringSessionRepository mentoringSessionRepository;

    public BorrowingService(BorrowingRecordRepository borrowingRecordRepository,
                            EquipmentRepository equipmentRepository,
                            MentoringSessionRepository mentoringSessionRepository) {
        this.borrowingRecordRepository = borrowingRecordRepository;
        this.equipmentRepository = equipmentRepository;
        this.mentoringSessionRepository = mentoringSessionRepository;
    }

    public List<BorrowingRecord> getAll() {
        return borrowingRecordRepository.findAll();
    }

    public List<BorrowingRecord> getByStudent(Long studentId) {
        return borrowingRecordRepository.findBySessionStudentId(studentId);
    }

    public List<BorrowingRecord> getByLecturer(Long lecturerId) {
        return borrowingRecordRepository.findBySessionLecturerId(lecturerId);
    }

    public BorrowingRecord getById(Long id) {
        return borrowingRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu mượn với ID: " + id));
    }

    @Transactional
    public BorrowingRecord createBorrowing(BorrowRequestDTO dto) {
        MentoringSession session = mentoringSessionRepository.findById(dto.getSessionId())
                .orElseThrow(() -> new CustomValidationException("Buổi mentoring không tồn tại!"));

        // Kiểm tra xem buổi mentoring này đã có phiếu mượn hay chưa
        if (borrowingRecordRepository.findBySessionId(dto.getSessionId()).isPresent()) {
            throw new CustomValidationException("Buổi mentoring này đã được đăng ký mượn thiết bị rồi!");
        }

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new CustomValidationException("Danh sách thiết bị mượn không được để trống!");
        }

        // Tạo phiếu mượn mới
        BorrowingRecord record = BorrowingRecord.builder()
                .session(session)
                .status("APPROVED") // Trực tiếp phê duyệt và trừ kho
                .requestDate(LocalDateTime.now())
                .issueDate(LocalDateTime.now())
                .details(new ArrayList<>())
                .build();

        for (BorrowItemDTO itemDto : dto.getItems()) {
            Equipment equipment = equipmentRepository.findById(itemDto.getEquipmentId())
                    .orElseThrow(() -> new CustomValidationException("Thiết bị không tồn tại với ID: " + itemDto.getEquipmentId()));

            // Kiểm tra tồn kho khả dụng
            if (equipment.getAvailableQuantity() < itemDto.getQuantity()) {
                throw new CustomValidationException("Thiết bị [" + equipment.getName() + "] không đủ số lượng trong kho! (Còn lại: "
                        + equipment.getAvailableQuantity() + ", Cần mượn: " + itemDto.getQuantity() + ")");
            }

            // Trừ trực tiếp số lượng khả dụng
            equipment.setAvailableQuantity(equipment.getAvailableQuantity() - itemDto.getQuantity());
            equipmentRepository.save(equipment);

            // Thêm chi tiết phiếu mượn
            BorrowingDetail detail = BorrowingDetail.builder()
                    .borrowingRecord(record)
                    .equipment(equipment)
                    .quantity(itemDto.getQuantity())
                    .build();

            record.getDetails().add(detail);
        }

        return borrowingRecordRepository.save(record);
    }

    @Transactional
    public void returnEquipment(Long recordId) {
        BorrowingRecord record = getById(recordId);

        if ("RETURNED".equals(record.getStatus())) {
            throw new CustomValidationException("Phiếu mượn này đã được hoàn trả trước đó!");
        }

        // Cộng trả lại số lượng mượn vào kho
        for (BorrowingDetail detail : record.getDetails()) {
            Equipment equipment = detail.getEquipment();
            equipment.setAvailableQuantity(equipment.getAvailableQuantity() + detail.getQuantity());
            equipmentRepository.save(equipment);
        }

        record.setStatus("RETURNED");
        record.setReturnDate(LocalDateTime.now());
        borrowingRecordRepository.save(record);
    }
}
