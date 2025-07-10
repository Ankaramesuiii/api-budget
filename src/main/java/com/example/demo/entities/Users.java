package com.example.demo.entities;

import com.example.demo.enums.Role;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;


@Entity
@Table
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
@Inheritance(strategy = InheritanceType.JOINED)
public class Users implements UserDetails, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private int id;

    private String cuid;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String status;

    private String phone;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getGrantedAuthorities();
    }

    @Override
    public String getUsername() {
        // In this application.properties, email is used as the username for Spring Security
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

}
