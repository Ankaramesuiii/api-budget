package com.example.demo.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "tests")
public class test {

    @Id
    @GeneratedValue
    Long id;

    @Column(name = "name")
    String name;

    public test(String n){
        name = n;
    }

    public test() {
    }

    public test(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
