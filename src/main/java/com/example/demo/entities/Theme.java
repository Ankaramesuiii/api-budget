package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;


@Entity
@Data
@Table(name = "theme")
public class Theme implements Serializable {

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
