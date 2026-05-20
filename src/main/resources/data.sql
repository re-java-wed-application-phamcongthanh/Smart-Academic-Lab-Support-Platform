SET FOREIGN_KEY_CHECKS = 0;

-- Xóa các bảng cũ nếu đã tồn tại để tránh xung đột cấu trúc cột (ví dụ department_id ở bảng users)
DROP TABLE IF EXISTS borrowing_details, borrowing_records, academic_evaluations, mentoring_sessions, user_profiles, users, equipments, lab_types, departments;

-- 1. Tạo các bảng (DDL)
CREATE TABLE IF NOT EXISTS departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS lab_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS equipments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    total_quantity INT NOT NULL,
    available_quantity INT NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    department_id BIGINT,
    FOREIGN KEY (department_id) REFERENCES departments(id)
);

CREATE TABLE IF NOT EXISTS user_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(15),
    address VARCHAR(255),
    student_class VARCHAR(50),
    user_id BIGINT NOT NULL UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS mentoring_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    lecturer_id BIGINT NOT NULL,
    date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL,
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (lecturer_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS academic_evaluations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL UNIQUE,
    comments TEXT,
    score INT,
    FOREIGN KEY (session_id) REFERENCES mentoring_sessions(id)
);

CREATE TABLE IF NOT EXISTS borrowing_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    request_date DATETIME NOT NULL,
    issue_date DATETIME,
    return_date DATETIME,
    FOREIGN KEY (session_id) REFERENCES mentoring_sessions(id)
);

CREATE TABLE IF NOT EXISTS borrowing_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    borrowing_record_id BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    FOREIGN KEY (borrowing_record_id) REFERENCES borrowing_records(id),
    FOREIGN KEY (equipment_id) REFERENCES equipments(id)
);

-- Xóa dữ liệu cũ (để chạy script nhiều lần không bị lỗi)
DELETE FROM borrowing_details;
DELETE FROM borrowing_records;
DELETE FROM academic_evaluations;
DELETE FROM mentoring_sessions;
DELETE FROM user_profiles;
DELETE FROM users;
DELETE FROM equipments;
DELETE FROM lab_types;
DELETE FROM departments;

ALTER TABLE borrowing_details AUTO_INCREMENT = 1;
ALTER TABLE borrowing_records AUTO_INCREMENT = 1;
ALTER TABLE academic_evaluations AUTO_INCREMENT = 1;
ALTER TABLE mentoring_sessions AUTO_INCREMENT = 1;
ALTER TABLE user_profiles AUTO_INCREMENT = 1;
ALTER TABLE users AUTO_INCREMENT = 1;
ALTER TABLE equipments AUTO_INCREMENT = 1;
ALTER TABLE lab_types AUTO_INCREMENT = 1;
ALTER TABLE departments AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;

-- 2. Thêm dữ liệu (DML)
-- Thêm Khoa/Ngành (Dữ liệu nền)
INSERT INTO departments (name) VALUES 
('Công nghệ Thông tin'), 
('Điện tử Viễn thông'), 
('Khoa học Dữ liệu');

-- Thêm Loại phòng Lab (Dữ liệu nền)
INSERT INTO lab_types (name) VALUES 
('Phòng Thực hành Mạng'), 
('Phòng Phần cứng'), 
('Phòng Lập trình Cao cấp');

-- Thêm Thiết bị (Total và Available)
INSERT INTO equipments (name, total_quantity, available_quantity) VALUES 
('Laptop Dell Precision', 20, 20),
('Raspberry Pi 4', 50, 50),
('Kính VR Oculus Rift', 10, 10),
('Mạch Arduino Uno', 100, 100),
('Oscilloscope', 5, 5);

-- Thêm User: (Mật khẩu được băm sẵn bằng BCrypt cho chữ '123456')
INSERT INTO users (username, password, role, department_id) VALUES 
('phamcongt56@gmail.com', '$2a$10$Bes5I3mr.JZeyMqar9LyZeLSzPZmtWKGgynloHFu9xwE50kCw8La.', 'ADMIN', NULL),
('admin1', '$2a$10$Bes5I3mr.JZeyMqar9LyZeLSzPZmtWKGgynloHFu9xwE50kCw8La.', 'ADMIN', NULL),
('lecturer1', '$2a$10$Bes5I3mr.JZeyMqar9LyZeLSzPZmtWKGgynloHFu9xwE50kCw8La.', 'LECTURER', 1),
('lecturer2', '$2a$10$Bes5I3mr.JZeyMqar9LyZeLSzPZmtWKGgynloHFu9xwE50kCw8La.', 'LECTURER', 2),
('student1', '$2a$10$Bes5I3mr.JZeyMqar9LyZeLSzPZmtWKGgynloHFu9xwE50kCw8La.', 'STUDENT', 1),
('student2', '$2a$10$Bes5I3mr.JZeyMqar9LyZeLSzPZmtWKGgynloHFu9xwE50kCw8La.', 'STUDENT', 2);

-- Thêm Hồ sơ User Profile
INSERT INTO user_profiles (full_name, email, phone, address, student_class, user_id) VALUES 
('Quản trị viên Hệ thống', 'phamcongt56@gmail.com', '0123456789', 'Hà Nội', NULL, 1),
('Quản trị viên Hệ thống', 'admin@smartplatform.com', '0123456789', 'Hà Nội', NULL, 2),
('Giảng viên Nguyễn Văn A', 'lecturer1@smartplatform.com', '0987654321', 'Hà Nội', NULL, 3),
('Giảng viên Trần Thị B', 'lecturer2@smartplatform.com', '0987654322', 'TP.HCM', NULL, 4),
('Sinh viên Phạm Công Thành', 'student1@smartplatform.com', '0345678901', 'Đà Nẵng', 'D19_CNPM01', 5),
('Sinh viên Lê Văn D', 'student2@smartplatform.com', '0345678902', 'Cần Thơ', 'D19_CNPM02', 6);

-- Thêm Lịch hẹn (Mentoring Session)
INSERT INTO mentoring_sessions (student_id, lecturer_id, date, start_time, end_time, status) VALUES 
(5, 3, '2026-06-01', '08:00:00', '09:00:00', 'COMPLETED'),
(6, 4, '2026-06-02', '09:00:00', '10:00:00', 'PENDING');

-- Thêm Đánh giá Học thuật
INSERT INTO academic_evaluations (session_id, comments, score) VALUES 
(1, 'Sinh viên hoàn thành tốt phần thiết kế hệ thống, cần cải thiện thêm kỹ năng viết code', 9);

-- Thêm Phiếu mượn
INSERT INTO borrowing_records (session_id, status, request_date, issue_date) VALUES 
(1, 'APPROVED', '2026-06-01 09:00:00', '2026-06-01 09:10:00');

-- Thêm Chi tiết Phiếu mượn (Giảm số lượng available trong thực tế)
INSERT INTO borrowing_details (borrowing_record_id, equipment_id, quantity) VALUES 
(1, 1, 1), -- Mượn 1 Laptop
(1, 4, 2); -- Mượn 2 Mạch Arduino
