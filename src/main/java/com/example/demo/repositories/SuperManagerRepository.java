package com.example.demo.repositories;

import com.example.demo.entities.SuperManager;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuperManagerRepository extends JpaRepository<SuperManager, Long> {
}
