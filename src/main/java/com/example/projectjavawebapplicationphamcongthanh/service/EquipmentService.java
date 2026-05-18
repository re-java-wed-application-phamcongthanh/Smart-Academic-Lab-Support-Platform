package com.example.projectjavawebapplicationphamcongthanh.service;

import com.example.projectjavawebapplicationphamcongthanh.entity.Equipment;
import com.example.projectjavawebapplicationphamcongthanh.repository.EquipmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

    public EquipmentService(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    public List<Equipment> getAll() {
        return equipmentRepository.findAll();
    }

    public Equipment getById(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thiết bị với ID: " + id));
    }

    public Equipment save(Equipment equipment) {
        // Có thể bổ sung validation logic ở đây
        if (equipment.getTotalQuantity() < 0 || equipment.getAvailableQuantity() < 0) {
            throw new RuntimeException("Số lượng không thể nhỏ hơn 0!");
        }
        if (equipment.getAvailableQuantity() > equipment.getTotalQuantity()) {
            throw new RuntimeException("Số lượng khả dụng không thể lớn hơn tổng số lượng!");
        }
        return equipmentRepository.save(equipment);
    }

    public void delete(Long id) {
        if (!equipmentRepository.existsById(id)) {
            throw new RuntimeException("Thiết bị không tồn tại!");
        }
        equipmentRepository.deleteById(id);
    }
}
