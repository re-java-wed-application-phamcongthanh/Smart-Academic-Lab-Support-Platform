package com.example.projectjavawebapplicationphamcongthanh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "borrowing_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowingRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private MentoringSession session;

    // Trạng thái: WAITING_FOR_ISSUANCE, ISSUED, RETURNED
    @Column(nullable = false, length = 30)
    private String status;

    @Column(nullable = false)
    private LocalDateTime requestDate;

    @Column
    private LocalDateTime issueDate;

    @Column
    private LocalDateTime returnDate;

    @OneToMany(mappedBy = "borrowingRecord", cascade = CascadeType.ALL)
    private List<BorrowingDetail> details;
}
