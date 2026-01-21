package com.ntd.unsaid.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRole {
    SYSADMIN("SYSADMIN"),
    USER("USER");

    private final String value;
}
