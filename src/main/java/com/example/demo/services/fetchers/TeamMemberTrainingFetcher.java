package com.example.demo.services.fetchers;

import com.example.demo.services.TrainingFetcherService;
import com.example.demo.entities.TeamMember;
import com.example.demo.entities.Training;
import com.example.demo.entities.Users;
import com.example.demo.enums.Role;
import com.example.demo.repositories.TrainingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamMemberTrainingFetcher implements TrainingFetcherService {

    private final TrainingRepository trainingRepository;

    public TeamMemberTrainingFetcher(TrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }

    @Override
    public boolean supports(Role role) {
        return Role.TEAM_MEMBER.equals(role);
    }

    @Override
    public List<Training> fetchTrainings(Users user) {
        TeamMember teamMember = (TeamMember) user;
        return trainingRepository.findByTeamMember(teamMember)
                .orElseThrow(() -> new RuntimeException("No training found for this user"));
    }
}

