package com.hotel.hotel.config;

import com.hotel.hotel.entity.Hotel;
import com.hotel.hotel.entity.Room;
import com.hotel.hotel.repository.HotelRepository;
import com.hotel.hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;

    @Override
    public void run(String... args) {
        log.info("Initializing hotel data...");

        Hotel h1 = hotelRepository.save(Hotel.builder().name("Grand Hotel").address("Moscow, Tverskaya 1").build());
        Hotel h2 = hotelRepository.save(Hotel.builder().name("Sea Resort").address("Sochi, Beach str. 5").build());

        for (int i = 1; i <= 5; i++) {
            roomRepository.save(Room.builder().hotelId(h1.getId()).number("10" + i).available(true).timesBooked(i).build());
        }
        for (int i = 1; i <= 3; i++) {
            roomRepository.save(Room.builder().hotelId(h2.getId()).number("20" + i).available(true).timesBooked(0).build());
        }

        log.info("Data initialized: 2 hotels, 8 rooms");
    }
}
