package com.example.demo.services;

import com.example.demo.entities.Budget;
import com.example.demo.entities.Team;
import com.example.demo.enums.BudgetType;
import com.example.demo.repositories.BudgetRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;

@Service
@Transactional
@AllArgsConstructor
public class BudgetService {
    private final BudgetRepository budgetRepository;

    public Budget getOrCreateBudget(Team team, BudgetType type, double totalBudget, double perMemberBudget, int year) {
        return budgetRepository.findByTeamAndType(team, type)
                .orElseGet(() -> {
                    Budget newBudget = new Budget();
                    newBudget.setTeam(team);
                    newBudget.setType(type);
                    newBudget.setTotalBudget(totalBudget);
                    newBudget.setRemainingBudget(totalBudget);
                    newBudget.setBudgetPerMember(perMemberBudget);
                    newBudget.setYear(year);
                    team.getBudgets().add(newBudget);
                    return budgetRepository.save(newBudget);
                });
    }

    public Budget updateBudget(Budget budget, double newRemaining) {
        budget.setRemainingBudget(newRemaining);
        return budgetRepository.save(budget);
    }

    public Map<BudgetType, BigDecimal> calculatePerMemberBudgets(
            Map<BudgetType, Double> budgetsByDirector,
            int memberCount
    ) {
        Map<BudgetType, BigDecimal> perMemberBudgets = new EnumMap<>(BudgetType.class);
        for (BudgetType type : budgetsByDirector.keySet()) {
            BigDecimal totalBudget = BigDecimal.valueOf(budgetsByDirector.get(type));
            BigDecimal perMemberBudget = totalBudget.divide(
                    BigDecimal.valueOf(memberCount),
                    2,
                    RoundingMode.HALF_UP
            );
            perMemberBudgets.put(type, perMemberBudget);
        }
        return perMemberBudgets;
    }
}