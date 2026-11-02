package com.hotel.hotel.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rooms")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long hotelId;

    @Column(nullable = false)
    private String number;

    @Column(nullable = false)
    private Boolean available = true;

    @Column(nullable = false)
    private Integer timesBooked = 0;

    private LocalDateTime lockedUntil;

    private String lockedByBookingId;
}
