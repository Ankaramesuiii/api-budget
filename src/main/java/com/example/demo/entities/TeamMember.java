package com.example.demo.entities;

import com.example.demo.enums.Post;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "team_member")
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user; // Links to the Users table for authentication

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team; // Each TeamMember belongs to one Team

    private Post post; // Dev, QA, BA, PO, etc.

    // Constructors
    public TeamMember() { }

    public TeamMember(Users user, Team team, Post post) {
        this.user = user;
        this.team = team;
        this.post = post;
    }
}