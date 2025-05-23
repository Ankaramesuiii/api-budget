package com.example.demo.utils;

import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainingRowDto {
    private String nomPrenom;
    private String directeur;
    private String bu;
    private String manager;
    private String theme;
    private LocalDate date;
}