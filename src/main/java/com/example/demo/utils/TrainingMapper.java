package com.example.demo.utils;

import com.example.demo.dtos.TrainingDTO;
import com.example.demo.entities.Training;
import com.example.demo.entities.Users;
import com.example.demo.enums.Role;
import org.springframework.stereotype.Component;

@Component
public class TrainingMapper {

    public TrainingDTO toDto(Training training, Users loggedInUser) {
        TrainingDTO dto = new TrainingDTO();
        dto.setId(training.getId());
        dto.setStartDate(training.getStartDate());
        dto.setEndDate(training.getEndDate());
        dto.setCodeSession(training.getCodeSession());
        dto.setDuration(training.getDuration());
        dto.setMode(training.getMode());
        dto.setStatus(training.getStatus());
        dto.setPresence(training.getPresence());
        dto.setCreationDate(training.getCreationDate());
        dto.setCodeDA(training.getCodeDA());
        dto.setInternalTrainer(training.isInternalTrainer());
        dto.setPrice(training.getPrice());
        dto.setCurrency(training.getCurrency());
        dto.setExchangeRate(training.getExchangeRate());
        dto.setPriceTND(training.getPriceTND());
        dto.setThemeName(training.getTheme().getName());

        if (loggedInUser.getRole() == Role.MANAGER || loggedInUser.getRole() == Role.SUPER_MANAGER) {
            dto.setTeamMemberName(training.getTeamMember().getName());
        }

        return dto;
    }

}