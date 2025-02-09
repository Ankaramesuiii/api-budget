package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "manager")
public class Manager {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user; // Links to the Users table for authentication

    @ManyToOne
    @JoinColumn(name = "super_manager_id", nullable = false)
    private SuperManager superManager; // Each Manager reports to a SuperManager

    @OneToOne(mappedBy = "manager", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Team team; // Each Manager manages one Team

    @ManyToOne
    @JoinColumn(name = "budget_id", nullable = false)
    private Budget budget; // Each Manager is allocated a budget

    // Constructors
    public Manager() { }

    public Manager(Users user, SuperManager superManager, Budget budget) {
        this.user = user;
        this.superManager = superManager;
        this.budget = budget;
    }
}