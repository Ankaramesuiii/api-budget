package com.example.demo.entities;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "super_manager")
public class SuperManager extends Users {

    @OneToOne
    @JoinColumn(name = "business_unit_id", nullable = false)
    private BusinessUnit businessUnit; // Each SuperManager manages one BU

    @OneToMany(mappedBy = "superManager", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Manager> managers; // SuperManager oversees 3-6 Managers

    @OneToOne(mappedBy = "superManager", cascade = CascadeType.ALL)
    private Budget budget; // SuperManager sets the budget

    public SuperManager() { }

    public SuperManager(Users user,BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }
}
