package com.example.demo.services;

import com.example.demo.dtos.MissionRequestDTO;
import com.example.demo.entities.Budget;
import com.example.demo.entities.Mission;
import com.example.demo.entities.TeamMember;
import com.example.demo.enums.BudgetType;
import com.example.demo.repositories.BudgetRepository;
import com.example.demo.repositories.MissionRepository;
import com.example.demo.repositories.TeamMemberRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;


@Service
@Transactional
@AllArgsConstructor

public class MissionService {

    private final TeamMemberRepository teamMemberRepository;
    private final MissionRepository missionRepository;
    private final BudgetRepository budgetRepository;

    @Transactional
    public void assignSharedMission(MissionRequestDTO request) {

        String[] idStrings = request.getTeamMemberIds().split(",");
        List<Integer> ids = Arrays.stream(idStrings)
                .map(String::trim)
                .map(Integer::parseInt)
                .toList();

        double totalCost = request.getCost();

        if (ids.isEmpty()) {
            throw new IllegalArgumentException("No team members provided.");
        }

        double costPerMember = totalCost / ids.size();

        for (Integer id : ids) {
            TeamMember member = teamMemberRepository.findById(id.longValue())
                    .orElseThrow(() -> new IllegalArgumentException("Team member not found: " + id));

            BigDecimal remaining = member.getMissionBudgetRemaining();


            // Create mission
            Mission mission = new Mission();
            mission.setTeamMember(member);
            mission.setDestination(request.getDestination());
            mission.setStartDate(request.getStartDate());
            mission.setEndDate(request.getEndDate());
            mission.setReason(request.getReason());
            mission.setCost(costPerMember);
            double duree = (double) request.getEndDate().getDayOfYear() - request.getStartDate().getDayOfYear();

            mission.setPerdiem(150*duree);
            mission.setSimCost(60.00); // Default value, can be overridden
            mission.setVisaPrice(300.00); // Default value, can be overridden

            double totalMissionCost = costPerMember + mission.getPerdiem() + mission.getSimCost() + mission.getVisaPrice();
            // Deduct from individual budget
            member.setMissionBudgetRemaining(remaining.subtract(BigDecimal.valueOf(totalMissionCost)));

            missionRepository.save(mission);
            teamMemberRepository.save(member);

            // Optional: Also deduct from team-level mission budget
            Budget teamMissionBudget = budgetRepository.findByTeamAndTypeAndYear(member.getTeam(), BudgetType.MISSION,request.getStartDate().getYear())
                    .orElseThrow(() -> new IllegalArgumentException("No mission budget found for team."));

            double teamRemaining = teamMissionBudget.getRemainingBudget();


            teamMissionBudget.setRemainingBudget(teamRemaining - totalMissionCost);
            budgetRepository.save(teamMissionBudget);
        }
    }
}
