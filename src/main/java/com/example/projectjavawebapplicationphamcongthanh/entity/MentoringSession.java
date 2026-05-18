package com.example.projectjavawebapplicationphamcongthanh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "mentoring_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentoringSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_id", nullable = false)
    private User lecturer;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    // Trạng thái: PENDING, COMPLETED, CANCELLED
    @Column(nullable = false, length = 20)
    private String status;
}
