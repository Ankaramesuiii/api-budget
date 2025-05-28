package com.example.demo.configs;

import com.example.demo.entities.BusinessUnit;
import com.example.demo.entities.SuperManager;
import com.example.demo.enums.Role;
import com.example.demo.repositories.BusinessUnitRepository;
import com.example.demo.repositories.SuperManagerRepository;
import com.example.demo.services.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StartupDataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(StartupDataInitializer.class);

    private final BusinessUnitRepository businessUnitRepository;
    private final SuperManagerRepository superManagerRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @Value("${app.seed:false}")
    private boolean seedEnabled;
    
    @Value("${app.default.password:password}")
    private String defaultPassword;

    @Transactional
    @Override
    public void run(String... args) {
        if (!seedEnabled) {
            logger.info("🔁 Seeding disabled.");
            return;
        }

        if (superManagerRepository.count() > 0 || businessUnitRepository.count() > 0) {
            logger.info("✅ SuperManagers already exist, skipping seeding.");
            return;
        }

        // Map of BU name to SuperManager full name
        Map<String, String> buToSm = new LinkedHashMap<>();
        buToSm.put("Business Intelligence & Analytics", "Ozzie Bayer");
        buToSm.put("Blockchain & Fintech", "Ms. Rich Treutel");
        buToSm.put("Communication & Relations publiques", "Boris Leuschke");
        buToSm.put("Cyber-sécurité", "Ahmed Bernhard");
        buToSm.put("Développement durable & RSE", "Keila Jakubowski MD");
        buToSm.put("Développement logiciel", "Chet Halvorson");
        buToSm.put("Data Science & Intelligence Artificielle", "Lesa Stehr");
        buToSm.put("Services financiers & Comptabilité", "Brady Wunsch I");
        buToSm.put("Formation & Développement", "Desmond Gottlieb");
        buToSm.put("Gestion de projet & Transformation numérique", "Treasa Cummings");
        buToSm.put("Innovation & R&D", "Dr. Talia Turner");
        buToSm.put("Management & Stratégie", "Coy Miller");
        buToSm.put("Marketing numérique", "Fermin Ebert");
        buToSm.put("Gestion des opérations & Supply Chain", "Lan Collier");
        buToSm.put("Produits & Services", "Dr. Pamula Franecki");
        buToSm.put("Ressources humaines & Talent Management", "Dario Larson");
        buToSm.put("Gestion des risques & Conformité", "Lisabeth Moen");
        buToSm.put("Support technique & Infrastructure", "Ms. Rina Donnelly");
        buToSm.put("Technologies de l'information", "Florentino Becker");
        buToSm.put("Ventes & Relations clients", "Demetrice O'Conner");

        buToSm.forEach((buName, fullName) -> {
            // Create and save Business Unit
            BusinessUnit bu = new BusinessUnit(buName);
            businessUnitRepository.save(bu);

            // Create SuperManager
            SuperManager sm = new SuperManager();
            sm.setName(fullName);
            sm.setEmail(UserService.generateEmail(fullName));
            sm.setPassword(passwordEncoder.encode(defaultPassword));
            sm.setRole(Role.SUPER_MANAGER);
            sm.setStatus("Active");
            sm.setPhone(userService.getPhoneNumber());
            sm.setCuid("SM-" + UUID.randomUUID().toString().substring(0, 4));
            sm.setBusinessUnit(bu);

            // Save
            superManagerRepository.save(sm);
            bu.setSuperManager(sm); // optional: keep relation in sync
        });

        logger.info("✅ Seeded BusinessUnits and SuperManagers.");
    }
}
