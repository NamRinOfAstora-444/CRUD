package com.ele.javademo.services;

import com.ele.javademo.dto.PagedResponse;
import com.ele.javademo.dto.user.UserRequest;
import com.ele.javademo.dto.user.UserResponse;

public interface IUserService {
    PagedResponse<UserResponse> getAllUsers(int page, int size);
    UserResponse createUser(UserRequest request);
    UserResponse updateUser(Long id, UserRequest request);
    void deleteUser(Long id);
}
