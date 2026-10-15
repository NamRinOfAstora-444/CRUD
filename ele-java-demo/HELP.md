# ele-java-demo

## Công nghệ sử dụng

### Core Framework
- **Spring Boot** (4.0.4) - Framework chính để xây dựng REST API
- **Java** (21) - Ngôn ngữ lập trình
- **Maven** (3.8+) - Build tool và dependency management

### Database & ORM
- **Spring Data JPA** - ORM framework
- **MySQL** (8.0+) - Relational database
- **Liquibase** - Database migration và version control

### API Documentation
- **Springdoc OpenAPI** (2.7.0) - Tự động generate API documentation
- **Swagger UI** - Interactive API explorer

### Validation & Security
- **Spring Boot Validation** - Bean Validation (Jakarta Validation API)
- **CORS Configuration** - Whitelist-based với cấu hình linh hoạt

### Code Generation
- **EnumToTypeScriptGenerator** - Tự động generate TypeScript enums từ Java enums
- **ControllerToTypeScriptAngularApiGenerator** - Generate Angular API service (HttpClient, Observable)
- **ControllerToTypeScriptVueApiGenerator** - Generate Vue API functions (axios, async/await)
- **ControllerToTypeScriptReactApiGenerator** - Generate React API functions (axios, plain promise)

## Scripts

- `./mvnw spring-boot:run` - Khởi động server phát triển trên http://localhost:8080
- `./mvnw clean compile` - Build và generate TypeScript enums
- `./mvnw clean package` - Build file JAR cho production
- `./mvnw process-classes` - Chạy TypeScript generator thủ công
- `./mvnw test` - Chạy unit tests

## Cấu trúc thư mục

```
src/main/java/com/ele/javademo/
├── controllers/                     # Controller Layer - API endpoints
│   └── UserController.java          # REST API cho User (GET all, POST create)

├── services/                        # Service Layer - Business logic
│   ├── IUserService.java            # Interface định nghĩa contract
│   └── UserService.java             # Implementation với @Transactional

├── repositories/                    # Repository Layer - Database access
│   └── UserRepository.java          # JpaRepository cho User entity

├── entities/                        # Entity Layer - Database models
│   └── User.java                    # JPA entity map với table users

├── dto/                             # Data Transfer Objects
│   ├── ApiResponse.java             # Generic wrapper cho tất cả API responses
│   ├── PagedResponse.java           # Wrapper cho paginated data
│   └── user/
│       ├── CreateUserRequest.java   # Input DTO với validation
│       └── UserResponse.java        # Output DTO với static factory method

├── business_code/                   # Business Code System
│   ├── CommonCode.java              # Enum tổng hợp chứa tất cả codes
│   └── UserErrorCode.java           # User-specific error codes

├── configs/                         # Configuration classes
│   ├── CorsConfig.java              # CORS configuration với whitelist
│   ├── OpenApiConfig.java           # Swagger/OpenAPI configuration
│   └── swagger/user/                # Custom Swagger annotations
│       ├── ApiResponseSuccess.java  # Annotation cho 200 response
│       └── ApiResponseCreated.java  # Annotation cho 201 response

├── code_gen/                        # Code Generation
│   ├── EnumToTypeScriptGenerator.java               # Auto-generate TS enums từ Java enums
│   ├── ControllerToTypeScriptAngularApiGenerator.java # Generate Angular API services
│   ├── ControllerToTypeScriptVueApiGenerator.java     # Generate Vue API functions
│   └── ControllerToTypeScriptReactApiGenerator.java   # Generate React API functions

├── exceptions/                      # Exception handling
│   └── GlobalExceptionHandler.java  # Global exception handler với @ControllerAdvice

└── EleJavaDemoApplication.java      # Main class (@SpringBootApplication)

src/main/resources/
├── application.properties           # Cấu hình ứng dụng (DB, server, CORS)
├── db/changelog/                    # Liquibase migration files
│   └── db.changelog-master.xml      # Master changelog
├── enumBusinessCode/                # Generated TypeScript files (gitignored)
│   └── business-codes.ts            # Auto-generated từ Java enums
├── api-angular/                     # Generated Angular API (gitignored)
│   └── <kebab-name>/
│       ├── <kebab-name>.interface.ts # TypeScript interfaces (prefix I)
│       └── <kebab-name>.api.ts       # Angular @Injectable service (HttpClient)
├── api-vue/                         # Generated Vue API (gitignored)
│   └── <kebab-name>/
│       ├── <kebab-name>.interface.ts # TypeScript interfaces (prefix I)
│       └── <kebab-name>.api.ts       # Vue async functions (axios/httpContext)
└── api-react/                       # Generated React API (gitignored)
    └── <kebab-name>/
        ├── <kebab-name>.interface.ts # TypeScript interfaces (prefix I)
        └── <kebab-name>.api.ts       # React plain functions (axios/httpContext)
```

