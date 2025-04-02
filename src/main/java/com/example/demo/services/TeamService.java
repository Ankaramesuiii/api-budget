package com.example.demo.services;

import com.example.demo.entities.*;
import com.example.demo.repositories.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private BudgetService budgetService;


    @Transactional  // Ensure everything happens within a transaction
    public Team getOrCreate(String managerName, Manager manager) {
        String teamName = "Team " + managerName;
        return teamRepository.findByName(teamName)
                .orElseGet(() -> {
                    Team team = new Team(teamName, manager);
                    Budget budget = new Budget();
                    budget.setTotalBudget(100000.0);
                    budget.setRemainingBudget(100000.0);
                    budget.setAmount(100000.0);
                    budget.setTeam(team);
                    team.setBudget(budget);
                    return teamRepository.save(team);
                });
    }
}