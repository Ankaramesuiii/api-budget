package com.example.demo.repositories;

import com.example.demo.entities.Budget;
import com.example.demo.entities.Team;
import com.example.demo.enums.BudgetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Budget findByTeam(Team team);
    List<Budget> findByTeamIn(Collection<Team> teams);

    @Modifying
    @Query("UPDATE Budget b SET b.remainingBudget = b.remainingBudget - :amount WHERE b.team.id = :teamId")
    void updateRemainingBudget(@Param("teamId") Long teamId, @Param("amount") Double amount);

    Optional<Budget> findByTeamAndType(Team team, BudgetType type);
    Optional<Budget> findByTeamIdAndType(int teamId, BudgetType type);
    
    Optional<Budget> findByTeamAndTypeAndYear(Team team, BudgetType type, int year);
    Optional<Budget> findByTeamIdAndTypeAndYear(int teamId, BudgetType type, int year);

}
