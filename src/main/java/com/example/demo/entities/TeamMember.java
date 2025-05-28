package com.example.demo.entities;

import com.example.demo.enums.BudgetType;
import com.example.demo.enums.Post;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NamedEntityGraph
@Entity
@Table(name = "team_member")
public class TeamMember extends Users {

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    @ToString.Exclude // Exclude from toString to prevent circular reference
    private Team team;

    @Enumerated(EnumType.STRING)
    private Post post;

    @OneToMany(mappedBy = "teamMember")
    private List<Mission> missions;

    // Training budget
    @Column(name = "training_budget_remaining")
    private BigDecimal trainingBudgetRemaining;

    // Mission budget
    @Column(name = "mission_budget_remaining")
    private BigDecimal missionBudgetRemaining;

    // Other budget
    @Column(name = "other_budget_remaining")
    private BigDecimal otherBudgetRemaining;

    public TeamMember() { }

    public TeamMember(Team team, Post post) {
        this.team = team;
        this.post = post;
    }

    // Helper method to get budget by type
    public BigDecimal getBudgetByType(BudgetType type) {
        return switch (type) {
            case TRAINING -> trainingBudgetRemaining;
            case MISSION -> missionBudgetRemaining;
            case OTHER -> otherBudgetRemaining;
        };
    }

    // Helper method to set budget by type
    public void setBudgetByType(BudgetType type, BigDecimal amount) {
        switch (type) {
            case TRAINING -> trainingBudgetRemaining = amount;
            case MISSION -> missionBudgetRemaining = amount;
            case OTHER -> otherBudgetRemaining = amount;
        }
    }
}
