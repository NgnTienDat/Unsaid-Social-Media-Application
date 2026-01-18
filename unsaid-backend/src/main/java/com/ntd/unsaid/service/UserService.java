package com.ntd.unsaid.service;

import com.ntd.unsaid.dto.request.UserCreationRequest;
import com.ntd.unsaid.dto.response.UserResponse;
import com.ntd.unsaid.entity.User;
import com.ntd.unsaid.enums.ErrorCode;
import com.ntd.unsaid.enums.UserRole;
import com.ntd.unsaid.enums.UserStatus;
import com.ntd.unsaid.exception.AppException;
import com.ntd.unsaid.mapper.UserMapper;
import com.ntd.unsaid.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    @Transactional
    public void createUser(UserCreationRequest userCreation) {
        if (this.userRepository.existsByEmail(userCreation.getEmail())) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTED);
        }
        User user = this.userMapper.toUser(userCreation);
        user.setRole(UserRole.USER);
        user.setActive(true);
        user.setPassword(this.passwordEncoder.encode(userCreation.getPassword()));

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTED);
        }
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Jwt jwt) {
        String userEmail = jwt.getClaimAsString("sub");
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toResponse(user);
    }


}
