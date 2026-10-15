# ele-angular-demo

## Thư viện sử dụng

### Thư viện cốt lõi
- **Angular** (^21.2.0) - Framework TypeScript để xây dựng ứng dụng web động
- **TypeScript** (~5.9.2) - JavaScript có hỗ trợ kiểu dữ liệu
- **Angular CLI** (^21.2.2) - Command-line interface cho Angular
- **RxJS** (~7.8.0) - Thư viện lập trình reactive với Observables

### UI Components
- **PrimeNG** (^21.1.3) - Thư viện component UI phong phú cho Angular
- **PrimeIcons** (^7.0.0) - Thư viện icon cho PrimeNG
- **@primeuix/themes** (^2.0.3) - Theme system cho PrimeNG

### Routing
- **Angular Router** (^21.2.0) - Thư viện điều hướng chính thức cho Angular

### Forms
- **Angular Forms** (^21.2.0) - Module quản lý forms với reactive forms và template-driven forms

### Development Tools
- **JSON Server** (^1.0.0-beta.12) - Tạo fake REST API cho phát triển và testing
- **npm-run-all2** (^8.0.4) - Chạy nhiều npm scripts song song hoặc tuần tự
- **Vitest** (^4.0.8) - Framework testing nhanh cho TypeScript/JavaScript
- **Prettier** (^3.8.1) - Code formatter
- **Husky** (^9.1.7) - Git hooks: tự động chạy strip-nocheck → test → build → format trước mỗi commit

## Scripts

- `npm run dev` - Khởi động server phát triển và JSON Server song song
- `npm start` - Khởi động server phát triển trên http://localhost:4200
- `npm run build` - Build ứng dụng cho production
- `npm run strip-nocheck` - Xóa toàn bộ `// @ts-nocheck` trong `src/**/*.ts` (cross-platform)
- `npm run test` - Chạy unit tests với Vitest (single run, không watch)
- `npm run format` - Format toàn bộ code với Prettier (`src/**/*.{ts,html,css}`)
- `npm run server` - Khởi động JSON Server trên port 3001

## Cấu trúc thư mục

```
src/app/
├── apis/                    # API calls và services
│   ├── business-code.ts     # Business code enums, messages và SILENT_BUSINESS_CODES
│   ├── http-context.ts      # HttpContextToken cho per-request config (SKIP_BUSINESS_ERROR_TOAST)
│   ├── http-error.interceptor.ts  # HTTP interceptor xử lý HTTP errors và business code errors
│   └── home/
│       ├── user.api.ts      # API gọi tới backend (get all users với pagination, create user)
│       └── user.interface.ts # Interface cho UserResponse và CreateUserRequest

├── views/                   # Các trang/màn hình chính
│   ├── home/
│   │   ├── home-page.component.ts   # Component trang danh sách người dùng
│   │   ├── home-page.component.html # Template bảng với pagination, actions
│   │   └── home-page.component.css  # Styles cho trang home
│   └── about/
│       ├── about-page.component.ts   # Component tạo tài khoản
│       ├── about-page.component.html # Template form với validation
│       ├── about-page.component.css  # Styles cho trang about
│       └── models/
│           ├── interfaces.ts # Interface cho form data và errors
│           └── constants.ts  # Constants cho dropdown options (role, status)

├── shared/                  # Tài nguyên dùng chung toàn dự án
│   ├── base/                # Base classes
│   │   └── base.component.ts # Base component với DestroyRef và untilDestroyed()
│   ├── components/          # Các component dùng chung
│   │   ├── navigation/
│   │   └── table/           # Component bảng tái sử dụng
│   ├── services/            # Services toàn cục
│   │   ├── user.service.ts  # Service quản lý users state và API calls
│   │   └── toast.service.ts # Service quản lý toast notifications global
│   └── utils/               # Utility functions dùng chung
│       └── date-formatter.ts # Hàm format ngày tháng (DD/MM/YYYY HH:mm)

├── app.routes.ts            # Cấu hình routes cho ứng dụng
├── app.config.ts            # Cấu hình providers (Router, HttpClient, PrimeNG, Interceptors)
├── app.ts                   # Component gốc
├── app.html                 # Template gốc (routing, toast provider global)
└── app.css                  # Global styles

├── main.ts                  # Entry point (bootstrap application)
└── styles.css               # Global CSS và PrimeNG theme imports
```

