package com.example.demo.repositories;

import com.example.demo.entities.SuperManager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SuperManagerRepository extends JpaRepository<SuperManager, Long> {
    Optional<SuperManager> findByName(String name);

}
