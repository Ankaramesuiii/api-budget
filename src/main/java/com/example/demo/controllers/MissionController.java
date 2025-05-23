package com.example.demo.controllers;

import com.example.demo.dtos.MissionRequestDTO;
import com.example.demo.services.MissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;

    @PostMapping
    public ResponseEntity<?> createMissions(@RequestBody MissionRequestDTO request) {
        try {
            missionService.assignSharedMission(request);
            return ResponseEntity.ok(
                    Map.of("message", "Missions created and budgets updated successfully.")
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

}

