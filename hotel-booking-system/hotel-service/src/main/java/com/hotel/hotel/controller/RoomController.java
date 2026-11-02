package com.hotel.hotel.controller;

import com.hotel.hotel.dto.ConfirmAvailabilityResponse;
import com.hotel.hotel.dto.RoomDto;
import com.hotel.hotel.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping("/{id}/confirm-availability")
    public ResponseEntity<ConfirmAvailabilityResponse> confirmAvailability(
            @PathVariable Long id,
            @RequestParam String bookingId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String requestId) {
        return ResponseEntity.ok(roomService.confirmAvailability(id, bookingId, startDate, endDate, requestId));
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<Void> releaseRoom(
            @PathVariable Long id,
            @RequestParam String bookingId,
            @RequestParam String requestId) {
        roomService.releaseRoom(id, bookingId, requestId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/confirm-booking")
    public ResponseEntity<Void> confirmBooking(
            @PathVariable Long id,
            @RequestParam String requestId) {
        roomService.confirmBooking(id, requestId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/recommend")
    public ResponseEntity<List<RoomDto>> getRecommendedRooms() {
        return ResponseEntity.ok(roomService.getRecommendedRooms());
    }

    @GetMapping
    public ResponseEntity<List<RoomDto>> getAllAvailableRooms() {
        return ResponseEntity.ok(roomService.getAllAvailableRooms());
    }
}
