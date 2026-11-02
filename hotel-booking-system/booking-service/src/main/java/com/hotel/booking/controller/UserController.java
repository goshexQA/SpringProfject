package com.hotel.booking.controller;

import com.hotel.booking.dto.AuthResponse;
import com.hotel.booking.dto.UserDto;
import com.hotel.booking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody UserDto dto) {
        UserDto created = userService.createUser(dto);
        AuthResponse auth = userService.authenticate(created.getUsername(), dto.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(auth);
    }

    @PostMapping("/auth")
    public ResponseEntity<AuthResponse> auth(@RequestBody Map<String, String> creds) {
        AuthResponse auth = userService.authenticate(creds.get("username"), creds.get("password"));
        return ResponseEntity.ok(auth);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(dto));
    }

    @PatchMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> updateUser(@RequestBody UserDto dto) {
        return ResponseEntity.ok(userService.updateUser(dto.getId(), dto));
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@RequestParam Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
