//package com.example.demo.services;
//
//import com.example.demo.entities.*;
//import com.example.demo.repositories.*;
//import com.github.javafaker.Faker;
//import jakarta.transaction.Transactional;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.time.LocalDate;
//import java.util.*;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
//@Service
//@Transactional
//public class TrainingService {
//
//    @Autowired private ExcelService excelService;
//    @Autowired private BusinessUnitService businessUnitService;
//    @Autowired private UserService userService;
//    @Autowired private TeamService teamService;
//    @Autowired private ThemeService themeService;
//    @Autowired private TrainingRepository trainingRepository;
//    @Autowired private BudgetService budgetService;
//
//    public int processExcelFile(MultipartFile file) throws IOException {
//        List<Map<String, String>> rows = excelService.readExcelFile(file);
//        if (rows.isEmpty()) return 0;
//
//        List<Training> trainings = new ArrayList<>();
//        Map<Team, Double> budgetUpdates = new HashMap<>();
//
//        for (Map<String, String> row : rows) {
//            Training training = processTrainingRow(row);
//            trainings.add(training);
//
//            // Track budget updates
//            budgetUpdates.merge(training.getTeamMember().getTeam(), training.getPriceTND(), Double::sum);
//        }
//
//        // Batch save trainings
//        trainingRepository.saveAll(trainings);
//
//        // Update budgets in batch
//        budgetService.updateBudgetsInBatch(budgetUpdates);
//
//        return rows.size();
//    }
//
//    private Training processTrainingRow(Map<String, String> row) {
//        BusinessUnit bu = businessUnitService.getOrCreate(row.get("BU"));
//        SuperManager sm = userService.getOrCreateSuperManager(row.get("Directeur"), bu);
//        Manager manager = userService.getOrCreateManager(row.get("Manager"), sm);
//        Team team = teamService.getOrCreate(row.get("Manager"), manager);
//        TeamMember teamMember = userService.getOrCreateTeamMember(row.get("Nom/Prénom"), team);
//        Theme theme = themeService.getOrCreate(row.get("Thématique"));
//
//        Optional<Training> existingTraining = trainingRepository.findByCodeSessionAndTeamMember(
//                row.get("Code session"), teamMember
//        );
//
//        return existingTraining.orElseGet(() -> createTrainingFromRow(row, teamMember, theme));
//    }
//
//
//    private Training createTrainingFromRow(Map<String, String> row, TeamMember teamMember, Theme theme) {
//        Training training = new Training();
//        training.setStartDate(LocalDate.parse(row.get("Date de début")));
//        training.setEndDate(LocalDate.parse(row.get("Date de fin")));
//        training.setCodeSession(row.get("Code session"));
//        training.setDuration(Integer.parseInt(row.get("Nombre de jour")));
//        training.setMode(row.get("Mode"));
//        training.setStatus(row.get("Statut"));
//        training.setPresence(row.get("Présence"));
//        training.setCreationDate(LocalDate.parse(row.get("Création demande d'achat")));
//        training.setCodeDA(row.get("Code DA"));
//        training.setInternalTrainer("Oui".equals(row.get("Formateur interne")));
//        training.setTeamMember(teamMember);
//        training.setTheme(theme);
//        training.setPriceTND(Double.parseDouble(row.get("Prix")) * Double.parseDouble(row.get("Taux de change")));
//        training.setCurrency(row.get("Devise"));
//        training.setExchangeRate(Double.parseDouble(row.get("Taux de change")));
//        return training;
//    }
//}
package com.example.demo.services;


