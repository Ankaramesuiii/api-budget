package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "team")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // Name of the team

    @OneToOne
    @JoinColumn(name = "manager_id", nullable = false)
    private Manager manager; // Each Team is managed by one Manager

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TeamMember> teamMembers; // List of team members

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BudgetCategory> budgetCategories; // Budget categories for the team

    // Constructors
    public Team() { }

    public Team(String name, Manager manager) {
        this.name = name;
        this.manager = manager;
    }
}