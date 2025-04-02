package com.example.demo.entities;


import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Entity
@Table(name = "business_unit")
public class BusinessUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // Name of the business unit

    @OneToOne(mappedBy = "businessUnit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private SuperManager superManager; // Each BU has one SuperManager

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SuperManager getSuperManager() {
        return superManager;
    }

    public void setSuperManager(SuperManager superManager) {
        this.superManager = superManager;
    }

    // Constructors
    public BusinessUnit() { }

    public BusinessUnit(String name) {
        this.name = name;
    }

}
