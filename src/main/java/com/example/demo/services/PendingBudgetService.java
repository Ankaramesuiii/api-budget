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
    // Set or update the budget
    public void setOrUpdateBudget(String directorId, BigDecimal budget, BudgetType type) {
        pendingBudgets.put(directorId +"_"+ type, budget);
        if (logger.isInfoEnabled()) {
            logger.info("Current pending budgets: {}", pendingBudgets);
        }
    }

    // Get the budget (optional, in case director hasn't set it yet)
    public Optional<BigDecimal> getBudget(String directorId, BudgetType type) {
        return Optional.ofNullable(pendingBudgets.get(directorId +"_"+ type));
    }

    public Map<BudgetType, Double> getBudgetsByDirector(String directorId) {
        return pendingBudgets.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(directorId + "_"))
                .collect(Collectors.toMap(
                        entry -> BudgetType.valueOf(entry.getKey().split("_")[1]),
                        entry -> entry.getValue().doubleValue() // Conversion explicite de BigDecimal en Double
                ));
    }

    // Remove the budget after successful import
    public void clearAllBudgets(String directorId) {
        pendingBudgets.keySet().removeIf(key -> key.startsWith(directorId + "_"));
    }


}

