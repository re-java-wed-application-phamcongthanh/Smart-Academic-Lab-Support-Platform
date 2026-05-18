package com.example.projectjavawebapplicationphamcongthanh.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "borrowing_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowingDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrowing_record_id", nullable = false)
    private BorrowingRecord borrowingRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(nullable = false)
    private Integer quantity;
}
