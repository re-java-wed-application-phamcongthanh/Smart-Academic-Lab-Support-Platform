package com.example.projectjavawebapplicationphamcongthanh.service;

import com.example.projectjavawebapplicationphamcongthanh.dto.RegisterRequestDTO;
import com.example.projectjavawebapplicationphamcongthanh.entity.Department;
import com.example.projectjavawebapplicationphamcongthanh.entity.Role;
import com.example.projectjavawebapplicationphamcongthanh.entity.User;
import com.example.projectjavawebapplicationphamcongthanh.entity.UserProfile;
import com.example.projectjavawebapplicationphamcongthanh.repository.DepartmentRepository;
import com.example.projectjavawebapplicationphamcongthanh.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, DepartmentRepository departmentRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(RegisterRequestDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }

        Role role;
        try {
            role = Role.valueOf(dto.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Vai trò không hợp lệ!");
        }

        if (role == Role.ADMIN) {
            throw new RuntimeException("Không được phép đăng ký tài khoản Quản trị viên (Admin)!");
        }

        Department department = null;
        if (dto.getDepartmentId() != null) {
            department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Khoa/Ngành không tồn tại!"));
        }

        // Tạo User
        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(role)
                .department(department)
                .build();

        // Tạo UserProfile
        String emailVal = (dto.getEmail() != null && !dto.getEmail().isBlank()) ? dto.getEmail() : dto.getUsername();
        UserProfile profile = UserProfile.builder()
                .fullName(dto.getFullName())
                .email(emailVal)
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .user(user)
                .build();

        user.setProfile(profile);

        // Lưu user (Cascades sẽ tự động lưu UserProfile)
        userRepository.save(user);
    }
}
