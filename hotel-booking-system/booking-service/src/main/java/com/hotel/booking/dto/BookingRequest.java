package com.hotel.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class BookingRequest {
    @NotNull
    private Long roomId;
    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
    private Boolean autoSelect = false;
}
