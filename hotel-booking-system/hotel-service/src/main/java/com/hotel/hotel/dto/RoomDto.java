package com.hotel.hotel.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class RoomDto {
    private Long id;
    private Long hotelId;
    private String number;
    private Boolean available;
    private Integer timesBooked;
}
