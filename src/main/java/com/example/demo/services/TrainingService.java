package com.example.demo.services;

import com.example.demo.dtos.TrainingImportResult;
import com.example.demo.entities.*;
import com.example.demo.enums.BudgetType;

import com.example.demo.exceptions.DuplicateTrainingException;
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

        Map<BudgetType, Double> budgetsByDirector = pendingBudgetService.getBudgetsByDirectorAndYear(user.getEmail(), year);
        log.info("zzz {}", budgetsByDirector.toString());
        boolean isFirstUpload = !budgetsByDirector.isEmpty();

        List<Map<String, String>> rows = excelService.readExcelFile(file);

        validateDirector(rows, user);

        ExcelService.validate(rows, ExcelHeaders.REQUIRED_HEADERS);

        Set<String> allMemberNames = extractMemberNames(rows);

        Map<BudgetType, BigDecimal> perMemberBudgets = isFirstUpload
                ? budgetService.calculatePerMemberBudgets(budgetsByDirector, allMemberNames.size())
                : Map.of(TRAINING, BigDecimal.ZERO, MISSION, BigDecimal.ZERO, OTHER, BigDecimal.ZERO);

        log.info("zzz Per member budgets: {}", perMemberBudgets);
        List<Training> trainings = processRowsByManager(
                rows,
                user,
                perMemberBudgets,
                budgetsByDirector,
                isFirstUpload,
                year
        );
        if (trainings.isEmpty()) {
            throw new DuplicateTrainingException("Toutes les lignes du fichier existent déjà. Aucun ajout effectué.");
        }
        trainingRepository.saveAll(trainings);

        pendingBudgetService.clearAllBudgets(user.getEmail());


        Double trainingBudgetDouble = budgetsByDirector.getOrDefault(TRAINING, 0.0);
        if (trainingBudgetDouble == 0.0) {
            // Pas de budget formation alloué, pas besoin d’avertissement
            return new TrainingImportResult(trainings.size(), "");
        }
        BigDecimal trainingBudget = BigDecimal.valueOf(trainingBudgetDouble);
        String warning = generateBudgetWarning(trainingBudget, calculateTotalUsedBudget(trainings));
        return new TrainingImportResult(trainings.size(), warning != null ? warning : "");

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
            throw new InvalidInputException("Aucun membre n'existe dans le fichier !");
        }
        return names;
    }

    private List<Training> processRowsByManager(
            List<Map<String, String>> rows,
            Users user,
            Map<BudgetType, BigDecimal> perMemberBudgets,
            Map<BudgetType, Double> budgetsByDirector,
            boolean isFirstUpload,
            int year
    ) {
        List<Training> trainings = new ArrayList<>();
        Map<String, List<Map<String, String>>> rowsByManager = rows.stream()
                .collect(Collectors.groupingBy(r -> r.get("Manager")));
        for (Map.Entry<String, List<Map<String, String>>> entry : rowsByManager.entrySet()) {
            log.info("zzz Processing manager: {}", entry.getKey());
            Team team = teamService.getOrCreateTeam(entry.getKey(), (SuperManager) user);
            log.info("Processing team: {}", team.getName());
            List<Map<String, String>> uniqueRows = getUniqueRows(entry);
            if (isFirstUpload) {
                initializeTeamBudgets(team, uniqueRows.size(), perMemberBudgets, budgetsByDirector, year);
            } else {
                if (team.getBudgetByTypeAndYear(TRAINING, year) == null) {
                    for (BudgetType type : new BudgetType[]{BudgetType.TRAINING, BudgetType.MISSION, BudgetType.OTHER}) {
                        budgetService.getOrCreateBudget(team, type, 0.0, 0.0, year);
                    }
                }
            }

            for (Map<String, String> row : entry.getValue()) {
                TeamMember member = teamMemberService.getOrCreateMember(row, team, perMemberBudgets, isFirstUpload);
                Optional<Training> training = processTraining(row, member, team, year);
                training.ifPresent(trainings::add);
            }
        }
        return trainings;
    }

    private List<Map<String, String>> getUniqueRows(Map.Entry<String, List<Map<String, String>>> entry) {
        // Use the "Nom/Prénom" column as the key
        // Use the entire row as the value
        // Keep the first occurrence
        return entry.getValue().stream()
                .collect(Collectors.toMap(
                        row -> row.get("Nom/Prénom"),
                        row -> row,
                        (existing, replacement) -> existing
                ))
                .values()
                .stream()
                .toList();
    }

    private void initializeTeamBudgets(
            Team team,
            int memberCount,
            Map<BudgetType, BigDecimal> perMemberBudgets,
            Map<BudgetType, Double> budgetsByDirector,
            int year
    ) {

        for (BudgetType type : budgetsByDirector.keySet()) {
            BigDecimal perMemberBudget = perMemberBudgets.get(type);
            if (perMemberBudget == null) {
                log.warn("Per member budget is null for BudgetType: {}. Defaulting to 0.", type);
                perMemberBudget = BigDecimal.ZERO;
            }
            double totalBudgetDouble = 0.0;
            double perMemberBudgetDouble = 0.0;
            try {
                log.info("zzz memberCount: {}", memberCount);
                totalBudgetDouble = perMemberBudget.multiply(BigDecimal.valueOf(memberCount)).doubleValue();
                log.info("zzzz totalBudgetDouble: {}.", totalBudgetDouble);
                perMemberBudgetDouble = perMemberBudget.doubleValue();
            } catch (NullPointerException e) {
                log.error("NullPointerException computing budget double values for type {}: {}", type, e.getMessage());
            }

            log.info("BudgetType: {}, totalBudget (team): {}, perMemberBudget: {}", type, totalBudgetDouble, perMemberBudgetDouble);

            budgetService.getOrCreateBudget(
                    team,
                    type,
                    totalBudgetDouble,
                    perMemberBudgetDouble,
                    year
            );
        }
    }

    private Optional<Training> processTraining(
            Map<String, String> row,
            TeamMember member,
            Team team,
            int year
    ) {
        Optional<Training> existing = trainingRepository.findByCodeSessionAndTeamMember(
                row.get("Code session"), member);

        if (existing.isPresent()) {
            log.info("Training already exists for member: {} and session: {}", member.getName(), row.get("Code session"));
            return Optional.empty();
        }

        Training training = trainingBuilderService.createTrainingFromRow(row, member);

        if (training.getStatus().equalsIgnoreCase("Annulé")) {
            log.info("Skipping cancelled training for member: {}", member.getName());
            return Optional.empty();
        }

        double cost = training.getPriceTND();
        log.info("Processing training for member: {} with cost: {} TND", member.getName(), cost);

        // Update member budget
        member.setTrainingBudgetRemaining(
                member.getTrainingBudgetRemaining().subtract(BigDecimal.valueOf(cost)));

        // Update team budget
        Budget trainingBudget = team.getBudgetByTypeAndYear(TRAINING, year);
        BigDecimal updatedRemaining = BigDecimal.valueOf(trainingBudget.getRemainingBudget())
                .subtract(BigDecimal.valueOf(cost));
        log.info("remaining budget after training: {}", updatedRemaining);
        log.info("Deducting training cost {} from team {}: before={}, after={}",
                cost, team.getName(), trainingBudget.getRemainingBudget(), updatedRemaining);

        budgetService.updateBudget(trainingBudget, updatedRemaining.doubleValue());

        return Optional.of(training);
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