## Mô tả chức năng

### 1. Kiến trúc 3 lớp (3-Layer Architecture)

```
Controller Layer (API)
    ↓
Service Layer (Business Logic)
    ↓
Repository Layer (Data Access)
    ↓
Entity (Database Model)
```

#### Controller Layer
- **Nhiệm vụ**: Nhận HTTP requests, validate input, gọi Service layer, trả về responses
- **Annotations**: `@RestController`, `@RequestMapping`, `@Valid`, `@Operation`
- **Ví dụ**: UserController xử lý GET all users (paginated) và POST create user

#### Service Layer
- **Nhiệm vụ**: Chứa business logic, xử lý dữ liệu, gọi Repository layer
- **Annotations**: `@Service`, `@Transactional` (auto commit/rollback)
- **Pattern**: Interface + Implementation để dễ testing và mở rộng
- **Transactional**:
  - `@Transactional` - Auto commit nếu thành công, rollback nếu exception
  - `@Transactional(readOnly = true)` - Tối ưu cho operations chỉ đọc

#### Repository Layer
- **Nhiệm vụ**: Tương tác trực tiếp với database
- **Technology**: Spring Data JPA
- **Đặc điểm**: Extend `JpaRepository<Entity, ID>` để có sẵn CRUD methods, pagination, sorting

#### DTO Pattern
- **Request DTOs**: Validate input từ client với Bean Validation annotations
- **Response DTOs**: Control output data, tách biệt Entity khỏi API contract
- **Mapping**: Static factory methods (VD: `UserResponse.fromEntity()`)

### 2. Business Code System

Hệ thống quản lý error codes và messages với cấu trúc module hóa:

```
business_code/
├── CommonCode.java           # Combined enum (HTTP codes + module codes)
└── UserErrorCode.java        # User-specific error codes
```

**CommonCode.java**:
- HTTP status-based codes: SUCCESS (200), BAD_REQUEST (400), UNAUTHORIZED (401), etc.
- Mỗi value có 3 field: `code`, `message`, `silent`

**UserErrorCode.java**:
- USER_NOT_FOUND ("USER_01", silent = false)
- USER_ALREADY_EXISTS ("USER_02", silent = false)
- EMAIL_ALREADY_EXISTS ("USER_03", silent = false)
- INVALID_USER_DATA ("USER_04", silent = **true**)

#### Silent Code

Field `silent` phân loại 2 nhóm business code:

| `silent` | Hành vi ở Frontend |
|---|---|
| `false` | Tự động hiển thị toast notification |
| `true` | **Không** hiển thị toast — component tự xử lý (vd: inline error trên form) |

```java
// Đánh dấu silent = true để component tự xử lý, không bắn toast
INVALID_USER_DATA("USER_04", "Dữ liệu người dùng không hợp lệ", true);
```

Generator tự động collect tất cả codes có `silent = true` và generate ra `SILENT_BUSINESS_CODES` trong file TypeScript.

**Mở rộng**: Tạo enum mới trong package `business_code` → TypeScript tự động generate kể cả `SILENT_BUSINESS_CODES`!

### 3. TypeScript Code Generation

Có 4 generator chạy độc lập, tất cả dùng Java Reflection để scan controllers và DTOs:

---

#### 3a. EnumToTypeScriptGenerator

Tự động generate TypeScript enums từ Java enums trong package `business_code`.

**Cách hoạt động**:
1. Auto-scan package `com.ele.javademo.business_code` để tìm tất cả enum classes
2. Sử dụng Java Reflection để extract enum values, `getCode()`, `getMessage()`, `isSilent()`
3. Generate TypeScript enums, Messages objects và `SILENT_BUSINESS_CODES`
4. Chạy tự động trong Maven phase `process-classes`

