package com.example.demo.entities;

import com.example.demo.enums.BudgetType;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@AllArgsConstructor
@Table(name = "team")
@NoArgsConstructor
@ToString
public class Team implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id")
    private int id;

    @Column(nullable = false)
    private String name; // Name of the team

    @OneToOne
    @JoinColumn(name = "manager_id", nullable = false)
    @ToString.Exclude // Exclude from toString to prevent circular reference
    private Manager manager; // Each Team is managed by one Manager

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude // Exclude from toString to prevent circular reference
    private List<TeamMember> teamMembers; // List of team members

    @OneToMany(mappedBy = "team")
    private List<Budget> budgets = new ArrayList<>();

    public Budget getBudgetByType(BudgetType type) {
        return budgets.stream()
                .filter(b -> b.getType() == type)
                .findFirst()
                .orElse(null); // or throw an exception if needed
    }

    public Team(String name, Manager manager) {
        this.name = name;
        this.manager = manager;
    }
}