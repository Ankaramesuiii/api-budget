package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import java.time.LocalDate;

@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "training")
public class Training implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "training_id")
    private int id;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private String codeSession;

    @Column(nullable = false)
    private int duration;

    @Column(nullable = false)
    private String mode;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String presence;

    @Column(nullable = false)
    private LocalDate creationDate;

    @Column(nullable = false)
    private String codeDA;

    @Column(nullable = false)
    private boolean internalTrainer;

    @Column(nullable = false)
    private double price; // Price of the training

    @Column(nullable = false)
    private String currency; // Currency (e.g., EUR, USD, TND)

    @Column(nullable = false)
    private double exchangeRate; // Exchange rate to TND

    @Column(nullable = false)
    private double priceTND; // Price in TND (price * exchangeRate)

    @ManyToOne
    @JoinColumn(name = "team_member_id", nullable = false)
    private TeamMember teamMember; // Each Training is assigned to one TeamMember

    @ManyToOne
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme; // Each Training is associated with one Theme

}