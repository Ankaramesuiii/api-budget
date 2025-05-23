package com.example.demo.services;

import com.example.demo.entities.Theme;
import com.example.demo.repositories.ThemeRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
@Service
@AllArgsConstructor
public class ThemeService {
    private final ThemeRepository themeRepository;

    public Theme getOrCreate(String name) {
        return themeRepository.findByName(name)
                .orElseGet(() -> themeRepository.save(new Theme(name)));
    }
}
