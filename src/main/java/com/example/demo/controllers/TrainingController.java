package com.example.demo.controllers;

import com.example.demo.dtos.BudgetSubmissionDTO;
import com.example.demo.dtos.TrainingImportResult;
import com.example.demo.entities.Users;
import com.example.demo.enums.BudgetType;
import com.example.demo.exceptions.UserNotFoundException;
import com.example.demo.repositories.UsersRepository;
import com.example.demo.services.PendingBudgetService;
import com.example.demo.services.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/training")
@RequiredArgsConstructor
@Tag(name = "Training Import", description = "Endpoints for training data import")
public class TrainingController {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TrainingController.class);
    private final UsersRepository userRepository;
    private final TrainingService trainingService;
    private final PendingBudgetService pendingBudgetService;

    @GetMapping("/")
    public ResponseEntity<String> getTraining(@org.springframework.web.bind.annotation.RequestBody String rawBody) {
        return ResponseEntity.ok(rawBody);
    }
    @PostMapping("/test-budgets")
    public ResponseEntity<String> testEndpoint(@RequestBody String rawBody) {
        return ResponseEntity.ok("Received: " + rawBody);
    }
    @Operation(
            summary = "Upload budgets for different types",
            description = "Upload budgets for FORMATION, VOYAGE, and AUTRE before importing team data",
            requestBody = @RequestBody(
                    description = "Budget values for different types including year",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BudgetSubmissionDTO.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Budgets saved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid budget data"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    @PostMapping("/upload-budgets")
    public ResponseEntity<Map<String, String>> uploadBudgets(
            @Parameter(description = "Budget data including year", required = true)
            @org.springframework.web.bind.annotation.RequestBody BudgetSubmissionDTO budgetSubmission,
            HttpServletRequest request
    ) {
        checkAuthentication();

        // Validate input
        if (Objects.isNull(budgetSubmission) ||
                Objects.isNull(budgetSubmission.getBudgets()) ||
                budgetSubmission.getBudgets().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Au moins un budget doit être fourni");
        }

        // Validate year (example: between 2000-2100)
        if (budgetSubmission.getYear() < 2000 || budgetSubmission.getYear() > 2100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Année doit être entre 2000 et 2100");
        }

        Users user = getUser();
        Map<String, String> response = new HashMap<>();

        // Process each budget type with year
        budgetSubmission.getBudgets().forEach((type, amount) -> {
            if (amount == null || amount < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Budget pour " + type + " ne pas être négatif ou nul");
            }

            boolean existed = pendingBudgetService.getBudget(
                    user.getEmail(),
                    type,
                    budgetSubmission.getYear()
            ).isPresent();

            pendingBudgetService.setOrUpdateBudget(
                    user.getEmail(),
                    BigDecimal.valueOf(amount),
                    type,
                    budgetSubmission.getYear()
            );

            response.put(type.toString(), existed ? "updated" : "set");
        });

        logger.info("Budgets submitted for year {}: {}", budgetSubmission.getYear(), budgetSubmission.getBudgets());
        response.put("message", "Budgets enregistrés avec succès");
        response.put("year", String.valueOf(budgetSubmission.getYear()));
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get pending budgets by year",
            description = "Retrieve all pending budgets for the authenticated director in a specific year"
    )
    @GetMapping("/budgets")
    public ResponseEntity<Map<BudgetType, Double>> getBudgets(
            @Parameter(description = "Year to filter budgets", required = true)
            @RequestParam int year
    ) {
        checkAuthentication();
        Users user = getUser();

        Map<BudgetType, Double> budgets = pendingBudgetService.getBudgetsByDirectorAndYear(
                user.getEmail(),
                year
        );

        return ResponseEntity.ok(budgets);
    }

    @Operation(
            summary = "Import training data from Excel",
            description = "Upload an Excel file after uploading the budget for a specific year",
            requestBody = @RequestBody(
                    description = "Multipart form with Excel file and year",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Training data imported"),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "404", description = "User/Budget not found")
            }
    )
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> importTrainingData(
            @Parameter(description = "Excel file to import training data", required = true)
            @RequestPart("file") MultipartFile file,

            @Parameter(description = "Year for budget validation", required = true)
            @RequestParam("year") int year
    ) throws IOException {
        checkAuthentication();
        checkFile(file);

        // Validate year
        if (year < 2000 || year > 2100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Year must be between 2000 and 2100");
        }

        Users user = getUser();
        Map<String, String> response = new HashMap<>();

        long startTime = System.currentTimeMillis();
        TrainingImportResult result = trainingService.processExcelFile(file, user, year); // Updated service call
        long duration = System.currentTimeMillis() - startTime;

        response.put("year", String.valueOf(year));
        response.put("message", String.format("%d rows imported in %d ms", result.processedRows(), duration));
        response.put("processedRows", String.valueOf(result.processedRows()));
        response.put("durationMs", String.valueOf(duration));

        if (result.budgetWarningMessage() != null) {
            response.put("warning", result.budgetWarningMessage());
        }

        return ResponseEntity.ok(response);
    }

    private Users getUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private void checkAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }
    }

    private void checkFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is missing or empty.");
        }
    }
}
