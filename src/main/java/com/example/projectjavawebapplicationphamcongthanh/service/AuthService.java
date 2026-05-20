package com.example.projectjavawebapplicationphamcongthanh.service;

import com.example.projectjavawebapplicationphamcongthanh.dto.RegisterRequestDTO;
import com.example.projectjavawebapplicationphamcongthanh.entity.Department;
import com.example.projectjavawebapplicationphamcongthanh.entity.Role;
import com.example.projectjavawebapplicationphamcongthanh.entity.User;
import com.example.projectjavawebapplicationphamcongthanh.entity.UserProfile;
import com.example.projectjavawebapplicationphamcongthanh.repository.DepartmentRepository;
import com.example.projectjavawebapplicationphamcongthanh.repository.UserRepository;
import com.example.projectjavawebapplicationphamcongthanh.repository.UserProfileRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileRepository userProfileRepository;

    public AuthService(UserRepository userRepository, 
                       DepartmentRepository departmentRepository, 
                       PasswordEncoder passwordEncoder,
                       UserProfileRepository userProfileRepository) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.userProfileRepository = userProfileRepository;
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

        if (role == Role.STUDENT || role == Role.LECTURER) {
            if (dto.getDepartmentId() == null) {
                throw new RuntimeException("Khoa/Ngành không được bỏ trống!");
            }
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

        // Validate student class if role is STUDENT
        if (role == Role.STUDENT) {
            if (dto.getStudentClass() == null || dto.getStudentClass().trim().isBlank()) {
                throw new RuntimeException("Sinh viên bắt buộc phải điền Lớp sinh hoạt!");
            }
        }

        // Kiểm tra trùng số điện thoại
        if (dto.getPhone() != null && !dto.getPhone().trim().isEmpty()) {
            if (userProfileRepository.existsByPhone(dto.getPhone().trim())) {
                throw new RuntimeException("Số điện thoại này đã được sử dụng!");
            }
        }

        // Tạo UserProfile
        String emailVal = (dto.getEmail() != null && !dto.getEmail().isBlank()) ? dto.getEmail() : dto.getUsername();
        UserProfile profile = UserProfile.builder()
                .fullName(dto.getFullName())
                .email(emailVal)
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .studentClass(role == Role.STUDENT ? dto.getStudentClass().trim() : null)
                .user(user)
                .build();

        user.setProfile(profile);

        // Lưu user (Cascades sẽ tự động lưu UserProfile)
        userRepository.save(user);
    }
}
