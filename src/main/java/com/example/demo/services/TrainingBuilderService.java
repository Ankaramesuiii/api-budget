package com.example.demo.services;

import com.example.demo.entities.TeamMember;
import com.example.demo.entities.Theme;
import com.example.demo.entities.Training;
import com.example.demo.exceptions.InvalidTrainingDataException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@AllArgsConstructor
public class TrainingBuilderService {

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.FRENCH)
    );
    private final ThemeService themeService;
    public Training createTrainingFromRow(Map<String, String> row, TeamMember member) {
        try {
            Theme theme = resolveTheme(row.get("Thématique"));

            Training training = new Training();
            training.setStartDate(parseDate(row.get("Date de début")));
            training.setEndDate(parseDate(row.get("Date de fin")));
            training.setCodeSession(getRequiredValue(row, "Code session"));
            training.setDuration(parseInt(row.get("Nombre de jour")));
            training.setMode(getRequiredValue(row, "Mode"));
            training.setStatus(getRequiredValue(row, "Statut"));
            training.setPresence(getRequiredValue(row, "Présence"));
            training.setCreationDate(parseDate(row.get("Création demande d'achat")));
            training.setCodeDA(row.get("Code DA"));
            training.setInternalTrainer(isInternalTrainer(row.get("Formateur interne")));
            training.setTeamMember(member);
            training.setTheme(theme);
            training.setPriceTND(parseDouble(row.get("Prix_TND")));
            training.setCurrency(getRequiredValue(row, "Devise"));
            training.setExchangeRate(parseDouble(row.get("Taux de change")));
            training.setPrice(parseDouble(row.get("Prix")));

            return training;
        } catch (Exception e) {
            throw new InvalidTrainingDataException("Failed to create training from row data: " + e.getMessage());
        }
    }

    private Theme resolveTheme(String themeName) {
        return themeService.getOrCreate(themeName);
    }

    public static LocalDate parseDate(String dateString) {
        // Clean up localized month names like "janv." to "janv"
        if (dateString != null) {
            dateString = dateString.replaceAll("(\\p{L}+)[.]", "$1");
        }

        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(dateString, formatter);
            } catch (DateTimeParseException ignored) {
                // Try next format
            }
        }

        throw new InvalidTrainingDataException("Invalid date format: " + dateString);
    }

    private int parseInt(String number) {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            throw new InvalidTrainingDataException("Invalid number format: " + number);
        }
    }

    private double parseDouble(String number) {
        try {
            return Double.parseDouble(number);
        } catch (NumberFormatException e) {
            throw new InvalidTrainingDataException("Invalid decimal number format: " + number);
        }
    }

    private boolean isInternalTrainer(String value) {
        return "Oui".equalsIgnoreCase(value);
    }

    private String getRequiredValue(Map<String, String> row, String key) {
        String value = row.get(key);
        if (value == null || value.isBlank()) {
            throw new InvalidTrainingDataException("Missing required field: " + key);
        }
        return value;
    }
}