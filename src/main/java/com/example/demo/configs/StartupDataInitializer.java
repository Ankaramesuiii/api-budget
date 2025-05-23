package com.example.demo.config;

import com.example.demo.entities.BusinessUnit;
import com.example.demo.entities.SuperManager;
import com.example.demo.enums.Role;
import com.example.demo.repositories.BusinessUnitRepository;
import com.example.demo.repositories.SuperManagerRepository;
import com.example.demo.services.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StartupDataInitializer implements CommandLineRunner {

    private final BusinessUnitRepository businessUnitRepository;
    private final SuperManagerRepository superManagerRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @Value("${app.seed:false}")
    private boolean seedEnabled;

    @Transactional
    @Override
    public void run(String... args) {
        if (!seedEnabled) {
            System.out.println("🔁 Seeding disabled.");
            return;
        }

        if (superManagerRepository.count() > 0 || businessUnitRepository.count() > 0) {
            System.out.println("✅ SuperManagers already exist, skipping seeding.");
            return;
        }

        // Map of BU name to SuperManager full name
        Map<String, String> buToSm = new LinkedHashMap<>() {{
            put("Business Intelligence & Analytics", "Ozzie Bayer");
            put("Blockchain & Fintech", "Ms. Rich Treutel");
            put("Communication & Relations publiques", "Boris Leuschke");
            put("Cyber-sécurité", "Ahmed Bernhard");
            put("Développement durable & RSE", "Keila Jakubowski MD");
            put("Développement logiciel", "Chet Halvorson");
            put("Data Science & Intelligence Artificielle", "Lesa Stehr");
            put("Services financiers & Comptabilité", "Brady Wunsch I");
            put("Formation & Développement", "Desmond Gottlieb");
            put("Gestion de projet & Transformation numérique", "Treasa Cummings");
            put("Innovation & R&D", "Dr. Talia Turner");
            put("Management & Stratégie", "Coy Miller");
            put("Marketing numérique", "Fermin Ebert");
            put("Gestion des opérations & Supply Chain", "Lan Collier");
            put("Produits & Services", "Dr. Pamula Franecki");
            put("Ressources humaines & Talent Management", "Dario Larson");
            put("Gestion des risques & Conformité", "Lisabeth Moen");
            put("Support technique & Infrastructure", "Ms. Rina Donnelly");
            put("Technologies de l'information", "Florentino Becker");
            put("Ventes & Relations clients", "Demetrice O'Conner");
        }};

        Random random = new Random();

        buToSm.forEach((buName, fullName) -> {
            // Create and save Business Unit
            BusinessUnit bu = new BusinessUnit(buName);
            businessUnitRepository.save(bu);

            // Create SuperManager
            SuperManager sm = new SuperManager();
            sm.setName(fullName);
            sm.setEmail(UserService.generateEmail(fullName));
            sm.setPassword(passwordEncoder.encode("password"));
            sm.setRole(Role.SUPER_MANAGER);
            sm.setStatus("Active");
            sm.setPhone(userService.getPhoneNumber());
            sm.setCuid("SM-" + UUID.randomUUID().toString().substring(0, 4));
            sm.setBusinessUnit(bu);

            // Save
            superManagerRepository.save(sm);
            bu.setSuperManager(sm); // optional: keep relation in sync
        });

        System.out.println("✅ Seeded BusinessUnits and SuperManagers.");
    }
}
