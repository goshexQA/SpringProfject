package com.hotel.booking.repository;

import com.hotel.booking.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findByUserId(Long userId, Pageable pageable);

    boolean existsByRoomIdAndStartDateAndEndDate(Long roomId, java.time.LocalDate start, java.time.LocalDate end);
}