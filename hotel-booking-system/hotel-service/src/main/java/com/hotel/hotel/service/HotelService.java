package com.hotel.hotel.service;

import com.hotel.hotel.dto.HotelDto;
import com.hotel.hotel.entity.Hotel;
import com.hotel.hotel.exception.ResourceNotFoundException;
import com.hotel.hotel.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;

    @Transactional
    public HotelDto createHotel(HotelDto dto) {
        Hotel hotel = Hotel.builder()
                .name(dto.getName())
                .address(dto.getAddress())
                .build();
        hotel = hotelRepository.save(hotel);
        return toDto(hotel);
    }

    public List<HotelDto> getAllHotels() {
        return hotelRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    private HotelDto toDto(Hotel h) {
        return HotelDto.builder()
                .id(h.getId())
                .name(h.getName())
                .address(h.getAddress())
                .build();
    }
}
