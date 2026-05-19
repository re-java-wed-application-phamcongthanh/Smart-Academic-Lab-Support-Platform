package com.example.projectjavawebapplicationphamcongthanh.controller;

import com.example.projectjavawebapplicationphamcongthanh.entity.Equipment;
import com.example.projectjavawebapplicationphamcongthanh.service.EquipmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.ModelAndView;

@Controller
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @GetMapping("/admin/equipments")
    public ModelAndView list() {
        System.out.println("==========================================");
        System.out.println(">>> ENTERED EquipmentController.list() with ModelAndView");
        System.out.println("==========================================");
        ModelAndView mav = new ModelAndView("admin/equipment-list");
        try {
            java.util.List<com.example.projectjavawebapplicationphamcongthanh.entity.Equipment> list = equipmentService.getAll();
            System.out.println(">>> List retrieved: " + list);
            if (list == null) {
                System.out.println(">>> Warning: list is null! Initializing to empty list.");
                list = new java.util.ArrayList<>();
            } else {
                System.out.println(">>> List size: " + list.size());
            }
            mav.addObject("equipments", list);
        } catch (Exception e) {
            System.out.println(">>> Exception in list(): " + e.getMessage());
            e.printStackTrace();
            mav.addObject("equipments", new java.util.ArrayList<>());
        }
        return mav;
    }


    @GetMapping("/admin/equipments/new")
    public String showCreateForm(Model model) {
        model.addAttribute("equipment", new Equipment());
        return "admin/equipment-form";
    }

    @PostMapping("/admin/equipments/save")
    public String save(@ModelAttribute("equipment") Equipment equipment, Model model) {
        try {
            equipmentService.save(equipment);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "admin/equipment-form";
        }
        return "redirect:/admin/equipments";
    }

    @GetMapping("/admin/equipments/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        try {
            model.addAttribute("equipment", equipmentService.getById(id));
        } catch (RuntimeException e) {
            return "redirect:/admin/equipments?error=" + e.getMessage();
        }
        return "admin/equipment-form";
    }

    @PostMapping("/admin/equipments/update/{id}")
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

    @GetMapping("/admin/equipments/delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        try {
            equipmentService.delete(id);
        } catch (RuntimeException e) {
            return "redirect:/admin/equipments?error=" + e.getMessage();
        }
        return "redirect:/admin/equipments";
    }
}
