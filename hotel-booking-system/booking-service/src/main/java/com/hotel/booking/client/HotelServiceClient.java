package com.hotel.booking.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Map;

@FeignClient(name = "hotel-service", configuration = FeignConfig.class)
public interface HotelServiceClient {

    @PostMapping("/api/rooms/{id}/confirm-availability")
    Map<String, Object> confirmAvailability(
            @PathVariable("id") Long roomId,
            @RequestParam("bookingId") String bookingId,
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate,
            @RequestParam("requestId") String requestId);

    @PostMapping("/api/rooms/{id}/release")
    void releaseRoom(
            @PathVariable("id") Long roomId,
            @RequestParam("bookingId") String bookingId,
            @RequestParam("requestId") String requestId);

    @PostMapping("/api/rooms/{id}/confirm-booking")
    void confirmBooking(
            @PathVariable("id") Long roomId,
            @RequestParam("requestId") String requestId);
}
