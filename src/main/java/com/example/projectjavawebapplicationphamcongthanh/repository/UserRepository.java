package com.example.projectjavawebapplicationphamcongthanh.repository;

import com.example.projectjavawebapplicationphamcongthanh.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.projectjavawebapplicationphamcongthanh.dto.AcademicProfileDTO;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    java.util.List<User> findByRole(com.example.projectjavawebapplicationphamcongthanh.entity.Role role);

    @Query("SELECT new com.example.projectjavawebapplicationphamcongthanh.dto.AcademicProfileDTO(" +
           "p.fullName, p.phone, d.name, " +
           "COUNT(m), AVG(CAST(e.score as double))) " +
           "FROM User u " +
           "JOIN u.profile p " +
           "LEFT JOIN u.department d " +
           "LEFT JOIN MentoringSession m ON m.student.id = u.id AND m.status = 'COMPLETED' " +
           "LEFT JOIN AcademicEvaluation e ON e.session.id = m.id " +
           "WHERE u.id = :studentId " +
           "GROUP BY p.fullName, p.phone, d.name")
    AcademicProfileDTO getAcademicProfile(@Param("studentId") Long studentId);
}
