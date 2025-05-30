package com.example.demo.controllers;

import com.example.demo.dtos.HotelRequestDTO;
import com.example.demo.services.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    /**
     * Assigns a shared hotel booking to team members if they share a mission on the same start date as the check-in.
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> assignSharedHotel(@RequestBody HotelRequestDTO request) {
        try {
            hotelService.assignSharedHotel(request);
            return ResponseEntity.ok(Map.of("message", "Hotel bien reservé, Bon séjour !"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error: " + ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Unexpected error: " + ex.getMessage()));
        }
    }
}
