package com.zorvyn.fintech.service;

import com.zorvyn.fintech.dto.request.AssignRoleRequest;
import com.zorvyn.fintech.dto.request.UpdateUserRequest;
import com.zorvyn.fintech.dto.response.UserResponse;
import com.zorvyn.fintech.entity.Role;
import com.zorvyn.fintech.entity.User;
import com.zorvyn.fintech.exception.ResourceNotFoundException;
import com.zorvyn.fintech.repository.RoleRepository;
import com.zorvyn.fintech.repository.UserRepository;
import com.zorvyn.fintech.util.Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                        PasswordEncoder passwordEncoder, AuditService auditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return UserResponse.fromEntity(user);
    }

    public Page<UserResponse> listUsers(Pageable pageable) {
        return userRepository.findByIsActiveTrue(pageable)
                .map(UserResponse::fromEntity);
    }

    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request, String ipAddress, String userAgent) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPassword() != null) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        user = userRepository.save(user);

        auditService.log(id.toString(), Constants.AUDIT_USER_UPDATE, "user",
                id.toString(), null, ipAddress, userAgent);

        return UserResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse assignRole(UUID userId, AssignRoleRequest request, String ipAddress, String userAgent) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Role role = roleRepository.findByName(request.getRoleName())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", request.getRoleName()));

        user.setRole(role);
        user = userRepository.save(user);

        auditService.log(userId.toString(), Constants.AUDIT_ROLE_ASSIGN, "user",
                userId.toString(),
                java.util.Map.of("newRole", request.getRoleName()),
                ipAddress, userAgent);

        return UserResponse.fromEntity(user);
    }
}
