package com.example.projectjavawebapplicationphamcongthanh.controller;

import com.example.projectjavawebapplicationphamcongthanh.entity.Equipment;
import com.example.projectjavawebapplicationphamcongthanh.service.EquipmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/equipments")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("equipments", equipmentService.getAll());
        return "admin/equipment-list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("equipment", new Equipment());
        return "admin/equipment-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("equipment") Equipment equipment, Model model) {
        try {
            equipmentService.save(equipment);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "admin/equipment-form";
        }
        return "redirect:/admin/equipments";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        try {
            model.addAttribute("equipment", equipmentService.getById(id));
        } catch (RuntimeException e) {
            return "redirect:/admin/equipments?error=" + e.getMessage();
        }
        return "admin/equipment-form";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable("id") Long id, @ModelAttribute("equipment") Equipment equipment, Model model) {
        try {
            equipment.setId(id);
            equipmentService.save(equipment);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "admin/equipment-form";
        }
        return "redirect:/admin/equipments";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        try {
            equipmentService.delete(id);
        } catch (RuntimeException e) {
            return "redirect:/admin/equipments?error=" + e.getMessage();
        }
        return "redirect:/admin/equipments";
    }
}
