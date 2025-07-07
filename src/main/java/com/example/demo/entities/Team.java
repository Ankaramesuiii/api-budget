package com.example.demo.entities;

import com.example.demo.enums.BudgetType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Slf4j
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
    @JsonIgnore
    private Manager manager; // Each Team is managed by one Manager

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude // Exclude from toString to prevent circular reference
    @JsonIgnore
    private List<TeamMember> teamMembers; // List of team members

    @OneToMany(mappedBy = "team")
    private List<Budget> budgets = new ArrayList<>();

    public Budget getBudgetByType(BudgetType type) {
        return budgets.stream()
                .filter(b -> b.getType() == type)
                .findFirst()
                .orElse(null); // or throw an exception if needed
    }

    public Budget getBudgetByTypeAndYear(BudgetType type, int year) {
        log.info("Looking for type: {}, year: {}", type, year);
        return budgets.stream()
                .filter(b -> b.getType() == type && b.getYear() == year)
                .findFirst()
                .orElse(null);
    }


    public Team(String name, Manager manager) {
        this.name = name;
        this.manager = manager;
    }
}