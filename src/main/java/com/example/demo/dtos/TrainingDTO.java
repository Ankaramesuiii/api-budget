package com.example.demo.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor

public class TrainingDTO {
    private int id;
    private LocalDate startDate;
    private LocalDate endDate;
    private String codeSession;
    private int duration;
    private String mode;
    private String status;
    private String presence;
    private LocalDate creationDate;
    private String codeDA;
    private boolean internalTrainer;
    private double price;
    private String currency;
    private double exchangeRate;
    private double priceTND;
    private String themeName;
    private String teamMemberName;
}