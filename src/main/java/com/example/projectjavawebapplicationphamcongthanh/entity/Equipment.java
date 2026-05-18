package com.example.projectjavawebapplicationphamcongthanh.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "equipments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false)
    private Integer totalQuantity;

    @Column(nullable = false)
    private Integer availableQuantity;
}
