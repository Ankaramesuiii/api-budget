package com.example.demo.controllers;

import com.example.demo.exceptions.FileMissingException;
import com.example.demo.exceptions.UnauthorizedException;
import com.example.demo.services.TrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/training")
public class TrainingController {

    @Autowired
    private TrainingService trainingService;

    // Thread pool for async processing
    private final ExecutorService executor = Executors.newCachedThreadPool();

    // Existing synchronous endpoint (kept for compatibility)
    @PostMapping("/import")
    public ResponseEntity<Map<String, String>> importTrainingData(@RequestParam("file") MultipartFile file) {
        checkAuthentication();
        checkFile(file);

        Map<String, String> response = new HashMap<>();
        try {
            long startTime = System.currentTimeMillis();
            int processedRows = trainingService.processExcelFile(file);
            long duration = System.currentTimeMillis() - startTime;

            response.put("message", String.format("Processed %d rows in %d ms", processedRows, duration));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Error processing file: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }


    // Helper methods
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

    @PostMapping(value = "/import/async", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter importTrainingDataAsync(@RequestParam("file") MultipartFile file) {
        checkAuthentication();
        checkFile(file);

        SseEmitter emitter = new SseEmitter(3600000L); // 1 hour timeout
        executor.execute(() -> {
            try {
                long startTime = System.currentTimeMillis();
                int processed = trainingService.processExcelFile(file);
                long duration = System.currentTimeMillis() - startTime;

                emitter.send(SseEmitter.event()
                        .name("complete")
                        .data("Processed " + processed + " rows in " + duration + "ms"));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }
}