package com.example.demo.services;

import com.example.demo.dtos.ManagerDTO;
import com.example.demo.dtos.SuperManagerDTO;
import com.example.demo.dtos.TeamMemberDTO;
import com.example.demo.entities.*;
import com.example.demo.enums.BudgetType;
import com.example.demo.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProfileService {

    @Autowired
    private UsersRepository usersRepository;

    public Map<String, Object> getUserProfile() {
        // Get the current authenticated user's email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Fetch user from the database
        Users user = usersRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Prepare the response map
        Map<String, Object> response = new HashMap<>();

        // Handle each role type and return corresponding DTO
        switch (user.getRole()) {
            case MANAGER:
                Manager manager = (Manager) user;
                Team team = manager.getTeam();

                // Get the TRAINING budget (or whichever type you need)
                Budget trainingBudget = team.getBudgetByType(BudgetType.TRAINING);

                ManagerDTO managerDTO = new ManagerDTO(
                        manager.getName(),
                        manager.getEmail(),
                        getTeamMembers(manager),
                        manager.getTeam().getName(),
                        manager.getSuperManager().getName(),
                        trainingBudget.getRemainingBudget(),
                        trainingBudget.getTotalBudget(),

                        manager.getSuperManager().getBusinessUnit().getName());
                response.put("manager", managerDTO);
                break;

            case SUPER_MANAGER:
                SuperManager superManager = (SuperManager) user;
                SuperManagerDTO superManagerDTO = new SuperManagerDTO(
                        superManager.getName(),
                        superManager.getEmail(),
                        superManager.getBusinessUnit().getName(),
                        getManagers(superManager));
                        //getTeams(superManager.getManagers()),
                        //getBudgets(superManager.getManagers()));
                response.put("superManager", superManagerDTO);
                break;

            case TEAM_MEMBER:
                TeamMemberDTO teamMemberDTO = getTeamMemberDTO((TeamMember) user);
                response.put("teamMember", teamMemberDTO);
                break;

            default:
                response.put("message", "Unknown role");
        }
        response.put("avatar", generateAvatar(user));  // To generate the initials

        return response;
    }

    private static TeamMemberDTO getTeamMemberDTO(TeamMember user) {
        return new TeamMemberDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPost().name(),
                user.getPhone(),
                user.getTeam().getManager().getName(),
                user.getTeam().getManager().getSuperManager().getName(),
                user.getTeam().getManager().getSuperManager().getBusinessUnit().getName(),
                user.getTrainingBudgetRemaining().toString(),
                user.getMissionBudgetRemaining().toString(),
                user.getOtherBudgetRemaining().toString()
                );
    }

    private String getBudgetPerMemberRemaining(TeamMember teamMember) {
        BigDecimal memberMissionBudgetRemaining = teamMember.getMissionBudgetRemaining();
        BigDecimal memberTrainingBudgetRemaining = teamMember.getTrainingBudgetRemaining();
        BigDecimal memberOtherBudgetRemaining = teamMember.getOtherBudgetRemaining();
        return "Mission Budget Remaining: " + memberMissionBudgetRemaining +
                ", Training Budget Remaining: " + memberTrainingBudgetRemaining +
                ", Other Budget Remaining: " + memberOtherBudgetRemaining;
    }
    private Map<BudgetType, Double> getBudgets(Team team) {
        Map<BudgetType, Double> budgetMap = new HashMap<>();
        for (Budget budget : team.getBudgets()) {
            budgetMap.put(budget.getType(), budget.getRemainingBudget());
        }
        return budgetMap;
    }

    private List<Team> getTeams(List<Manager> managers) {
        List<Team> teams = new ArrayList<>();
        for (Manager manager : managers) {
            teams.add(manager.getTeam());
        }
        return teams;
    }

    private String generateAvatar(Users user) {
        String name = user.getName();
        String[] nameParts = name.split(" ");
        String initials = "";
        for (String part : nameParts) {
            if (!part.isEmpty()) {
                initials += part.charAt(0);
            }
        }
        return initials.toUpperCase();
    }
    // Helper method to get team members for a manager
    private List<String> getTeamMembers(Manager manager) {
        List<String> teamMembers = new ArrayList<>();
        for (TeamMember teamMember : manager.getTeam().getTeamMembers()) {
            teamMembers.add(teamMember.getName());
        }
        return teamMembers;
    }

    // Helper method to get managers under a SuperManager
    private List<String> getManagers(SuperManager superManager) {
        List<String> managers = new ArrayList<>();
        for (Manager manager : superManager.getManagers()) {
            managers.add(manager.getName());
        }
        return managers;
    }
}

