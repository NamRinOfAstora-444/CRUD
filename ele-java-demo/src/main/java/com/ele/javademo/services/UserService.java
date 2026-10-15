package com.ele.javademo.services;

import com.ele.javademo.business_code.UserErrorCode;
import com.ele.javademo.dto.PagedResponse;
import com.ele.javademo.dto.user.UserRequest;
import com.ele.javademo.dto.user.UserResponse;
import com.ele.javademo.entities.User;
import com.ele.javademo.exceptions.DuplicateResourceException;
import com.ele.javademo.exceptions.ResourceNotFoundException;
import com.ele.javademo.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getAllUsers(int page, int size) {
        if (page < 0 || size < 1) throw new IllegalArgumentException("Page phải >= 0 và size phải > 0");
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updateAt"));
        Page<User> userPage = userRepository.findAll(pageable);
        List<UserResponse> content = userPage.getContent().stream().map(UserResponse::formEntity).collect(Collectors.toList());
        return PagedResponse.of(content, userPage.getTotalElements(), page, size);
    }

    @Override
    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(UserErrorCode.EMAIL_ALREADY_EXISTS.getCode(), "Email đã được sử dụng");
        }
        User user = User.builder()
                .name(request.getName().trim())
                .email(request.getEmail().trim())
                .role(request.getRole())
                .status(request.getStatus())
                .build();
        return UserResponse.formEntity(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(UserErrorCode.USER_NOT_FOUND.getCode(), "Không tìm thấy người dùng"));
        if (!user.getEmail().equalsIgnoreCase(request.getEmail().trim())) {
            throw new IllegalArgumentException("Email không được thay đổi");
        }
        user.setName(request.getName().trim());
        user.setRole(request.getRole());
        user.setStatus(request.getStatus());
        return UserResponse.formEntity(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(UserErrorCode.USER_NOT_FOUND.getCode(), "Không tìm thấy người dùng"));
        userRepository.delete(user);
    }
}
