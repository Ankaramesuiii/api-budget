package com.example.demo.services.fetchers;


import com.example.demo.services.TrainingFetcherService;
import com.example.demo.entities.*;
import com.example.demo.enums.Role;
import com.example.demo.repositories.TrainingRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ManagerTrainingFetcher implements TrainingFetcherService {

    private final TrainingRepository trainingRepository;

    public ManagerTrainingFetcher(TrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }

    @Override
    public boolean supports(Role role) {
        return Role.MANAGER.equals(role);
    }

    @Override
    public List<Training> fetchTrainings(Users user) {
        Manager manager = (Manager) user;
        Team team = manager.getTeam();

        if (team == null) return Collections.emptyList();

        List<TeamMember> teamMembers = team.getTeamMembers();
        return trainingRepository.findByTeamMemberIn(teamMembers);
    }
}

