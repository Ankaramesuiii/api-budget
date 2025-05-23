package com.example.demo.repositories;

import com.example.demo.entities.Manager;
import com.example.demo.entities.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByName(String name);
    Optional<Team> findByManager(Manager manager);
}
