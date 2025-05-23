package com.example.demo.dtos;

public record TrainingImportResult(
        int processedRows,
        String budgetWarningMessage
) {}
