package com.sentinel.user.service;

import com.sentinel.common.dto.PagedResponse;
import com.sentinel.common.exception.ResourceNotFoundException;
import com.sentinel.user.dto.UpdateUserRequest;
import com.sentinel.user.dto.UserDto;
import com.sentinel.user.entity.User;
import com.sentinel.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserDto> getAllUsers(int page, int size, String search) {
        Page<User> users;
        if (search != null && !search.isBlank()) {
            users = userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    search, search, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        } else {
            users = userRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
        }
        return toPagedResponse(users);
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return toDto(user);
    }

    @Transactional
    public UserDto updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getRole() != null) user.setRole(request.getRole());
        if (request.getActive() != null) user.setActive(request.getActive());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        user = userRepository.save(user);
        log.info("User updated: {}", user.getEmail());
        return toDto(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setActive(false);
        userRepository.save(user);
        log.info("User deactivated: {}", user.getEmail());
    }

    public static UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getEmail(), user.getFullName(), user.getRole(), user.getActive(),
                user.getAvatarUrl(), user.getPhone(),
                user.getCreatedAt() != null ? user.getCreatedAt().toString() : null,
                user.getLastLoginAt() != null ? user.getLastLoginAt().toString() : null);
    }

    private PagedResponse<UserDto> toPagedResponse(Page<User> page) {
        return new PagedResponse<>(page.getContent().stream().map(UserService::toDto).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isLast());
    }
}

