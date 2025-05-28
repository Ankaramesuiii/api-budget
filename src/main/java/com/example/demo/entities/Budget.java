package com.example.demo.entities;

import com.example.demo.enums.BudgetType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "budget",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_budget_team_type",
                columnNames = {"team_id", "type"}))
public class Budget implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "budget_id")
    private int id;

    @Column(nullable = false)
    private double totalBudget; // Total budget allocated to the team

    @Column(nullable = false)
    private double remainingBudget; // Remaining budget after deducting training costs

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BudgetType type;

    @Column(nullable = false)
    private double budgetPerMember;      // Evenly divided budget per team member (e.g., 10,000 TND/member)

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;
}