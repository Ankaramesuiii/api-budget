package com.example.demo.services;

import com.example.demo.exceptions.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExcelService {

    public List<Map<String, String>> readExcelFile(MultipartFile file) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                Map<String, String> rowData = new HashMap<>();

                for (int j = 0; j < headerRow.getLastCellNum(); j++) {
                    String header = headerRow.getCell(j).getStringCellValue();
                    Cell cell = row.getCell(j);
                    rowData.put(header, cell != null ? cell.toString() : "");
                }

                rows.add(rowData);
            }
        }

        return rows;
    }

    /**
     * Validates that all required headers are present in the provided Excel rows.
     *
     * @param rows            List of rows, each represented as a Map of column name to value.
     * @param requiredHeaders Set of expected/required column headers.
     * @throws InvalidInputException if any required headers are missing.
     */
    public static void validate(List<Map<String, String>> rows, Set<String> requiredHeaders) {
        if (rows == null || rows.isEmpty()) {
            throw new InvalidInputException("Le fichier est vide ou invalide !");
        }
        log.info(requiredHeaders.toString());
        Set<String> actualHeaders = rows.get(0).keySet();
        log.info(actualHeaders.toString());
        Set<String> missingHeaders = requiredHeaders.stream()
                .filter(required -> !actualHeaders.contains(required))
                .collect(Collectors.toSet());

        if (!missingHeaders.isEmpty()) {
            throw new InvalidInputException("Colonnes manquantes dans le fichier : " +
                    String.join(", ", missingHeaders));
        }
    }
}
