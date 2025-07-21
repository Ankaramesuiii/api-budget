package com.example.demo.services;

import com.example.demo.dtos.TeamMemberDTO;
import com.example.demo.entities.Team;
import com.example.demo.entities.TeamMember;
import com.example.demo.enums.BudgetType;
import com.example.demo.enums.Role;
import com.example.demo.repositories.TeamMemberRepository;
import com.example.demo.repositories.ThemeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

import static com.example.demo.enums.BudgetType.*;

@Service
@AllArgsConstructor
public class TeamMemberService {
    private final TeamMemberRepository teamMemberRepository;
    private final UserService userService;
    private final ThemeRepository themeRepository;

    public TeamMember getOrCreateMember(
            Map<String, String> row,
            Team team,
            Map<BudgetType, BigDecimal> perMemberBudgets,
            boolean isFirstUpload
    ) {
        return teamMemberRepository.findByName(row.get("Nom/Prénom"))
                .orElseGet(() -> createNewMember(row, team, perMemberBudgets, isFirstUpload));
    }

    private TeamMember createNewMember(
            Map<String, String> row,
            Team team,
            Map<BudgetType, BigDecimal> perMemberBudgets,
            boolean isFirstUpload
    ) {
        TeamMember member = new TeamMember(team, userService.getRandomPost());
        member.setName(row.get("Nom/Prénom"));
        userService.setUserFields(member, row.get("Matricule"), Role.TEAM_MEMBER);

        if (isFirstUpload) {
            member.setTrainingBudgetRemaining(perMemberBudgets.get(TRAINING));
            member.setMissionBudgetRemaining(perMemberBudgets.get(MISSION));
            member.setOtherBudgetRemaining(perMemberBudgets.get(OTHER));
        } else {
            member.setTrainingBudgetRemaining(BigDecimal.ZERO);
            member.setMissionBudgetRemaining(BigDecimal.ZERO);
            member.setOtherBudgetRemaining(BigDecimal.ZERO);
        }

        return teamMemberRepository.save(member);
    }



    public static TeamMemberDTO getTeamMemberDTO(TeamMember teamMember) {
        return new TeamMemberDTO(
                teamMember.getId(),
                teamMember.getName(),
                teamMember.getEmail(),
                teamMember.getPost().name(),
                teamMember.getPhone(),
                teamMember.getTeam().getManager().getName(),
                teamMember.getTeam().getManager().getSuperManager().getName(),
                teamMember.getTeam().getManager().getSuperManager().getBusinessUnit().getName(),
                teamMember.getTrainingBudgetRemaining().toString(),
                teamMember.getMissionBudgetRemaining().toString(),
                teamMember.getOtherBudgetRemaining().toString()
        );
    }
}

