package com.ssafy.ssamentle.entity.enums;

public enum UserRole {

    ADMIN("관리자"),
    USER("사용자");

    private final String userRole;

    UserRole(String userRole) {
        this.userRole = userRole;
    }
}
