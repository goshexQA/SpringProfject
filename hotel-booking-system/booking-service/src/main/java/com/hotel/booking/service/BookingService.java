package com.hotel.booking.service;

import com.hotel.booking.client.HotelServiceClient;
import com.hotel.booking.dto.BookingRequest;
import com.hotel.booking.dto.BookingResponse;
import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingStatus;
import com.hotel.booking.exception.BookingException;
import com.hotel.booking.repository.BookingRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final HotelServiceClient hotelServiceClient;

    @Transactional
    public BookingResponse createBooking(BookingRequest request, Long userId) {
        String bookingId = UUID.randomUUID().toString();
        String requestId = UUID.randomUUID().toString();

        log.info("Starting saga: bookingId={}, requestId={}, userId={}", bookingId, requestId, userId);

        Booking booking = Booking.builder()
                .userId(userId)
                .roomId(request.getRoomId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(BookingStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .requestId(requestId)
                .build();

        booking = bookingRepository.save(booking);
        log.info("Booking {} created with status PENDING", booking.getId());

        try {
            Map<String, Object> response = confirmAvailabilityWithCircuitBreaker(
                    request.getRoomId(), booking.getId().toString(),
                    request.getStartDate(), request.getEndDate(), requestId);

            boolean success = Boolean.TRUE.equals(response.get("success"));

            if (success) {
                booking.setStatus(BookingStatus.CONFIRMED);
                bookingRepository.save(booking);
                hotelServiceClient.confirmBooking(request.getRoomId(), requestId);
                log.info("Booking {} confirmed", booking.getId());
            } else {
                compensate(booking, requestId);
                throw new BookingException("Room not available");
            }
        } catch (Exception e) {
            log.error("Saga failed for booking {}, compensating", booking.getId(), e);
            compensate(booking, requestId);
            throw new BookingException("Booking failed: " + e.getMessage());
        }

        return toResponse(booking);
    }

    @CircuitBreaker(name = "hotelService", fallbackMethod = "fallbackConfirm")
    public Map<String, Object> confirmAvailabilityWithCircuitBreaker(
            Long roomId, String bookingId, java.time.LocalDate start,
            java.time.LocalDate end, String requestId) {
        return hotelServiceClient.confirmAvailability(roomId, bookingId, start, end, requestId);
    }

    public Map<String, Object> fallbackConfirm(Long roomId, String bookingId,
            java.time.LocalDate start, java.time.LocalDate end, String requestId, Throwable t) {
        log.error("Circuit breaker fallback: {}", t.getMessage());
        throw new RuntimeException("Hotel service unavailable", t);
    }

    private void compensate(Booking booking, String requestId) {
        log.info("Compensating booking {}", booking.getId());
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        try {
            hotelServiceClient.releaseRoom(booking.getRoomId(), booking.getId().toString(), requestId);
            log.info("Compensation completed: lock released");
        } catch (Exception e) {
            log.error("Failed to release lock during compensation", e);
        }
    }

    public java.util.List<BookingResponse> getUserBookings(Long userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(this::toResponse).toList();
    }

    public BookingResponse getBookingById(Long id) {
        Booking b = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingException("Booking not found: " + id));
        return toResponse(b);
    }

    @Transactional
    public void cancelBooking(Long id, Long userId) {
        Booking b = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingException("Booking not found"));
        if (!b.getUserId().equals(userId)) {
            throw new BookingException("Not your booking");
        }
        if (b.getStatus() == BookingStatus.CONFIRMED) {
            hotelServiceClient.releaseRoom(b.getRoomId(), b.getId().toString(), UUID.randomUUID().toString());
        }
        b.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(b);
    }

    private BookingResponse toResponse(Booking b) {
        return BookingResponse.builder()
                .id(b.getId())
                .userId(b.getUserId())
                .roomId(b.getRoomId())
                .startDate(b.getStartDate())
                .endDate(b.getEndDate())
                .status(b.getStatus().name())
                .createdAt(b.getCreatedAt())
                .build();
    }
}
