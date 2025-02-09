package com.example.demo.enums;

public enum Permission {

    SUPER_MANAGER_READ("super_manager:read"),
    SUPER_MANAGER_WRITE("super_manager:post"),
    SUPER_MANAGER_DELETE("super_manager:delete"),
    SUPER_MANAGER_UPDATE("super_manager:update"),

    MANAGER_READ("manager:read"),
    MANAGER_WRITE("manager:post"),
    MANAGER_DELETE("manager:delete"),
    MANAGER_UPDATE("manager:update"),

    TEAM_MEMBER_READ("team_member:read"),
    TEAM_MEMBER_WRITE("team_member:post"),
    TEAM_MEMBER_DELETE("team_member:delete"),
    TEAM_MEMBER_UPDATE("team_member:update");

    private final String permission;

    Permission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}
