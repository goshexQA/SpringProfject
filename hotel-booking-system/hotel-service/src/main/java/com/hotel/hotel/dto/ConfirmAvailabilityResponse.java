package com.hotel.hotel.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ConfirmAvailabilityResponse {
    private boolean success;
    private String message;
}
