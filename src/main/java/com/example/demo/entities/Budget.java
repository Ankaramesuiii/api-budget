package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "budget")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double totalAmount; // Total budget amount set by SuperManager

    @OneToOne
    @JoinColumn(name = "super_manager_id", nullable = false)
    private SuperManager superManager; // Budget is set by the SuperManager

    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Manager> managers; // Budget is allocated to Managers

    // Constructors
    public Budget() { }

    public Budget(Double totalAmount, SuperManager superManager) {
        this.totalAmount = totalAmount;
        this.superManager = superManager;
    }
}