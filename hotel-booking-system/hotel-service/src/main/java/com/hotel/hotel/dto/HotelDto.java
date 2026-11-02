package com.hotel.hotel.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class HotelDto {
    private Long id;
    private String name;
    private String address;
}
