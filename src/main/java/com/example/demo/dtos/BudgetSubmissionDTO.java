package com.example.demo.dtos;

import com.example.demo.enums.BudgetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BudgetSubmissionDTO {
    private int year;
    private Map<BudgetType, Double> budgets;
}
