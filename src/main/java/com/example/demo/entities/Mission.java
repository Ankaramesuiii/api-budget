package com.example.demo.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "mission")
public class Mission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mission_id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "team_member_id")
    private TeamMember teamMember;

    private String destination;
    private LocalDate startDate;
    @Column(nullable = true)
    private LocalDate endDate;
    private String reason;

    private double cost;
}