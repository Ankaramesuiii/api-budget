package com.example.demo.services.fetchers;

import com.example.demo.services.TrainingFetcherService;
import com.example.demo.entities.*;
import com.example.demo.enums.Role;
import com.example.demo.repositories.TrainingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class SuperManagerTrainingFetcher implements TrainingFetcherService {

    private final TrainingRepository trainingRepository;

    public SuperManagerTrainingFetcher(TrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }

    @Override
    public boolean supports(Role role) {
        return Role.SUPER_MANAGER.equals(role);
    }

    @Override
    public List<Training> fetchTrainings(Users user) {
        SuperManager superManager = (SuperManager) user;
        List<Manager> managers = superManager.getManagers();

        List<Team> teams = managers.stream()
                .map(Manager::getTeam)
                .filter(Objects::nonNull)
                .toList();

        List<TeamMember> teamMembers = teams.stream()
                .flatMap(team -> team.getTeamMembers().stream())
                .toList();

        return trainingRepository.findByTeamMemberIn(teamMembers);
    }
}

