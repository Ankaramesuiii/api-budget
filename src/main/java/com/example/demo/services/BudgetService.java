package com.example.demo.services;

import com.example.demo.entities.*;
import com.example.demo.repositories.BudgetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class BudgetService {
    @Autowired private BudgetRepository budgetRepository;

    public Budget createDefaultBudget(Team team) {
        Budget budget = new Budget();
        budget.setTotalBudget(100000.0);
        budget.setRemainingBudget(100000.0);
        budget.setAmount(100000.0);
        budget.setTeam(team);
        return budgetRepository.save(budget);
    }

    public void updateBudgetsInBatch(Map<Team, Double> budgetUpdates) {
        List<Budget> budgets = budgetRepository.findAllById(
                budgetUpdates.keySet().stream()
                        .map(Team::getBudget)
                        .filter(Objects::nonNull)
                        .map(Budget::getId)
                        .collect(Collectors.toList())
        );

        budgets.forEach(budget -> {
            Double amount = budgetUpdates.get(budget.getTeam());
            System.out.println("Updating budget for team " + budget.getTeam().getName() + " by " + amount);
            System.out.println("Old remaining budget: " + budget.getRemainingBudget());
            System.out.println("team"+ budget.getTeam().getName());
            budget.setRemainingBudget(budget.getRemainingBudget() - amount);
        });

        budgetRepository.saveAll(budgets);
    }
}