## Mô tả chức năng

### 1. Quản lý State (Services)
- **shared/services/user.service.ts**: Service quản lý state của users với Angular Signals
  - **State**: `users` (signal), `loading` (signal), `error` (signal)
  - **Methods**:
    - `fetchUsersAsync()` - Async method fetch danh sách users từ API
    - `createUserAsync(request: CreateUserRequest)` - Async method tạo user mới và cập nhật state
  - Sử dụng Angular Signals cho reactive state management
  - Service được inject với `providedIn: 'root'` (singleton)

### 2. API Layer
- **apis/home/user.api.ts**: Tập trung các API calls liên quan đến users
  - `getAllUsers(page, size, skipToast?)`: GET danh sách users với pagination, `skipToast=true` để tắt toast business error
  - `createUser(request, skipToast?)`: POST tạo user mới, `skipToast=true` để tắt toast business error
- **apis/home/user.interface.ts**: Type definitions cho User entities
  - `UserResponse`: Interface cho user data trả về từ API
  - `CreateUserRequest`: Interface cho request tạo user mới
- **apis/business-code.ts**: Business code enums và messages
  - `UserErrorCode`: USER_NOT_FOUND, USER_ALREADY_EXISTS, EMAIL_ALREADY_EXISTS, INVALID_USER_DATA
  - `CommonCode`: SUCCESS, BAD_REQUEST, UNAUTHORIZED, FORBIDDEN, NOT_FOUND, CONFLICT, VALIDATION_ERROR, INTERNAL_ERROR, SERVICE_UNAVAILABLE
  - `BusinessCodeMessages`: Mapping code → message tiếng Việt
  - `SILENT_BUSINESS_CODES`: Tập các code không tự động bắn toast (vd: INVALID_USER_DATA — component tự xử lý)
- **apis/http-context.ts**: HttpContextToken cho per-request config
  - `SKIP_BUSINESS_ERROR_TOAST`: Set `true` để tắt toast business error cho 1 request cụ thể
- **apis/http-error.interceptor.ts**: HTTP Interceptor xử lý hai tầng lỗi
  - HTTP status ≠ 200: Tự động hiển thị error message lên toast (ưu tiên business code từ BusinessCodeMessages)
  - HTTP status = 200 + `code != SUCCESS`: Hiển thị toast trừ khi code thuộc `SILENT_BUSINESS_CODES` hoặc `SKIP_BUSINESS_ERROR_TOAST = true`; response vẫn được trả về để component tự xử lý logic

### 3. Views (Pages)
- **HomePage**: Hiển thị bảng danh sách users với:
  - PrimeNG Table component (pagination, sorting)
  - Actions: Nút Sửa và Xóa cho mỗi row
  - Format ngày tháng
  - Loading state từ UserService
  - Nút "Tạo tài khoản" chuyển đến AboutPage
  - Subscribe users$ observable để hiển thị data

- **AboutPage**: Form tạo tài khoản với:
  - Reactive Forms với validation (required, email format)
  - Dropdown options (role, status với giá trị mặc định)
  - Toast notification khi thành công/thất bại
  - Tự động redirect về HomePage sau khi tạo thành công
  - Loading state từ UserService
  - FormGroup với FormControl cho từng field

### 4. Shared Resources
- **shared/base**: Base classes cho components
  - **base.component.ts**: Base component với DestroyRef
    - Cung cấp `untilDestroyed()` method để tự động unsubscribe khi component destroy
    - Components chỉ cần `extends BaseComponent` và dùng `this.untilDestroyed()` thay vì `takeUntilDestroyed(this.destroyRef)`

