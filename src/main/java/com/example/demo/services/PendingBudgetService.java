package com.example.demo.services;

import com.example.demo.enums.BudgetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PendingBudgetService {

    private final Map<String, BigDecimal> pendingBudgets = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(PendingBudgetService.class);

    // Set or update the budget with year
    public void setOrUpdateBudget(String directorId, BigDecimal budget, BudgetType type, int year) {
        String key = buildKey(directorId, type, year);
        pendingBudgets.put(key, budget);
        if (logger.isInfoEnabled()) {
            logger.info("Current pending budgets: {}", pendingBudgets);
        }
    }

    // Get the budget (optional, in case director hasn't set it yet)
    public Optional<BigDecimal> getBudget(String directorId, BudgetType type, int year) {
        String key = buildKey(directorId, type, year);
        return Optional.ofNullable(pendingBudgets.get(key));
    }

    // Get all budgets for a director in a specific year
    public Map<BudgetType, Double> getBudgetsByDirectorAndYear(String directorId, int year) {
        String prefix = directorId + "_" + year + "_";
        return pendingBudgets.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(prefix))
                .collect(Collectors.toMap(
                        entry -> BudgetType.valueOf(entry.getKey().split("_")[2]),
                        entry -> entry.getValue().doubleValue()
                ));
    }

    // Remove all budgets for a director in a specific year
    public void clearBudgetsByYear(String directorId, int year) {
        String prefix = directorId + "_" + year + "_";
        pendingBudgets.keySet().removeIf(key -> key.startsWith(prefix));
    }

    // Remove all budgets for a director (regardless of year)
    public void clearAllBudgets(String directorId) {
        pendingBudgets.keySet().removeIf(key -> key.startsWith(directorId + "_"));
    }

    // Helper method to build consistent keys
    private String buildKey(String directorId, BudgetType type, int year) {
        return directorId + "_" + year + "_" + type;
    }
}