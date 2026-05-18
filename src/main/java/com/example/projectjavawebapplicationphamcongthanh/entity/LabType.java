package com.example.projectjavawebapplicationphamcongthanh.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lab_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;
}
