package com.example.demo.repositories;

import com.example.demo.entities.test;
import org.springframework.data.jpa.repository.JpaRepository;

public interface testRepository extends JpaRepository<test,Long> {
}
