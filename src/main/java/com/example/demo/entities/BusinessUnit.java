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

    // Constructors
    public BusinessUnit() { }

    public BusinessUnit(String name) {
        this.name = name;
    }

}
