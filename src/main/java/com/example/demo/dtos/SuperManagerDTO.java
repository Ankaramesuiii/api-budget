package com.example.demo.dtos;

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