- **shared/components**: Các component dùng chung toàn dự án
  - **table/**: Component bảng tái sử dụng (wrapper PrimeNG Table)
  - **navigation/**: Component navigation menu

- **shared/utils**: Utility functions dùng chung
  - **date-formatter.ts**: Format ISO string → DD/MM/YYYY HH:mm

- **shared/services**: Services toàn cục
  - **toast.service.ts**: Singleton service quản lý toast notifications
    - Methods: `success()`, `error()`, `info()`, `warn()`, `show()`
    - Inject MessageService từ PrimeNG
    - Toast được config trong app.config.ts
  - **user.service.ts**: Singleton service quản lý users state
    - Sử dụng RxJS BehaviorSubject cho reactive state
    - Methods để fetch và create users

### 5. View Models
- **views/about/models**: Chứa interface và constants riêng cho trang About
  - **interfaces.ts**: Type definitions cho `FormData` và `FormErrors`
  - **constants.ts**: Dropdown options cho role và status

## Luồng dữ liệu

### 1. Fetch Users (Lấy danh sách người dùng)
```
┌─────────────┐
│  HomePage   │
└──────┬──────┘
       │ ngOnInit
       │ userService.fetchUsers()
       ▼
┌──────────────┐
│ user.service │ (loading$.next(true))
│  (Service)   │
└──────┬───────┘
       │ call API
       ▼
┌──────────────┐
│ user.api.ts  │ HTTP GET /users (+ HttpContext)
└──────┬───────┘
       │ request
       ▼
┌──────────────────────┐
│ httpErrorInterceptor │
└──────┬───────────────┘
       │ next(req)
       ▼
┌──────────────┐
│      BE      │
└──────┬───────┘
       │ HTTP Response
       ▼
┌──────────────────────┐
│ httpErrorInterceptor │ kiểm tra code, bắn toast nếu lỗi
└──────┬───────────────┘
       │ Observable emit
       ▼
┌──────────────┐
│ user.service │ (users$.next(data), loading$.next(false))
└──────┬───────┘
       │ Observable emit
       ▼
┌─────────────┐
│  HomePage   │ users$ | async → render Table với data
└─────────────┘
```

### 2. Create User (Tạo người dùng mới)
```
┌──────────────┐
│  AboutPage   │
└──────┬───────┘
       │ submit form
       │ userService.createUser(userData)
       ▼
┌──────────────┐
│ user.service │ (loading$.next(true))
│  (Service)   │
└──────┬───────┘
       │ call API
       ▼
┌──────────────┐
│ user.api.ts  │ HTTP POST /users (+ HttpContext)
└──────┬───────┘
       │ request
       ▼
┌──────────────────────┐
│ httpErrorInterceptor │
└──────┬───────────────┘
       │ next(req)
       ▼
┌──────────────┐
│      BE      │
└──────┬───────┘
       │ HTTP Response
       ▼
┌──────────────────────┐
│ httpErrorInterceptor │ kiểm tra code, bắn toast nếu lỗi
└──────┬───────────────┘
       │ Observable emit
       ▼
┌──────────────┐
│ user.service │ (users$.next([...users, newUser]), loading$.next(false))
└──────┬───────┘
       │ success
       ▼
┌──────────────┐
│  AboutPage   │ toastService.success('Tạo thành công')
└──────┬───────┘
       │ setTimeout
       ▼
┌──────────────────┐
│ router.navigate  │ redirect về HomePage
└──────────────────┘
```

### 3. Toast Notifications (Thông báo toàn cục)
```
┌─────────────────┐
│ Any Component   │ (AboutPage, HomePage, etc...)
└────────┬────────┘
         │ toastService.success/error/info/warn()
         ▼
┌─────────────────┐     ┌──────────────────────────┐
│  toastService   │ ◄───│  httpErrorInterceptor    │
│  (singleton)    │     │ (HTTP & business errors) │
└────────┬────────┘     └──────────────────────────┘
         │ Subject.next()
         ▼
┌─────────────────┐
│  app.html       │ <p-toast /> component
└────────┬────────┘
         │ subscribe toast$
         ▼
┌─────────────────┐
│  PrimeNG        │ hiển thị toast notification
│  Toast          │ (top-right corner)
└─────────────────┘
```


## User Management CRUD

The User Management screen calls the backend at `http://localhost:8080/api/users`.
Start the backend before using the page. The expected response wrapper is
`{ code, message, result }`, and the list result contains `content`, `totalElements`,
`page`, and `size`.

Implemented on the Angular side:
- GET list with server-side pagination (page is zero-based).
- Create and update dialogs with client-side validation.
- Delete confirmation and page adjustment after deletion.
- Loading states and toast notifications.
