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
                name = "UK_budget_team_type_year",
                columnNames = {"team_id", "type", "year"})) // Updated constraint name and columns
public class Budget implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "budget_id")
    private int id;

    @Column(nullable = false)
    private double totalBudget;

    @Column(nullable = false)
    private double remainingBudget;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BudgetType type;

    @Column(nullable = false)
    private double budgetPerMember;

    @Column(nullable = false)
    private int year;  // Added year column

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;
}