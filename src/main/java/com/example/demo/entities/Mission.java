package com.example.demo.entities;


import jakarta.persistence.*;

import java.time.LocalDate;

public class Mission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "team_member_id")
    private TeamMember teamMember;

    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;

}