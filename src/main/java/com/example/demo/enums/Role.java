package com.example.demo.enums;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.demo.enums.Permission.*;

public enum Role {
    SUPER_MANAGER(
            Set.of(
                    SUPER_MANAGER_READ,
                    SUPER_MANAGER_WRITE,
                    SUPER_MANAGER_DELETE,
                    SUPER_MANAGER_UPDATE,
                    MANAGER_READ,
                    MANAGER_WRITE,
                    MANAGER_DELETE,
                    MANAGER_UPDATE,
                    TEAM_MEMBER_READ,
                    TEAM_MEMBER_WRITE,
                    TEAM_MEMBER_DELETE,
                    TEAM_MEMBER_UPDATE
            )
    ),
    MANAGER(
            Set.of(
                    MANAGER_READ,
                    MANAGER_WRITE,
                    MANAGER_DELETE,
                    MANAGER_UPDATE,
                    TEAM_MEMBER_READ,
                    TEAM_MEMBER_WRITE,
                    TEAM_MEMBER_DELETE,
                    TEAM_MEMBER_UPDATE
            )
    ),
    TEAM_MEMBER(
            Set.of(
                    TEAM_MEMBER_READ,
                    TEAM_MEMBER_WRITE,
                    TEAM_MEMBER_DELETE,
                    TEAM_MEMBER_UPDATE
            )
    );


    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public List<SimpleGrantedAuthority> getGrantedAuthorities() {
        List<SimpleGrantedAuthority> authorities = permissions.stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return authorities;
    }
}
