# Nền tảng Hỗ trợ Học thuật & Quản lý Thiết bị Thực hành (Smart Academic & Lab Support Platform)

Dự án Java Web Application xây dựng một hệ thống quản lý đặt lịch cố vấn học tập và cấp phát thiết bị thực hành.

## Cấu trúc dự án
Kiến trúc: Monolithic, tổ chức theo chuẩn 3-layer (Controller - Service - Repository).
- **Backend:** Java, Spring Boot, Spring Data JPA, Spring Security.
- **Database:** MySQL/PostgreSQL.
- **Frontend (Dự kiến):** Thymeleaf / HTML, CSS, JS.

## Kế hoạch triển khai (4 Sprints)

### Sprint 1: Xây dựng Nền tảng (Entities & Security)
**Mục tiêu:** Setup xong Database, các class model và luồng Đăng nhập/Phân quyền (CORE-01, 02, 03, 04).

- **Task 1.1: Ánh xạ Database (Tạo các class trong package `entity`)**
  - Tạo `User` và `UserProfile` (@OneToOne).
  - Tạo `Department`, `LabType` (Dữ liệu nền).
  - Tạo `Equipment` (id, name, totalQuantity, availableQuantity).
  - Tạo `MentoringSession` (id, studentId, lecturerId, date, startTime, endTime, status).
  - Tạo `BorrowingRecord` và `BorrowingDetail` (@OneToMany và @ManyToOne).
- **Task 1.2: Khởi tạo Data Access (`repository`)**
  - Tạo các interface JpaRepository.
- **Task 1.3: Thiết lập Spring Security (`config`)**
  - Phân quyền theo Role: ADMIN, LECTURER, STUDENT.
- **Task 1.4: Code luồng Xác thực & Danh mục (`controller`, `service`, `dto`)**
  - Login/Register API.
  - CRUD Equipment cho Admin.

### Sprint 2: Core Logic - Xử lý Xung đột Tài nguyên
**Mục tiêu:** Giải quyết bài toán Đặt lịch và Hủy lịch sao cho không bao giờ bị trùng (CORE-05, CORE-09).

- **Task 2.1: Luồng Đặt lịch Cố vấn**
  - Xử lý check trùng lặp lịch bằng custom query.
  - Lưu lịch trạng thái `PENDING`.
- **Task 2.2: Luồng Hủy lịch & Giải phóng Slot**
  - Hủy lịch trước 24h và cập nhật trạng thái `CANCELLED`.

### Sprint 3: Quản lý Transaction & Tồn kho
**Mục tiêu:** Đảm bảo toàn vẹn dữ liệu khi ghi vào nhiều bảng cùng lúc (CORE-06, CORE-08).

- **Task 3.1: Giảng viên Đánh giá & Chỉ định thiết bị**
  - Cập nhật session, lưu evaluation, tạo phiếu mượn (được quản lý bởi `@Transactional`).
- **Task 3.2: Admin Xác nhận Xuất kho thiết bị**
  - Trừ lùi số lượng thiết bị (`availableQuantity`) nếu đủ số lượng, ngược lại rollback.

### Sprint 4: Truy vấn Phức tạp, UI & Nâng cao
**Mục tiêu:** Hoàn thiện giao diện, thống kê và các tính năng mở rộng (CORE-07, Mở rộng).

- **Task 4.1: Tra cứu Hồ sơ Học thuật**
  - Truy vấn JOIN trả về thông tin chi tiết các lịch học tập.
- **Task 4.2: Xử lý Lỗi Toàn cục (Global Exception Handler)**
  - Quản lý trả lỗi chuẩn bằng `@ControllerAdvice`.
- **Task 4.3: Giao diện Thymeleaf & Tính năng ngầm (Mở rộng)**
  - Xây dựng giao diện.
  - `EmailService` với `@Async`.
  - Cron Job với `@EnableScheduling`.

---
*README này đóng vai trò như một bản đặc tả (SRS) và checklist công việc để theo dõi tiến độ của dự án.*
