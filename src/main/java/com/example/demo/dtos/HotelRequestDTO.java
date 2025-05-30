package com.example.demo.dtos;

import lombok.Data;

import java.time.LocalDate;

@Data
public class HotelRequestDTO {
    private String teamMemberIds; // Comma-separated list of team member IDs
    private String name;
    private String address;
    private LocalDate checkIn; // Format: YYYY-MM-DD
    private LocalDate checkOut; // Format: YYYY-MM-DD
    private double cost; // Cost per night per person
}
