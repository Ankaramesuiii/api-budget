package com.example.demo.repositories;

import com.example.demo.entities.TeamMember;
import com.example.demo.entities.Training;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainingRepository extends JpaRepository<Training, Long> {
    Optional<Training> findByCodeSessionAndTeamMember(String codeSession, TeamMember teamMember);
}
