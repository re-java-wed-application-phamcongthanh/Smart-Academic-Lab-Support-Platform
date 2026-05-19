package com.example.projectjavawebapplicationphamcongthanh.repository;

import com.example.projectjavawebapplicationphamcongthanh.entity.MentoringSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface MentoringSessionRepository extends JpaRepository<MentoringSession, Long> {

    List<MentoringSession> findByStudentId(Long studentId);

    List<MentoringSession> findByLecturerId(Long lecturerId);

    List<MentoringSession> findByStudentIdAndStatus(Long studentId, String status);

    @Query("SELECT COUNT(m) > 0 FROM MentoringSession m " +
           "WHERE m.lecturer.id = :lecturerId " +
           "AND m.date = :date " +
           "AND m.startTime < :endTime " +
           "AND m.endTime > :startTime " +
           "AND m.status NOT IN ('REJECTED', 'CANCELLED')")
    boolean existsOverlappingSession(@Param("lecturerId") Long lecturerId,
                                      @Param("date") LocalDate date,
                                      @Param("startTime") LocalTime startTime,
                                      @Param("endTime") LocalTime endTime);
}
