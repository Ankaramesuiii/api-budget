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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
            if (remaining.compareTo(BigDecimal.valueOf(costPerMember)) < 0) {
                throw new IllegalArgumentException("Not enough mission budget for team member: " + id);
            }

            // Create mission
            Mission mission = new Mission();
            mission.setTeamMember(member);
            mission.setDestination(request.getDestination());
            mission.setStartDate(request.getStartDate());
            mission.setEndDate(request.getEndDate());
            mission.setReason(request.getReason());
            mission.setCost(costPerMember);

            // Deduct from individual budget
            member.setMissionBudgetRemaining(remaining.subtract(BigDecimal.valueOf(costPerMember)));

            missionRepository.save(mission);
            teamMemberRepository.save(member);

            // Optional: Also deduct from team-level mission budget
            Budget teamMissionBudget = budgetRepository.findByTeamAndType(member.getTeam(), BudgetType.MISSION)
                    .orElseThrow(() -> new IllegalArgumentException("No mission budget found for team."));

            double teamRemaining = teamMissionBudget.getRemainingBudget();
            if (teamRemaining < costPerMember) {
                throw new IllegalArgumentException("Not enough team mission budget.");
            }

            teamMissionBudget.setRemainingBudget(teamRemaining - costPerMember);
            budgetRepository.save(teamMissionBudget);
        }
    }
}
