package com.example.projectjavawebapplicationphamcongthanh.controller;

import com.example.projectjavawebapplicationphamcongthanh.dto.RegisterRequestDTO;
import com.example.projectjavawebapplicationphamcongthanh.repository.DepartmentRepository;
import com.example.projectjavawebapplicationphamcongthanh.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final AuthService authService;
    private final DepartmentRepository departmentRepository;

    public AuthController(AuthService authService, DepartmentRepository departmentRepository) {
        this.authService = authService;
        this.departmentRepository = departmentRepository;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new RegisterRequestDTO());
        model.addAttribute("departments", departmentRepository.findAll());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") RegisterRequestDTO dto,
                               BindingResult result,
                               Model model) {
        if (result.hasErrors()) {
            model.addAttribute("error", "Đăng ký không thành công. Vui lòng kiểm tra và sửa các lỗi nhập liệu bên dưới!");
            model.addAttribute("departments", departmentRepository.findAll());
            return "register";
        }
        try {
            authService.register(dto);
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if (msg.contains("Số điện thoại")) {
                result.rejectValue("phone", "error.phone", msg);
            } else if (msg.contains("Tên đăng nhập")) {
                result.rejectValue("username", "error.username", msg);
            } else if (msg.contains("Lớp sinh hoạt")) {
                result.rejectValue("studentClass", "error.studentClass", msg);
            } else if (msg.contains("Khoa/Ngành")) {
                result.rejectValue("departmentId", "error.departmentId", msg);
            } else {
                model.addAttribute("error", msg);
            }
            model.addAttribute("departments", departmentRepository.findAll());
            return "register";
        }
        return "redirect:/login?registered";
    }
}
