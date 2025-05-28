package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@Entity
@Table(name = "manager")
public class Manager extends Users {

    @ManyToOne
    @JoinColumn(name = "super_manager_id", nullable = false)
    @ToString.Exclude // Exclude from toString to prevent circular reference
    private SuperManager superManager; // Each Manager reports to a SuperManager

    @OneToOne(mappedBy = "manager", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude // Exclude from toString to prevent circular reference
    private Team team; // Each Manager manages one Team

    // Constructors
    public Manager() { }

    public Manager(SuperManager superManager) {
        this.superManager = superManager;
    }

    public SuperManager getSuperManager() {
        return superManager;
    }

    public void setSuperManager(SuperManager superManager) {
        this.superManager = superManager;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }
}