**Output**: `src/main/resources/enumBusinessCode/business-codes.ts`
```typescript
export enum UserErrorCode {
  USER_NOT_FOUND = "USER_01",
  INVALID_USER_DATA = "USER_04",
  // ...
}

export const UserErrorCodeMessages: Record<UserErrorCode, string> = {
  [UserErrorCode.USER_NOT_FOUND]: "Không tìm thấy người dùng",
  // ...
};

// Combined
export const BusinessCode = { ...UserErrorCode, ...CommonCode } as const;
export const BusinessCodeMessages = { ...UserErrorCodeMessages, ...CommonCodeMessages };

/**
 * Các business code KHÔNG hiển thị toast tự động.
 * Caller tự chịu trách nhiệm xử lý (vd: hiển thị inline error trên form).
 */
export const SILENT_BUSINESS_CODES = new Set<string>([
  UserErrorCode.INVALID_USER_DATA,
]);
```

**Sử dụng trong Frontend**:
```typescript
import { CommonCode, CommonCodeMessages } from './business-codes';

if (response.code === CommonCode.USER_NOT_FOUND) {
  console.log(CommonCodeMessages[CommonCode.USER_NOT_FOUND]);
}
```

---

#### 3b. Controller → TypeScript API Generators

3 generator scan `@RestController` classes và DTO classes để tự động generate TypeScript API code cho từng frontend framework.

**Cách hoạt động chung**:
1. Scan package `com.ele.javademo.controllers` tìm tất cả `@RestController`
2. Với mỗi controller, scan package `com.ele.javademo.dto.<tên-controller>` tìm DTO classes
3. Generate 2 files: interface file và API file
4. Chạy thủ công qua `exec-maven-plugin`

**Naming convention**:
- `UserController` → folder `user/`, files `user.interface.ts` + `user.api.ts`
- `OrderDetailController` → folder `order-detail/`, files `order-detail.interface.ts` + `order-detail.api.ts`

**So sánh output giữa 3 frameworks**:

| | Angular | Vue | React |
|---|---|---|---|
| Output folder | `api-angular/` | `api-vue/` | `api-react/` |
| HTTP client | `HttpClient` (DI) | `httpContext` (axios) | `httpContext` (axios) |
| Style | `class @Injectable` | `async` functions | plain functions (return promise) |
| Return type (list) | `Observable<IBaseResponse<IBasePagination<IFoo>>>` | `Promise<IBasePaginationResponse<IFoo>>` | `IBasePaginationResponse<IFoo>` |
| Return type (single) | `Observable<IBaseResponse<IFoo>>` | `Promise<IBaseResponse<IFoo>>` | `IBaseResponse<IFoo>` |
| Error interceptor import | — | — | `import "../http-error.interceptor"` |
| Shared models path | `../../shared/models/interfaces` | `@/shared/models/interfaces.ts` | `../../shared/models/interfaces` |

**Ví dụ output Angular** (`user.api.ts`):
```typescript
@Injectable({ providedIn: 'root' })
export class UserApiService {
  http = inject(HttpClient);

  getUsers(page: number, size: number, skipToast = false): Observable<IBaseResponse<IBasePagination<IUserResponse>>> {
    // ...
  }
}
```

**Ví dụ output Vue** (`user.api.ts`):
```typescript
export const getUsers = async (
  page: number,
  size: number,
): Promise<IBasePaginationResponse<IUserResponse>> => {
  const response = await httpContext.get<IBasePaginationResponse<IUserResponse>>('/api/users', {
    params: { page, size },
  })
  return response.data
}
```

**Ví dụ output React** (`user.api.ts`):
```typescript
export const getUsers = (
  page: number = 1,
  size: number = 10,
) => {
  return httpContext.get<IBasePaginationResponse<IUserResponse>>("/api/users", {
    params: { page, size },
  });
};
```

### 4. API Endpoints

**Base URL**: `http://localhost:8080/api`

#### GET /api/users (Paginated)
```http
GET /api/users?page=0&size=10
```

**Query Parameters**:
- `page` (optional, default: 0) - Page number (0-indexed)
- `size` (optional, default: 10) - Records per page

**Response**:
```json
{
  "code": "00",
  "message": "Thao tác thành công",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Nguyễn Văn A",
        "email": "nguyenvana@example.com",
        "role": "ADMIN",
        "status": "ACTIVE",
        "createdAt": "2024-01-01T10:00:00",
        "updatedAt": "2024-01-01T10:00:00"
      }
    ],
    "totalRecords": 100,
    "page": 0,
    "size": 10
  }
}
```

#### POST /api/users (Create)
```http
POST /api/users
Content-Type: application/json

{
  "name": "Nguyễn Văn B",
  "email": "nguyenvanb@example.com",
  "role": "USER",
  "status": "ACTIVE"
}
```

**Validation Rules**:
- `name`: Required, max 100 chars
- `email`: Required, valid email format, max 100 chars
- `role`: Required, max 50 chars
- `status`: Required, max 20 chars