import com.example.demo.entities.*;
import com.example.demo.enums.Post;
import com.example.demo.enums.Role;
import com.example.demo.repositories.*;
import com.github.javafaker.Faker;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class TrainingService {

    @Autowired private ExcelService excelService;
    @Autowired private BusinessUnitRepository businessUnitRepository;
    @Autowired private SuperManagerRepository superManagerRepository;
    @Autowired private ManagerRepository managerRepository;
    @Autowired private TeamRepository teamRepository;
    @Autowired private TeamMemberRepository teamMemberRepository;
    @Autowired private ThemeRepository themeRepository;
    @Autowired private TrainingRepository trainingRepository;
    @Autowired private BudgetRepository budgetRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    private final Faker faker = new Faker();
    @Autowired
    private UserService userService;
    @Autowired
    private BudgetService budgetService;

    public int processExcelFile(MultipartFile file) throws IOException {
        List<Map<String, String>> rows = excelService.readExcelFile(file);
        if (rows.isEmpty()) return 0;

        Map<String, BusinessUnit> businessUnits = businessUnitRepository.findAll()
                .stream()
                .collect(Collectors.toMap(BusinessUnit::getName, Function.identity()));

        Map<String, SuperManager> superManagers = superManagerRepository.findAll()
                .stream()
                .collect(Collectors.toMap(SuperManager::getName, Function.identity()));

        Map<String, Manager> managers = managerRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Manager::getName, Function.identity()));

        Map<String, Team> teams = teamRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Team::getName, Function.identity()));

        Map<String, TeamMember> teamMembers = teamMemberRepository.findAll()
                .stream()
                .collect(Collectors.toMap(TeamMember::getName, Function.identity()));

        Map<String, Theme> themes = themeRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Theme::getName, Function.identity()));


        List<Training> trainings = new ArrayList<>(rows.size());
        Map<Team, Double> budgetUpdates = new HashMap<>();

        for (Map<String, String> row : rows) {
            Training training = processTrainingRow(row, businessUnits, superManagers, managers, teams, teamMembers, themes);
            trainings.add(training);

            // Track budget changes
            Team team = training.getTeamMember().getTeam();
            System.out.println("Updating budget for team " + team.getName() + " by " + training.getPriceTND());
            budgetUpdates.merge(team, training.getPriceTND(), Double::sum);

            System.out.println("Budget update: " + budgetUpdates.get(team));

        }

        // 4. Batch save
        trainingRepository.saveAll(trainings);

        // 5. Update budgets
        budgetService.updateBudgetsInBatch(budgetUpdates);

        return rows.size();
    }

    private Training  processTrainingRow(Map<String, String> row,
                                         Map<String, BusinessUnit> businessUnits,
                                         Map<String, SuperManager> superManagers,
                                         Map<String, Manager> managers,
                                         Map<String, Team> teams,
                                         Map<String, TeamMember> teamMembers,
                                         Map<String, Theme> themes) {

        BusinessUnit bu = businessUnits.computeIfAbsent(row.get("BU"), name -> {
            BusinessUnit newBu = new BusinessUnit(name);
            return businessUnitRepository.save(newBu);
        });


        SuperManager sm = superManagers.computeIfAbsent(row.get("Directeur"), name -> {
            SuperManager newSm = new SuperManager(bu,name);

            userService.setUserFields(newSm, row.get("Matricule"), Role.SUPER_MANAGER);
            return superManagerRepository.save(newSm);
        });

        Manager manager = managers.computeIfAbsent(row.get("Manager"), name -> {
            Manager newManager = new Manager(sm);
            newManager.setName(name);
            userService.setUserFields(newManager, row.get("Matricule"), Role.MANAGER);
            return managerRepository.save(newManager);
        });

        Team team = teams.computeIfAbsent(row.get("Manager"), name -> {
            Team newTeam = new Team("Team "+name, manager);
            Budget defaultBudget = new Budget();
            defaultBudget.setTotalBudget(100000.0);
            defaultBudget.setRemainingBudget(100000.0);
            defaultBudget.setTeam(newTeam);
            defaultBudget.setAmount(100000.0);
            newTeam.setBudget(defaultBudget);
            return teamRepository.save(newTeam);
        });

        TeamMember teamMember = teamMembers.computeIfAbsent(row.get("Nom/Prénom"), name -> {
            TeamMember newTeamMember = new TeamMember(team, userService.getRandomPost());
            newTeamMember.setName(name);
            userService.setUserFields(newTeamMember, row.get("Matricule"), Role.TEAM_MEMBER);
            return teamMemberRepository.save(newTeamMember);
        });

        Theme theme = themes.computeIfAbsent(row.get("Thématique"), name -> {
            Theme newTheme = new Theme(name);
            return themeRepository.save(newTheme);
        });
        Optional<Training> existingTraining = trainingRepository.findByCodeSessionAndTeamMember(
                row.get("Code session"), teamMember
        );

        return existingTraining.orElseGet(() -> createTrainingFromRow(row, teamMember, theme));

    }


    private Training createTrainingFromRow(Map<String, String> row, TeamMember teamMember, Theme theme) {
        Training training = new Training();
        training.setStartDate(LocalDate.parse(row.get("Date de début")));
        training.setEndDate(LocalDate.parse(row.get("Date de fin")));
        training.setCodeSession(row.get("Code session"));
        training.setDuration(Integer.parseInt(row.get("Nombre de jour")));
        training.setMode(row.get("Mode"));
        training.setStatus(row.get("Statut"));
        training.setPresence(row.get("Présence"));
        training.setCreationDate(LocalDate.parse(row.get("Création demande d'achat")));
        training.setCodeDA(row.get("Code DA"));
        training.setInternalTrainer(row.get("Formateur interne").equals("Oui"));
        training.setTeamMember(teamMember);
        training.setTheme(theme);
        training.setPriceTND(Double.parseDouble(row.get("Prix_TND")));
        training.setCurrency(row.get("Devise"));
        training.setExchangeRate(Double.parseDouble(row.get("Taux de change")));
        training.setPrice(Double.parseDouble(row.get("Prix")));
        return training;
    }

}