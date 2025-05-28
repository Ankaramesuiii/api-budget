package com.example.demo.dtos;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MissionRequestDTO {
    private String teamMemberIds;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private double cost; // Total cost shared by all
}

