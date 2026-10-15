# Spring Boot User CRUD

Backend dùng endpoint `/api/users` và response wrapper `code`, `message`, `result`.

- `GET /api/users?page=0&size=10`: danh sách phân trang, sắp xếp `updateAt` giảm dần.
- `POST /api/users`: tạo user, validate name/email/role/status và không cho email trùng.
- `PUT /api/users/{id}`: cập nhật name/role/status, email không được thay đổi.
- `DELETE /api/users/{id}`: xóa user.

Swagger UI theo cấu hình tài liệu: `http://localhost:8080/swagger-ui/index.html#/`.
