package com.example.demo.services;

import com.example.demo.entities.*;
import com.example.demo.enums.BudgetType;
import com.example.demo.enums.Role;
import com.example.demo.repositories.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@AllArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;
    private final ManagerRepository managerRepository;
    private final UserService userService;

    public Team getOrCreateTeam(String managerName, SuperManager superManager) {
        Manager manager = managerRepository.findByName(managerName)
                .orElseGet(() -> createNewManager(managerName, superManager));

        return teamRepository.findByManager(manager)
                .orElseGet(() -> createNewTeam(manager));
    }

    private Manager createNewManager(String name, SuperManager superManager) {
        Manager manager = new Manager(superManager);
        manager.setName(name);
        userService.setUserFields(manager, userService.getCuid("M-"), Role.MANAGER);
        return managerRepository.save(manager);
    }

    private Team createNewTeam(Manager manager) {
        Team team = new Team("Team " + manager.getName(), manager);
        return teamRepository.save(team);
    }
}