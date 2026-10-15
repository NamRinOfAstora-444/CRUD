package com.ele.javademo.controllers;

import com.ele.javademo.configs.swagger.user.ApiResponseCreated;
import com.ele.javademo.configs.swagger.user.ApiResponseSuccess;
import com.ele.javademo.dto.ApiResponse;
import com.ele.javademo.dto.PagedResponse;
import com.ele.javademo.dto.user.UserRequest;
import com.ele.javademo.dto.user.UserResponse;
import com.ele.javademo.services.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "UserManagement", description = "APIs for managing users")
public class User {
    private final IUserService userService;

    @GetMapping
    @Operation(summary = "Lấy phân trang danh sách người dùng", description = "Lấy danh sách theo thời gian cập nhật giảm dần")
    @ApiResponseSuccess
    public ApiResponse<PagedResponse<UserResponse>> getAllUsers(
            @Parameter(description = "Page number (0-indexed)", example = "0") @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(userService.getAllUsers(page, size));
    }

    @PostMapping
    @Operation(summary = "Tạo người dùng")
    @ApiResponseCreated
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        return ApiResponse.success("Tạo người dùng thành công", userService.createUser(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật người dùng")
    @ApiResponseSuccess
    public ApiResponse<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return ApiResponse.success("Cập nhật người dùng thành công", userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa người dùng")
    @ApiResponseSuccess
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success("Xóa người dùng thành công", null);
    }
}
