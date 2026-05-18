package com.example.projectjavawebapplicationphamcongthanh.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "academic_evaluations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicEvaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private MentoringSession session;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column
    private Integer score;
}
