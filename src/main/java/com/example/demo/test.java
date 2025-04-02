package com.example.demo;


import com.github.javafaker.Faker;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class test {
    public static void main(String[] args) {
        String[] headers = {"Date de début", "Date de fin", "Code session", "Nombre de jour", "Thématique",
                "Plan de formation", "Axe", "Mode", "Matricule", "Nom/Prénom", "BU", "Manager", "Directeur",
                "Cabinet", "Prix", "Devise", "Taux de change", "Prix_TND", "Statut", "Présence",
                "Création demande d'achat", "Code DA", "Formateur interne"};

        Faker faker = new Faker();
        Random random = new Random();

        String[] themes = {"Gestion de projet", "Cybersécurité", "Marketing digital", "Finance", "Développement personnel",
                "Leadership", "Blockchain", "Design UX/UI", "Management d'équipe", "Stratégie d'entreprise",
                "Entrepreneuriat", "Programmation avancée", "Développement mobile", "Data Science", "Cloud Computing",
                "Intelligence Artificielle", "Machine Learning", "Sécurité informatique", "Développement durable",
                "Innovation", "Vente et négociation", "Communication", "Méthodes agiles"};

        String[] businessUnits = {"Technologies de l'information", "Marketing numérique", "Développement logiciel",
                "Data Science & Intelligence Artificielle", "Blockchain & Fintech", "Ressources humaines & Talent Management",
                "Services financiers & Comptabilité", "Ventes & Relations clients", "Gestion de projet & Transformation numérique",
                "Innovation & R&D", "Support technique & Infrastructure", "Cyber-sécurité", "Formation & Développement",
                "Management & Stratégie", "Produits & Services", "Communication & Relations publiques",
                "Gestion des risques & Conformité", "Développement durable & RSE", "Business Intelligence & Analytics",
                "Gestion des opérations & Supply Chain"};

        List<String> internationalTrainingCenters = Arrays.asList("Coursera", "Udemy", "LinkedIn Learning");
        List<String> tunisianTrainingCenters = Arrays.asList("Tunisia Digital Academy", "Smart Tunisia");
        String[] modes = {"En ligne", "Présentiel", "Hybride"};
        String[] statusOptions = {"Confirmé", "En attente", "Annulé"};
        String[] presenceOptions = {"Présent", "Absent", "Excusé"};

        // Map to store BU -> Director -> Managers -> Employees hierarchy
        Map<String, Map<String, Map<String, List<String>>>> hierarchy = new HashMap<>();

        // Generate hierarchy
        for (String bu : businessUnits) {
            Map<String, Map<String, List<String>>> directorMap = new HashMap<>();
            String director = faker.name().fullName();
            Map<String, List<String>> managerMap = new HashMap<>();

            // Each director has up to 6 managers
            int numberOfManagers = 1 + random.nextInt(6); // 1 to 6 managers
            for (int i = 0; i < numberOfManagers; i++) {
                String manager = faker.name().fullName();
                List<String> employees = new ArrayList<>();

                // Each manager has 6 to 10 team members
                int numberOfEmployees = 6 + random.nextInt(5); // 6 to 10 employees
                for (int j = 0; j < numberOfEmployees; j++) {
                    employees.add(faker.name().fullName());
                }

                managerMap.put(manager, employees);
            }

            directorMap.put(director, managerMap);
            hierarchy.put(bu, directorMap);
        }

        // Map to store employee -> matricule mapping
        Map<String, String> employeeToMatricule = new HashMap<>();
        int matriculeCounter = 1000; // Start matricule numbering from 1000

        // Liste pour stocker toutes les formations avant de les écrire dans Excel
        List<Object[]> trainingData = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Formation Data");

            // Écrire l'en-tête
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowCount = 1; // Commencer à écrire les données à partir de la ligne 1
            for (String bu : hierarchy.keySet()) {
                Map<String, Map<String, List<String>>> directorMap = hierarchy.get(bu);
                for (String director : directorMap.keySet()) {
                    Map<String, List<String>> managerMap = directorMap.get(director);
                    for (String manager : managerMap.keySet()) {
                        List<String> employees = managerMap.get(manager);
                        for (String employee : employees) {
                            // Generate or reuse matricule for the employee
                            String matricule = employeeToMatricule.get(employee);
                            if (matricule == null) {
                                matricule = "EMP" + (++matriculeCounter);
                                employeeToMatricule.put(employee, matricule);
                            }

                            // Track used dates for this employee to avoid overlaps
                            Set<LocalDate> usedDates = new HashSet<>();

                            // Assign 1 to 3 external themes
                            int numberOfExternalThemes = 1 + random.nextInt(3); // 1 to 3 external themes
                            for (int i = 0; i < numberOfExternalThemes; i++) {
                                String theme = themes[random.nextInt(themes.length)];
                                String trainingCenter = internationalTrainingCenters.get(random.nextInt(internationalTrainingCenters.size()));
                                scheduleTraining(employee, theme, trainingCenter, usedDates, trainingData, rowCount++, bu, manager, director, random, statusOptions, presenceOptions, modes, matricule);
                            }

                            // Assign 2 to 6 internal themes
                            int numberOfInternalThemes = 2 + random.nextInt(5); // 2 to 6 internal themes
                            for (int i = 0; i < numberOfInternalThemes; i++) {
                                String theme = themes[random.nextInt(themes.length)];
                                String trainingCenter = tunisianTrainingCenters.get(random.nextInt(tunisianTrainingCenters.size()));
                                scheduleTraining(employee, theme, trainingCenter, usedDates, trainingData, rowCount++, bu, manager, director, random, statusOptions, presenceOptions, modes, matricule);
                            }
                        }
                    }
                }
            }

            // Trier les formations par date de création de la demande d'achat
            trainingData.sort(Comparator.comparing(o -> LocalDate.parse((String) o[20]))); // Index 20 = Création demande d'achat

            // Réattribuer les codes DA en fonction de l'ordre trié
            for (int i = 0; i < trainingData.size(); i++) {
                trainingData.get(i)[21] = "DA-" + (1001 + i); // Index 21 = Code DA
            }

            // Écrire les données triées dans le fichier Excel
            int dataRowCount = 1; // Commencer à écrire les données à partir de la ligne 1
            for (Object[] data : trainingData) {
                Row row = sheet.createRow(dataRowCount++);
                for (int i = 0; i < data.length; i++) {
                    row.createCell(i).setCellValue(data[i].toString());
                }
            }

            // Écrire le fichier Excel
            try (FileOutputStream fileOut = new FileOutputStream("training_data.xlsx")) {
                workbook.write(fileOut);
            }
            System.out.println("Training data generated successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void scheduleTraining(String employee, String theme, String trainingCenter, Set<LocalDate> usedDates,
                                         List<Object[]> trainingData, int rowCount, String bu, String manager, String director,
                                         Random random, String[] statusOptions, String[] presenceOptions, String[] modes, String matricule) {
        LocalDate trainingStartDate;
        do {
            // Generate a random date within the year 2025
            trainingStartDate = LocalDate.of(2025, 1, 1).plusDays(random.nextInt(365)); // Random date in 2025
        } while (usedDates.contains(trainingStartDate)); // Ensure no overlap

        usedDates.add(trainingStartDate);

        int duration = 3 + random.nextInt(5); // Duration of 3 to 7 days
        String currency = trainingCenter.contains("Tunisia") ? "TND" : (random.nextBoolean() ? "EUR" : "USD");
        double price = 100 + (random.nextDouble() * 300);
        double exchangeRate = currency.equals("TND") ? 1.0 : 2.5 + (random.nextDouble());
        double priceTND = price * exchangeRate;

        // Generate the status and presence based on the rules
        String status = statusOptions[random.nextInt(statusOptions.length)];
        String presence;

        if (status.equals("Annulé")) {
            presence = "Non applicable"; // Or "Annulé"
        } else if (status.equals("En attente")) {
            presence = "En attente de confirmation"; // Or "Non défini"
        } else {
            // If the start date is in the future, set presence to "A définir"
            if (trainingStartDate.isAfter(LocalDate.now())) {
                presence = "A définir";
            } else {
                presence = presenceOptions[random.nextInt(presenceOptions.length)];
            }
        }

        // Generate a random creation date for the purchase request before the training start date
        LocalDate creationDate = trainingStartDate.minusDays(1 + random.nextInt(30)); // Between 1 and 30 days before

        // Add the training data to the list
        trainingData.add(new Object[]{
                trainingStartDate.toString(),
                trainingStartDate.plusDays(duration).toString(),
                "SES-" + (1000 + rowCount),
                duration,
                theme,
                "Formation annuelle",
                "Axe-" + random.nextInt(5),
                modes[random.nextInt(modes.length)],
                matricule, // Use the consistent matricule for the employee
                employee,
                bu,
                manager,
                director,
                trainingCenter,
                price,
                currency,
                exchangeRate,
                priceTND,
                status,
                presence,
                creationDate.toString(),
                "DA-" + (1000 + rowCount), // Temporary DA code
                random.nextBoolean() ? "Oui" : "Non"
        });
    }
}
