package com.example.projectjavawebapplicationphamcongthanh.repository;

import com.example.projectjavawebapplicationphamcongthanh.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
