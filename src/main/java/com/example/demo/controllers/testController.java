package com.example.demo.controllers;

import com.example.demo.entities.test;
import com.example.demo.services.testService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class testController {
    @Autowired
    private testService testService;

    @GetMapping("/tests")
    public List<test> getAllTests() {
        List<test> tests = testService.getAllTests();
        System.out.println("Fetched tests: " + tests);
        return tests;
    }

    @GetMapping("/tests/{id}")
    public test getTestById(@PathVariable Long id) {
        return testService.getTestById(id);
    }

    @PostMapping("/tests")
    public test addTest(@RequestBody test t) {
        return testService.addTest(t);
    }
}
