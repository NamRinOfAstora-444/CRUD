# Angular User CRUD

Frontend theo tài liệu CRUD Angular, gọi backend Spring Boot tại `http://localhost:8080/api/users`.

## Chạy frontend
1. Cài Node.js phù hợp với Angular CLI của project.
2. Chạy `npm install`.
3. Chạy `npm start`.

## API được sử dụng
- `GET /api/users?page=0&size=10`
- `POST /api/users`
- `PUT /api/users/{id}`
- `DELETE /api/users/{id}`

Response được đọc theo wrapper `code`, `message`, `result`; danh sách nằm trong `result.content` và tổng bản ghi ở `result.totalElements`.
