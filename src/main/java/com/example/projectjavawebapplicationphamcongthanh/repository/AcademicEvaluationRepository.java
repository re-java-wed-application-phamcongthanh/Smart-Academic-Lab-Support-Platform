package com.example.projectjavawebapplicationphamcongthanh.repository;

import com.example.projectjavawebapplicationphamcongthanh.entity.AcademicEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AcademicEvaluationRepository extends JpaRepository<AcademicEvaluation, Long> {
    java.util.Optional<AcademicEvaluation> findBySessionId(Long sessionId);
}
