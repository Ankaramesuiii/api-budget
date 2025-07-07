package com.example.demo.services;

import com.example.demo.dtos.HotelRequestDTO;
import com.example.demo.entities.Budget;
import com.example.demo.entities.Hotel;
import com.example.demo.enums.BudgetType;
import com.example.demo.repositories.BudgetRepository;
import com.example.demo.repositories.HotelRepository;

import com.example.demo.repositories.TeamMemberRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class HotelService {
    private final TeamMemberRepository teamMemberRepository;
    private final HotelRepository hotelRepository;
    private final BudgetRepository budgetRepository;

    @Transactional
    public void assignSharedHotel(HotelRequestDTO request) {
        String[] idStrings = request.getTeamMemberIds().split(",");
        List<Integer> ids = Arrays.stream(idStrings)
                .map(String::trim)
                .map(Integer::parseInt)
                .toList();

        if (ids.isEmpty()) {
            throw new IllegalArgumentException("Aucun membre d'équipe sélectionné.");
        }

        double totalCost = request.getCost();
        double costPerMember = totalCost / ids.size();

        var members = ids.stream()
                .map(id -> teamMemberRepository.findById(id.longValue())
                        .orElseThrow(() -> new IllegalArgumentException("Membre non trouvé avec id : " + id)))
                .toList();

        // Step 1: Verify common mission criteria (dates, destination, reason)
        var firstMember = members.get(0);
        var sampleMission = firstMember.getMissions().stream()
                .filter(m -> m.getStartDate().equals(request.getCheckIn()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Aucune mission trouvé pour la date: " + request.getCheckIn()));

        // Step 2: Create hotel booking for each member
        for (var member : members) {
            var remainingBudget = member.getMissionBudgetRemaining();

            // Find THIS member's mission that matches the criteria
            var memberMission = member.getMissions().stream()
                    .filter(m ->
                            m.getStartDate().equals(request.getCheckIn()) &&
                                    m.getEndDate().equals(sampleMission.getEndDate()) &&
                                    m.getDestination().equals(sampleMission.getDestination()) &&
                                    m.getReason().equals(sampleMission.getReason()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Aucune mission trouvé correspondent a votre recherche pour l'utilsateur : " + member.getId()));

            var hotel = new Hotel();
            hotel.setTeamMember(member);
            hotel.setName(request.getName());
            hotel.setCheckInDate(request.getCheckIn());
            hotel.setCheckOutDate(request.getCheckOut());
            hotel.setCost(costPerMember);
            hotel.setAddress(request.getAddress());
            hotel.setMission(memberMission);  // Assign the member's own mission

            member.setMissionBudgetRemaining(remainingBudget.subtract(BigDecimal.valueOf(costPerMember)));
            hotelRepository.save(hotel);

            // Update team budget (unchanged)
            Budget teamMissionBudget = budgetRepository.findByTeamAndTypeAndYear(
                            member.getTeam(), BudgetType.MISSION, request.getCheckIn().getYear())
                    .orElseThrow(() -> new IllegalArgumentException("No mission budget found for team."));
            teamMissionBudget.setRemainingBudget(teamMissionBudget.getRemainingBudget() - costPerMember);
        }
    }
}