**Response (Success)**:
```json
{
  "code": "00",
  "message": "Thao tác thành công",
  "data": {
    "id": 2,
    "name": "Nguyễn Văn B",
    "email": "nguyenvanb@example.com",
    "role": "USER",
    "status": "ACTIVE",
    "createdAt": "2024-01-02T10:00:00",
    "updatedAt": "2024-01-02T10:00:00"
  }
}
```

**Response (Validation Error)**:
```json
{
  "code": "422",
  "message": "Dữ liệu không hợp lệ",
  "data": null
}
```

### 5. Features chính

#### Bean Validation
- Automatic validation với `@Valid` trong Controller
- Custom error messages bằng tiếng Việt
- Annotations: `@NotBlank`, `@Email`, `@Size`

#### Pagination
- Spring Data Pageable với `PageRequest.of(page, size)`
- Response metadata: totalRecords, page, size
- Client control page number và size

#### CORS Configuration
- Centralized config trong `configs/CorsConfig.java`
- Whitelist-based: Chỉ allow origins trong danh sách
- Configurable via `application.properties`: `cors.allowed-origins`

#### Database Migration
- Liquibase auto-migration khi start application
- Version control cho database schema changes
- Rollback support
- Changelog: `src/main/resources/db/changelog/`

#### Swagger/OpenAPI
- Auto-generated documentation từ annotations
- Interactive UI: `http://localhost:8080/swagger-ui.html`
- Custom annotations: `@Operation`, `@Parameter`, `@ApiResponseSuccess`

#### Environment-based Configuration
- Externalized config: Environment variables với default values
- No hardcoded credentials
- Format: `${ENV_VAR:default_value}`

## Luồng dữ liệu

### 1. Get All Users (Lấy danh sách người dùng với pagination)
```
┌─────────────────┐
│   Frontend      │ GET /api/users?page=0&size=10
└────────┬────────┘
         │ HTTP Request
         ▼
┌─────────────────┐
│ UserController  │ @GetMapping, validate params
└────────┬────────┘
         │ getAllUsers(page, size)
         ▼
┌─────────────────┐
│  UserService    │ @Transactional(readOnly = true)
└────────┬────────┘
         │ PageRequest.of(page, size)
         ▼
┌─────────────────┐
│ UserRepository  │ findAll(pageable)
└────────┬────────┘
         │ SQL Query với LIMIT/OFFSET
         ▼
┌─────────────────┐
│  MySQL DB       │ SELECT * FROM users LIMIT 10 OFFSET 0
└────────┬────────┘
         │ Page<User>
         ▼
┌─────────────────┐
│  UserService    │ Convert Entity → UserResponse DTOs
└────────┬────────┘
         │ PagedResponse.of(...)
         ▼
┌─────────────────┐
│ UserController  │ ApiResponse.success(pagedUsers)
└────────┬────────┘
         │ JSON Response
         ▼
┌─────────────────┐
│   Frontend      │ Render table với pagination
└─────────────────┘
```

### 2. Create User (Tạo người dùng mới)
```
┌─────────────────┐
│   Frontend      │ POST /api/users (JSON body)
└────────┬────────┘
         │ HTTP Request
         ▼
┌─────────────────┐
│ UserController  │ @PostMapping, @Valid validation
└────────┬────────┘
         │ Bean Validation (Jakarta Validation)
         ▼
┌─────────────────┐
│ Validation      │ Check @NotBlank, @Email, @Size
└────────┬────────┘
         │ Valid? → Continue | Invalid? → Return 422
         ▼
┌─────────────────┐
│  UserService    │ @Transactional (begin transaction)
└────────┬────────┘
         │ Business logic validation
         │ Convert DTO → Entity
         ▼
┌─────────────────┐
│ UserRepository  │ save(user)
└────────┬────────┘
         │ INSERT INTO users
         ▼
┌─────────────────┐
│  MySQL DB       │ Persist data
└────────┬────────┘
         │ User entity (với ID)
         ▼
┌─────────────────┐
│  UserService    │ @Transactional (commit if success)
│                 │ Convert Entity → UserResponse
└────────┬────────┘
         │ UserResponse
         ▼
┌─────────────────┐
│ UserController  │ ApiResponse.success(userResponse)
└────────┬────────┘
         │ JSON Response (201 Created)
         ▼
┌─────────────────┐
│   Frontend      │ Show success toast, redirect
└─────────────────┘
```

### 3. TypeScript Generation (Auto-generate khi build)

