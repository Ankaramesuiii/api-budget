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

    public SuperManager() { }

    public SuperManager(BusinessUnit businessUnit,String name) {
        this.businessUnit = businessUnit;
        this.setName(name);
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public List<Manager> getManagers() {
        return managers;
    }

    public void setManagers(List<Manager> managers) {
        this.managers = managers;
    }
}
