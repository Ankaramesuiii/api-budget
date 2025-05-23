package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Data
@Table(name = "theme")
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "theme_id")
    private int id;

    @Column(nullable = false)
    private String name; // Name of the theme

    // Constructors
    public Theme() { }

    public Theme(String name) {
        this.name = name;
    }
}