#### 3a. Enum Generator (chạy tự động khi build)
```
┌─────────────────┐
│  Developer      │ ./mvnw clean compile
└────────┬────────┘
         │ Maven lifecycle
         ▼
┌─────────────────┐
│  Maven          │ Phase: process-classes
└────────┬────────┘
         │ exec-maven-plugin
         ▼
┌───────────────────────────────┐
│ EnumToTypeScriptGenerator     │ main()
└────────┬──────────────────────┘
         │ Scan: com.ele.javademo.business_code
         ▼
┌─────────────────┐
│  Reflection     │ getCode(), getMessage(), isSilent()
└────────┬────────┘
         │ Generate TypeScript
         ▼
┌─────────────────┐
│  File System    │ enumBusinessCode/business-codes.ts
└─────────────────┘
```

#### 3b. API Generators (chạy thủ công)
```
┌─────────────────┐
│  Developer      │ ./mvnw process-classes -P gen-angular-api
└────────┬────────┘                         (hoặc gen-vue-api / gen-react-api)
         │ exec-maven-plugin
         ▼
┌──────────────────────────────────────┐
│ ControllerToTypeScript[X]ApiGenerator│ main()
└────────┬─────────────────────────────┘
         │ Step 1: Scan @RestController classes
         ▼
┌─────────────────┐
│  Reflection     │ com.ele.javademo.controllers
└────────┬────────┘
         │ Step 2: For each controller
         ▼
┌─────────────────┐
│  Reflection     │ Scan dto.<controller-name> package
│                 │ - DTO fields → interface
│                 │ - @GetMapping/@PostMapping/... → API methods
│                 │ - @RequestParam/@PathVariable/@RequestBody → params
└────────┬────────┘
         │ Step 3: Generate 2 files per controller
         ▼
┌──────────────────────────────┐
│  File System                 │ api-angular/ (hoặc api-vue/ / api-react/)
│                              │ └── user/
│                              │     ├── user.interface.ts
│                              │     └── user.api.ts
└──────────────────────────────┘
```

## Cấu hình

### Application Properties

File: `src/main/resources/application.properties`

```properties
# Server Configuration
server.port=${SERVER_PORT:8080}
# Port chạy ứng dụng, có thể override qua environment variable SERVER_PORT

# Database Configuration
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/myDB?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true}
# MySQL connection string, có thể override qua DB_URL
spring.datasource.username=${DB_USERNAME:root}
# Database username, có thể override qua DB_USERNAME
spring.datasource.password=${DB_PASSWORD:Str4ng3r@96}
# Database password, có thể override qua DB_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
# MySQL JDBC driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=none
# none = Không tự động thay đổi schema (dùng Liquibase thay thế)
spring.jpa.show-sql=true
# true = Hiển thị SQL queries trong console (hữu ích cho debugging)
spring.jpa.properties.hibernate.format_sql=true
# true = Format SQL queries cho dễ đọc

# Liquibase Configuration
spring.liquibase.enabled=true
# true = Bật Liquibase để tự động chạy migrations khi start app
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.xml
# Path tới file changelog master của Liquibase

# CORS Configuration
cors.allowed-origins=http://localhost:4200,http://localhost:3000,http://localhost:5173
# Danh sách origins được phép truy cập API (whitelist)

# Swagger Configuration
springdoc.api-docs.path=/api-docs
# Path để access OpenAPI JSON schema
springdoc.swagger-ui.path=/swagger-ui.html
# Path để access Swagger UI interactive documentation
```

### CORS Origins

Thêm/bớt allowed origins:

```properties
cors.allowed-origins=http://localhost:4200,http://localhost:3000,https://myapp.com
```

## Reference Documentation

### Spring Boot
- [Official Maven documentation](https://maven.apache.org/guides/index.html)
- [Spring Boot Maven Plugin](https://docs.spring.io/spring-boot/4.0.4/maven-plugin)
- [Spring Data JPA](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#data.sql.jpa-and-spring-data)

### Database
- [Accessing data with MySQL](https://spring.io/guides/gs/accessing-data-mysql/)
- [Liquibase Documentation](https://docs.liquibase.com/home.html)

### API Documentation
- [Springdoc OpenAPI](https://springdoc.org/)
- [Swagger UI](https://swagger.io/tools/swagger-ui/)

### Validation
- [Jakarta Bean Validation](https://jakarta.ee/specifications/bean-validation/3.0/)
- [Spring Validation](https://docs.spring.io/spring-framework/reference/core/validation/beanvalidation.html)
