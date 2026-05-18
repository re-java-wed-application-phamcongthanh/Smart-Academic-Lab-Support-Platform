package com.example.projectjavawebapplicationphamcongthanh.service;

import com.example.projectjavawebapplicationphamcongthanh.dto.BookingRequestDTO;
import com.example.projectjavawebapplicationphamcongthanh.entity.MentoringSession;
import com.example.projectjavawebapplicationphamcongthanh.entity.User;
import com.example.projectjavawebapplicationphamcongthanh.exception.CustomValidationException;
import com.example.projectjavawebapplicationphamcongthanh.repository.MentoringSessionRepository;
import com.example.projectjavawebapplicationphamcongthanh.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MentoringSessionService {

    private final MentoringSessionRepository mentoringSessionRepository;
    private final UserRepository userRepository;

    public MentoringSessionService(MentoringSessionRepository mentoringSessionRepository, UserRepository userRepository) {
        this.mentoringSessionRepository = mentoringSessionRepository;
        this.userRepository = userRepository;
    }

    public List<MentoringSession> getAll() {
        return mentoringSessionRepository.findAll();
    }

    public List<MentoringSession> getByStudent(Long studentId) {
        return mentoringSessionRepository.findByStudentId(studentId);
    }

    public List<MentoringSession> getByLecturer(Long lecturerId) {
        return mentoringSessionRepository.findByLecturerId(lecturerId);
    }

    public MentoringSession getById(Long id) {
        return mentoringSessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn với ID: " + id));
    }

    @Transactional
    public MentoringSession createSession(Long studentId, BookingRequestDTO dto) {
        // 1. Kiểm tra điều kiện 24 giờ
        LocalDateTime sessionStart = LocalDateTime.of(dto.getDate(), dto.getStartTime());
        if (sessionStart.isBefore(LocalDateTime.now().plusHours(24))) {
            throw new CustomValidationException("Bạn chỉ được đặt lịch hẹn trước tối thiểu 24 giờ!");
        }

        // 2. Kiểm tra giờ bắt đầu phải trước giờ kết thúc
        if (dto.getStartTime().isAfter(dto.getEndTime()) || dto.getStartTime().equals(dto.getEndTime())) {
            throw new CustomValidationException("Thời gian bắt đầu phải trước thời gian kết thúc!");
        }

        // 3. Kiểm tra giảng viên tồn tại
        User lecturer = userRepository.findById(dto.getLecturerId())
                .orElseThrow(() -> new CustomValidationException("Giảng viên không tồn tại!"));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new CustomValidationException("Sinh viên không tồn tại!"));

        // 4. Kiểm tra trùng lịch của giảng viên
        boolean overlap = mentoringSessionRepository.existsOverlappingSession(
                dto.getLecturerId(),
                dto.getDate(),
                dto.getStartTime(),
                dto.getEndTime()
        );
        if (overlap) {
            throw new CustomValidationException("Giảng viên đã có lịch hẹn khác trùng khớp với khoảng thời gian này!");
        }

        MentoringSession session = MentoringSession.builder()
                .student(student)
                .lecturer(lecturer)
                .date(dto.getDate())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .status("PENDING")
                .build();

        return mentoringSessionRepository.save(session);
    }

    @Transactional
    public void approveSession(Long id) {
        MentoringSession session = getById(id);
        if (!"PENDING".equals(session.getStatus())) {
            throw new CustomValidationException("Chỉ có thể duyệt lịch hẹn đang ở trạng thái PENDING!");
        }
        session.setStatus("APPROVED");
        mentoringSessionRepository.save(session);
    }

    @Transactional
    public void rejectSession(Long id) {
        MentoringSession session = getById(id);
        if (!"PENDING".equals(session.getStatus()) && !"APPROVED".equals(session.getStatus())) {
            throw new CustomValidationException("Không thể từ chối lịch hẹn đã hoàn thành hoặc đã hủy!");
        }
        session.setStatus("REJECTED");
        mentoringSessionRepository.save(session);
    }

    @Transactional
    public void completeSession(Long id) {
        MentoringSession session = getById(id);
        if (!"APPROVED".equals(session.getStatus())) {
            throw new CustomValidationException("Chỉ lịch hẹn đã duyệt (APPROVED) mới có thể hoàn thành (COMPLETED)!");
        }
        session.setStatus("COMPLETED");
        mentoringSessionRepository.save(session);
    }
}
