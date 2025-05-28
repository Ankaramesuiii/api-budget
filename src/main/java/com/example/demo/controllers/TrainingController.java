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
                    description = "Budget values for different types",
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
            @Parameter(description = "Budget data for different types", required = true)
            @org.springframework.web.bind.annotation.RequestBody BudgetSubmissionDTO budgets
    ) {
        checkAuthentication();

        // Validate input

        if (Objects.isNull(budgets) || Objects.isNull(budgets.getBudgets()) || budgets.getBudgets().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one budget type must be provided");
        }

        Users user = getUser();
        Map<String, String> response = new HashMap<>();

        // Process each budget type
        budgets.getBudgets().forEach((type, amount) -> {
            if (amount == null || amount < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Budget for " + type + " must be greater than 0");
            }

            boolean existed = pendingBudgetService.getBudget(user.getEmail(), type).isPresent();
            pendingBudgetService.setOrUpdateBudget(user.getEmail(), BigDecimal.valueOf(amount), type);

            response.put(type.toString(), existed ? "updated" : "set");
        });

        logger.info("Budgets: {}", budgets);
        response.put("message", "sui");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/budgets")
    public ResponseEntity<Map<BudgetType, Double>> getBudgets() {
        checkAuthentication();
        Users user = getUser();
        Map<BudgetType, Double> budgets = pendingBudgetService.getBudgetsByDirector(user.getEmail());
        return ResponseEntity.ok(budgets);
    }

    @Operation(
            summary = "Import training data from Excel",
            description = "Upload an Excel file after uploading the budget",
            requestBody = @RequestBody(
                    description = "Multipart form with Excel file",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Training data imported"),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> importTrainingData(
            @Parameter(description = "Excel file to import training data", required = true)
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        checkAuthentication();
        checkFile(file);

        Users user = getUser();
        Map<String, String> response = new HashMap<>();

        long startTime = System.currentTimeMillis();
        TrainingImportResult result = trainingService.processExcelFile(file, user);
        long duration = System.currentTimeMillis() - startTime;

        response.put("message", String.format(" %d lignes importées en %d ms.", result.processedRows(), duration));

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
