package com.example.demo.services;

import com.example.demo.entities.Training;
import com.example.demo.entities.Users;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainingRetrievalService {

    private final List<TrainingFetcherService> fetchers;

    public TrainingRetrievalService(List<TrainingFetcherService> fetchers) {
        this.fetchers = fetchers;
    }

    public List<Training> getTrainingsForUser(Users user) {
        return fetchers.stream()
                .filter(fetcher -> fetcher.supports(user.getRole()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unsupported role: " + user.getRole()))
                .fetchTrainings(user);
    }
}

