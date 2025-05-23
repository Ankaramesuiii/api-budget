package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Setter
@Getter
@Entity
@Table(name = "training")
public class Training {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "training_id")
    private int id;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private String codeSession;

    @Column(nullable = false)
    private int duration;

    @Column(nullable = false)
    private String mode;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String presence;

    @Column(nullable = false)
    private LocalDate creationDate;

    @Column(nullable = false)
    private String codeDA;

    @Column(nullable = false)
    private boolean internalTrainer;

    @Column(nullable = false)
    private double price; // Price of the training

    @Column(nullable = false)
    private String currency; // Currency (e.g., EUR, USD, TND)

    @Column(nullable = false)
    private double exchangeRate; // Exchange rate to TND

    @Column(nullable = false)
    private double priceTND; // Price in TND (price * exchangeRate)

    @ManyToOne
    @JoinColumn(name = "team_member_id", nullable = false)
    private TeamMember teamMember; // Each Training is assigned to one TeamMember

    @ManyToOne
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme; // Each Training is associated with one Theme

    // Constructors
    public Training() { }

    public Training(LocalDate startDate, LocalDate endDate, String codeSession, int duration, String mode,
                    String status, String presence, LocalDate creationDate, String codeDA, boolean internalTrainer,
                    double price, String currency, double exchangeRate, double priceTND,
                    TeamMember teamMember, Theme theme) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.codeSession = codeSession;
        this.duration = duration;
        this.mode = mode;
        this.status = status;
        this.presence = presence;
        this.creationDate = creationDate;
        this.codeDA = codeDA;
        this.internalTrainer = internalTrainer;
        this.price = price;
        this.currency = currency;
        this.exchangeRate = exchangeRate;
        this.priceTND = priceTND;
        this.teamMember = teamMember;
        this.theme = theme;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getCodeSession() {
        return codeSession;
    }

    public void setCodeSession(String codeSession) {
        this.codeSession = codeSession;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPresence() {
        return presence;
    }

    public void setPresence(String presence) {
        this.presence = presence;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public String getCodeDA() {
        return codeDA;
    }

    public void setCodeDA(String codeDA) {
        this.codeDA = codeDA;
    }

    public boolean isInternalTrainer() {
        return internalTrainer;
    }

    public void setInternalTrainer(boolean internalTrainer) {
        this.internalTrainer = internalTrainer;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public double getPriceTND() {
        return priceTND;
    }

    public void setPriceTND(double priceTND) {
        this.priceTND = priceTND;
    }

    public TeamMember getTeamMember() {
        return teamMember;
    }

    public void setTeamMember(TeamMember teamMember) {
        this.teamMember = teamMember;
    }

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }
}