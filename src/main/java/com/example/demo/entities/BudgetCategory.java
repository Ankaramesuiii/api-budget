package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "budget_category")
public class BudgetCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String categoryName; // Travel, Training, Team Building, etc.

    @Column(nullable = false)
    private Double allocatedAmount; // Amount allocated for this category

    @Column(nullable = false)
    private Double consumedAmount; // Amount consumed so far for this category

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team; // Each BudgetCategory belongs to one Team

    // Constructors
    public BudgetCategory() { }

    public BudgetCategory(String categoryName, Double allocatedAmount, Team team) {
        this.categoryName = categoryName;
        this.allocatedAmount = allocatedAmount;
        this.consumedAmount = 0.0; // Initially, consumed amount is 0
        this.team = team;
    }

    // Helper method to calculate remaining amount
    public Double getRemainingAmount() {
        return allocatedAmount - consumedAmount;
    }
}