package com.hotel.booking.service;

import com.hotel.booking.dto.AuthResponse;
import com.hotel.booking.dto.UserDto;
import com.hotel.booking.entity.User;
import com.hotel.booking.exception.BookingException;
import com.hotel.booking.repository.UserRepository;
import com.hotel.booking.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public UserDto createUser(UserDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new BookingException("Username exists");
        }
        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole() != null ? dto.getRole() : "USER")
                .build();
        user = userRepository.save(user);
        return toDto(user);
    }

    public AuthResponse authenticate(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BookingException("Invalid credentials"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BookingException("Invalid credentials");
        }
        String token = jwtUtil.generateToken(user.getId().toString(), user.getUsername(), user.getRole());
        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    public UserDto updateUser(Long id, UserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BookingException("User not found"));
        if (dto.getUsername() != null) user.setUsername(dto.getUsername());
        if (dto.getPassword() != null) user.setPassword(passwordEncoder.encode(dto.getPassword()));
        if (dto.getRole() != null) user.setRole(dto.getRole());
        user = userRepository.save(user);
        return toDto(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    private UserDto toDto(User u) {
        return UserDto.builder()
                .id(u.getId())
                .username(u.getUsername())
                .role(u.getRole())
                .build();
    }
}
