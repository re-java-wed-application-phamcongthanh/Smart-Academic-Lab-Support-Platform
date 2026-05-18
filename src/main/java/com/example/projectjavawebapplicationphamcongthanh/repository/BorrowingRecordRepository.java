package com.example.projectjavawebapplicationphamcongthanh.repository;

import com.example.projectjavawebapplicationphamcongthanh.entity.BorrowingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BorrowingRecordRepository extends JpaRepository<BorrowingRecord, Long> {
    java.util.Optional<BorrowingRecord> findBySessionId(Long sessionId);
    java.util.List<BorrowingRecord> findBySessionStudentId(Long studentId);
    java.util.List<BorrowingRecord> findBySessionLecturerId(Long lecturerId);
}
