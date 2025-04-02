package com.example.demo.services;

import com.example.demo.entities.BusinessUnit;
import com.example.demo.repositories.BusinessUnitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class BusinessUnitService {
    @Autowired private BusinessUnitRepository businessUnitRepository;

    public BusinessUnit getOrCreate(String name) {
        return businessUnitRepository.findByName(name)
                .orElseGet(() -> businessUnitRepository.save(new BusinessUnit(name)));
    }
}
