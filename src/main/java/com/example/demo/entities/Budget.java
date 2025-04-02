package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "budget")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private double totalBudget; // Total budget allocated to the team

    @Column(nullable = false)
    private double remainingBudget; // Remaining budget after deducting training costs

    @Column(nullable = false)
    private double amount; // Total budget allocated to the team

    @OneToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team; // Each Budget is assigned to one Team

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getTotalBudget() {
        return totalBudget;
    }

    public void setTotalBudget(double totalBudget) {
        this.totalBudget = totalBudget;
    }

    public double getRemainingBudget() {
        return remainingBudget;
    }

    public void setRemainingBudget(double remainingBudget) {
        this.remainingBudget = remainingBudget;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    // Constructors
    public Budget() { }

    public Budget(double totalBudget, Team team) {
        this.totalBudget = totalBudget;
        this.remainingBudget = totalBudget; // Initially, remainingBudget = totalBudget
        this.team = team;
    }

}