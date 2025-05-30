package com.example.demo.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "mission")
public class Mission implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mission_id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "team_member_id")
    private TeamMember teamMember;
    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL)
    private List<Hotel> hotels;
    private String destination;
    private LocalDate startDate;
    @Column(nullable = true)
    private LocalDate endDate;
    private String reason;

    private double cost;

    private double perdiem = 45.00; // Default value, can be overridden
    private double visaPrice = 300.00; // Default value, can be overridden
    private double simCost = 20.00; // Default value, can be overridden
}