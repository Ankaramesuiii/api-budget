package com.example.demo.repositories;

import com.example.demo.entities.BusinessUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessUnitRepository extends JpaRepository<BusinessUnit, Long> {
    Optional<BusinessUnit> findByName(String name);
}
