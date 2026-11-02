package com.hotel.hotel.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice

hotel-booking-system/hotel-service/src/main/java/com/hotel/hotel = "hotel-booking-system/hotel-service/src/main/java/com/hotel/hotel"

# RoomService.java
Set-Content -Path "hotel-booking-system/hotel-service/src/main/java/com/hotel/hotel/service/RoomService.java" -Encoding UTF8 -Value @"
package com.hotel.hotel.service;

import com.hotel.hotel.dto.ConfirmAvailabilityResponse;
import com.hotel.hotel.dto.RoomDto;
import com.hotel.hotel.entity.Room;
import com.hotel.hotel.exception.ConflictException;
import com.hotel.hotel.exception.ResourceNotFoundException;
import com.hotel.hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {

    private final RoomRepository roomRepository;
    private final Map<String, Boolean> processedRequests = new ConcurrentHashMap<>();

    @Transactional
    public ConfirmAvailabilityResponse confirmAvailability(Long roomId, String bookingId,
                                                           LocalDate startDate, LocalDate endDate,
                                                           String requestId) {
        log.info("Confirm availability: roomId={}, bookingId={}, requestId={}", roomId, bookingId, requestId);

        if (processedRequests.containsKey("confirm_" + requestId)) {
            log.info("Idempotent: request {} already processed", requestId);
            return new ConfirmAvailabilityResponse(true, "Already processed");
        }

        Room room = roomRepository.findByIdWithLock(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + roomId));

        if (room.getLockedUntil() != null && room.getLockedUntil().isAfter(LocalDateTime.now())) {
            if (!room.getLockedByBookingId().equals(bookingId)) {
                throw new ConflictException("Room is temporarily locked by another booking");
            }
        }

        room.setLockedUntil(LocalDateTime.now().plusMinutes(10));
        room.setLockedByBookingId(bookingId);
        roomRepository.save(room);

        processedRequests.put("confirm_" + requestId, true);
        log.info("Room {} locked for booking {}", roomId, bookingId);

        return new ConfirmAvailabilityResponse(true, "Room locked successfully");
    }

    @Transactional
    public void releaseRoom(Long roomId, String bookingId, String requestId) {
        log.info("Release room: roomId={}, bookingId={}, requestId={}", roomId, bookingId, requestId);

        if (processedRequests.containsKey("release_" + requestId)) {
            return;
        }

        Room room = roomRepository.findByIdWithLock(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + roomId));

        if (bookingId.equals(room.getLockedByBookingId())) {
            room.setLockedUntil(null);
            room.setLockedByBookingId(null);
            roomRepository.save(room);
            processedRequests.put("release_" + requestId, true);
            log.info("Room {} lock released", roomId);
        }
    }

    @Transactional
    public void confirmBooking(Long roomId, String requestId) {
        log.info("Confirm booking for room: roomId={}, requestId={}", roomId, requestId);

        if (processedRequests.containsKey("final_confirm_" + requestId)) {
            return;
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + roomId));

        room.setTimesBooked(room.getTimesBooked() + 1);
        room.setLockedUntil(null);
        room.setLockedByBookingId(null);
        roomRepository.save(room);

        processedRequests.put("final_confirm_" + requestId, true);
        log.info("Room {} booking confirmed, timesBooked={}", roomId, room.getTimesBooked());
    }

    public List<RoomDto> getRecommendedRooms() {
        return roomRepository.findByAvailableTrueOrderByTimesBookedAscIdAsc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<RoomDto> getAllAvailableRooms() {
        return roomRepository.findByAvailableTrueOrderByTimesBookedAscIdAsc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private RoomDto toDto(Room r) {
        return RoomDto.builder()
                .id(r.getId())
                .hotelId(r.getHotelId())
                .number(r.getNumber())
                .available(r.getAvailable())
                .timesBooked(r.getTimesBooked())
                .build();
    }
}
