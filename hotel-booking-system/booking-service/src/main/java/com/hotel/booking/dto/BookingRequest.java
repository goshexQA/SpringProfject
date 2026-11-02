package com.hotel.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class BookingRequest {
    @NotNull(message = "Room ID is required")
    private Long roomId;
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    private LocalDate endDate;
    
    private Boolean autoSelect = false;

    // астомная валидация диапазона дат
    @jakarta.validation.constraints.AssertTrue(message = "End date must be after or equal to start date")
    public boolean isDateRangeValid() {
        return startDate != null && endDate != null && !startDate.isAfter(endDate);
    }
}
