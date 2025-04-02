package com.example.demo.services;

import com.example.demo.entities.Theme;
import com.example.demo.repositories.ThemeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
@Service
public class ThemeService {
    @Autowired private ThemeRepository themeRepository;

    public Theme getOrCreate(String name) {
        return themeRepository.findByName(name)
                .orElseGet(() -> themeRepository.save(new Theme(name)));
    }
}
