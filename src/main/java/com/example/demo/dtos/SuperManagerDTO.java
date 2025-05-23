package com.example.demo.dtos;

import com.example.demo.entities.Budget;
import com.example.demo.entities.Team;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Setter
@Getter
public class SuperManagerDTO {
    private String name;
    private String email;
    private String businessUnitName;
    private List<String> managers;
    //private List<Team> team;
    //private List<Budget> budget;
}
