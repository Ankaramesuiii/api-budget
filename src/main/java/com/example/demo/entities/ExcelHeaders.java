package com.example.demo.entities;

import java.util.Set;

public class ExcelHeaders {
    public static final String DATE_DEBUT = "Date de début";
    public static final String DATE_FIN = "Date de fin";
    public static final String CODE_SESSION = "Code session";
    public static final String NB_JOUR = "Nombre de jour";
    public static final String THEMATIQUE = "Thématique";
    public static final String PLAN_FORMATION = "Plan de formation";
    public static final String AXE = "Axe";
    public static final String MODE = "Mode";
    public static final String MATRICULE = "Matricule";
    public static final String NOM_PRENOM = "Nom/Prénom";
    public static final String BU = "BU";
    public static final String MANAGER = "Manager";
    public static final String DIRECTEUR = "Directeur";
    public static final String CABINET = "Cabinet";
    public static final String PRIX = "Prix";
    public static final String DEVISE = "Devise";
    public static final String TAUX_CHANGE = "Taux de change";

    public static final Set<String> REQUIRED_HEADERS = Set.of(
            DATE_DEBUT,
            DATE_FIN,
            CODE_SESSION,
            NB_JOUR,
            THEMATIQUE,
            PLAN_FORMATION,
            AXE,
            MODE,
            MATRICULE,
            NOM_PRENOM,
            BU,
            MANAGER,
            DIRECTEUR,
            CABINET,
            PRIX,
            DEVISE,
            TAUX_CHANGE
    );

    private ExcelHeaders() {
        // Prevent instantiation
    }
}
