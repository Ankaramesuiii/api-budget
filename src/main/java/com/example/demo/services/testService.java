package com.example.demo.services;

import com.example.demo.entities.test;
import com.example.demo.repositories.testRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;


@Service
public class testService {

    private static final Logger logger = LoggerFactory.getLogger(testService.class);
    @Autowired
    private testRepository testRepository;

    public List<test> getAllTests() {
        logger.info(testRepository.findAll().toString());
        return testRepository.findAll();  // This fetches all the records from the "test" table
    }

    public test getTestById(Long id) {
        return testRepository.findById(id).orElse(null);  // Fetches a specific record by ID
    }

    public test addTest(test t) {
        return testRepository.save(t);
    }
}
