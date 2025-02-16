package com.example.demo.entities;

import com.example.demo.enums.Post;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.userdetails.User;

@Setter
@Getter
@Entity
@Table(name = "team_member")
public class TeamMember extends Users {
    private Long id;
    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team; // Each TeamMember belongs to one Team

    private Post post; // Dev, QA, BA, PO, etc.

    // Constructors
    public TeamMember() { }

    public TeamMember(Team team, Post post) {
        this.team = team;
        this.post = post;
    }
}