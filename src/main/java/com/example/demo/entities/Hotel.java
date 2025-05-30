package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "hotel")
@AllArgsConstructor
@NoArgsConstructor
public class Hotel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hotel_id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "team_member_id")
    private TeamMember teamMember;

    @ManyToOne
    @JoinColumn(name = "mission_id")
    private Mission mission;

    private String name;
    private String address;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private double cost; // Total cost for the hotel stay



}
