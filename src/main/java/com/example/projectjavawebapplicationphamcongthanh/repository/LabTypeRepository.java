package com.example.projectjavawebapplicationphamcongthanh.repository;

import com.example.projectjavawebapplicationphamcongthanh.entity.LabType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LabTypeRepository extends JpaRepository<LabType, Long> {
}
