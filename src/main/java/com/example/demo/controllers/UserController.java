package com.example.demo.controllers;

import com.example.demo.dtos.TeamMemberDTO;
import com.example.demo.dtos.TrainingDTO;
import com.example.demo.entities.*;
import com.example.demo.enums.Role;
import com.example.demo.repositories.UsersRepository;
import com.example.demo.services.ProfileService;
import com.example.demo.services.TeamMemberService;
import com.example.demo.services.TrainingRetrievalService;
import com.example.demo.utils.TrainingMapper;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.demo.enums.Role.TEAM_MEMBER;


@RestController
@RequestMapping("/user")
@AllArgsConstructor
public class UserController {

    private final UsersRepository usersRepository;
    private final TrainingMapper trainingMapper;
    private final TrainingRetrievalService trainingRetrievalService;
    private final ProfileService profileService;

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("/")
    public Map<String, Object> dashboard() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // Get only the username

        Map<String, Object> response = new HashMap<>();
        response.put("username", username);
        response.put("roles", authentication.getAuthorities());

        return response;
    }

    @GetMapping("/team-members")
    public Map<String, Object> teamMembers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Users user = usersRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        List<TeamMember> teamMembers = new ArrayList<>();
        if (user.getRole() == Role.MANAGER) {
            Manager manager = (Manager) user;
            teamMembers = manager.getTeam().getTeamMembers();
        }
        List<TeamMemberDTO> teamMemberDTOs = teamMembers.stream()
                .map(TeamMemberService::getTeamMemberDTO)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("teamMembers", teamMemberDTOs);
        System.out.println("Team members: " + teamMemberDTOs);
        return response;
    }


    @GetMapping("/training")
    public Map<String, Object> training() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Users user = usersRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        List<Training> trainingList = trainingRetrievalService.getTrainingsForUser(user);
        List<TrainingDTO> trainingDTOs = trainingList.stream()
                .map(training -> trainingMapper.toDto(training, user))
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("training", trainingDTOs);

        return response;
    }


    @GetMapping("/profile")
    public Map<String, Object> profile() {
        return profileService.getUserProfile();
    }


}

