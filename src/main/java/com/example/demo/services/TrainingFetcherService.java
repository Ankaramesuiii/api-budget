package com.example.demo.services;

import com.example.demo.entities.Training;
import com.example.demo.entities.Users;
import com.example.demo.enums.Role;

import java.util.List;

public interface TrainingFetcherService {
    boolean supports(Role role);
    List<Training> fetchTrainings(Users user);
}
