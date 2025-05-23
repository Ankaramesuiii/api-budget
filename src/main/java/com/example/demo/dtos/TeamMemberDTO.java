package com.example.demo.dtos;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class TeamMemberDTO {
    private int id;
    private String name;
    private String email;
    private String post;
    private String phone;
    private String manager;
    private String superManager;
    private String businessUnit;
    private String remainingBudget;
    private String budgetType;
    private String theme;
}
