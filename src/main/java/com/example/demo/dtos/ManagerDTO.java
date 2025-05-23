package com.example.demo.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class ManagerDTO {
    private String name;
    private String email;
    private List<String> teamMembers;
    private String teamName;
    private String superManager;
    private double remainingBudget;
    private double teamBudget;
    private String businessUnit;
}
