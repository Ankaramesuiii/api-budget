package com.example.demo.services;

import com.example.demo.dtos.TrainingImportResult;
import com.example.demo.entities.*;
import com.example.demo.enums.BudgetType;

import com.example.demo.exceptions.FileMissingException;
import com.example.demo.exceptions.InvalidDirectorException;
import com.example.demo.exceptions.InvalidInputException;
import com.example.demo.repositories.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.demo.enums.BudgetType.*;
@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class TrainingService {
    private final ExcelService excelService;
    private final BudgetService budgetService;
    private final TeamService teamService;
    private final TeamMemberService teamMemberService;
    private final TrainingBuilderService trainingBuilderService;
    private final PendingBudgetService pendingBudgetService;
    private final TrainingRepository trainingRepository;

    public TrainingImportResult processExcelFile(MultipartFile file, Users user, int year) throws IOException {
        validateFile(file);
        Map<BudgetType, Double> budgetsByDirector = getDirectorBudgets(user, year);
        List<Map<String, String>> rows = excelService.readExcelFile(file);

        validateDirector(rows, user);
        Set<String> allMemberNames = extractMemberNames(rows);
        Map<BudgetType, BigDecimal> perMemberBudgets = budgetService.calculatePerMemberBudgets(
                budgetsByDirector,
                allMemberNames.size()
        );

        List<Training> trainings = processRowsByManager(
                rows,
                user,
                perMemberBudgets,
                budgetsByDirector,
                year
        );

        trainingRepository.saveAll(trainings);
        pendingBudgetService.clearAllBudgets(user.getEmail());

        String warning = generateBudgetWarning(
                BigDecimal.valueOf(budgetsByDirector.get(TRAINING)),
                calculateTotalUsedBudget(trainings)
        );

        return new TrainingImportResult(trainings.size(), warning);
    }

    // Private helper methods
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileMissingException("Le fichier est absent");
        }
    }

    private Map<BudgetType, Double> getDirectorBudgets(Users user, int year) {
        Map<BudgetType, Double> budgets = pendingBudgetService.getBudgetsByDirectorAndYear(user.getEmail(), year);
        if (budgets.isEmpty()) {
            throw new InvalidInputException("Aucun budget trouvé pour cet utilisateur");
        }
        log.info("Budgets for director: {}", budgets);
        return budgets;
    }

    private void validateDirector(List<Map<String, String>> rows, Users user) {
        String expectedName = user.getName();
        boolean allMatch = rows.stream()
                .map(r -> r.get("Directeur"))
                .allMatch(expectedName::equalsIgnoreCase);
        if (!allMatch) {
            throw new InvalidDirectorException("Le fichier contient des lignes avec un autre directeur que vous !");
        }
    }

    private Set<String> extractMemberNames(List<Map<String, String>> rows) {
        Set<String> names = rows.stream()
                .map(r -> r.get("Nom/Prénom"))
                .collect(Collectors.toSet());
        if (names.isEmpty()) {
            throw new InvalidInputException("Aucun member n'existe dans le fichier !");
        }
        return names;
    }

    private List<Training> processRowsByManager(
            List<Map<String, String>> rows,
            Users user,
            Map<BudgetType, BigDecimal> perMemberBudgets,
            Map<BudgetType, Double> budgetsByDirector,
            int year
    ) {
        List<Training> trainings = new ArrayList<>();
        Map<String, List<Map<String, String>>> rowsByManager = rows.stream()
                .collect(Collectors.groupingBy(r -> r.get("Manager")));

        for (Map.Entry<String, List<Map<String, String>>> entry : rowsByManager.entrySet()) {
            Team team = teamService.getOrCreateTeam(entry.getKey(), (SuperManager) user);
            processTeamRows(entry.getValue(), team, perMemberBudgets, budgetsByDirector, trainings, year);
        }
        return trainings;
    }

    private void processTeamRows(
            List<Map<String, String>> rows,
            Team team,
            Map<BudgetType, BigDecimal> perMemberBudgets,
            Map<BudgetType, Double> budgetsByDirector,
            List<Training> trainings,
            int year
    ) {
        Set<String> memberNames = rows.stream()
                .map(r -> r.get("Nom/Prénom"))
                .collect(Collectors.toSet());

        initializeTeamBudgets(team, memberNames.size(), perMemberBudgets, budgetsByDirector,year);

        BigDecimal teamTrainingCost = BigDecimal.ZERO;
        for (Map<String, String> row : rows) {
            TeamMember member = teamMemberService.getOrCreateMember(row, team, perMemberBudgets);
            Optional<Training> training = processTraining(row, member, team, teamTrainingCost, year);
            training.ifPresent(trainings::add);
        }
    }

    private void initializeTeamBudgets(
            Team team,
            int memberCount,
            Map<BudgetType, BigDecimal> perMemberBudgets,
            Map<BudgetType, Double> budgetsByDirector,
            int year
    ) {
        for (BudgetType type : budgetsByDirector.keySet()) {
            BigDecimal teamBudget = perMemberBudgets.get(type).multiply(BigDecimal.valueOf(memberCount));
            budgetService.getOrCreateBudget(
                    team,
                    type,
                    teamBudget.doubleValue(),
                    perMemberBudgets.get(type).doubleValue(),
                    year
            );
        }

    }

    private Optional<Training> processTraining(
            Map<String, String> row,
            TeamMember member,
            Team team,
            BigDecimal teamTrainingCost,
            int year
    ) {
        Optional<Training> existing = trainingRepository.findByCodeSessionAndTeamMember(
                row.get("Code session"), member
        );

        if (existing.isPresent()) {
            return Optional.empty();
        }

        Training training = trainingBuilderService.createTrainingFromRow(row, member);
        if (training.getStatus().contentEquals("Annulé")) {
            return Optional.empty();
        }

        processTrainingBudget(training, member, team, teamTrainingCost, year);
        return Optional.of(training);
    }

    private void processTrainingBudget(
            Training training,
            TeamMember member,
            Team team,
            BigDecimal teamTrainingCost,
            int year
    ) {
        double cost = training.getPriceTND();

        // Update member budget
        member.setTrainingBudgetRemaining(
                member.getTrainingBudgetRemaining().subtract(BigDecimal.valueOf(cost))
        );

        // Update team budget
        Budget trainingBudget = team.getBudgetByTypeAndYear(TRAINING, year);
        BigDecimal updatedRemaining = BigDecimal.valueOf(trainingBudget.getRemainingBudget())
                .subtract(BigDecimal.valueOf(cost));
        budgetService.updateBudget(trainingBudget, updatedRemaining.doubleValue());
    }

    private BigDecimal calculateTotalUsedBudget(List<Training> trainings) {
        return trainings.stream()
                .map(t -> BigDecimal.valueOf(t.getPriceTND()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generateBudgetWarning(BigDecimal totalBudget, BigDecimal usedBudget) {
        BigDecimal usagePercentage = usedBudget
                .divide(totalBudget, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        return usagePercentage.compareTo(BigDecimal.valueOf(80)) > 0
                ? String.format(
                "️ Attention : vous avez utilisé %.2f%% de votre budget de formation (%.2f TND sur %.2f TND).",
                usagePercentage,
                usedBudget,
                totalBudget
        )
                : null;
    }
}