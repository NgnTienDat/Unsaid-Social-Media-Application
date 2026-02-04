package com.ntd.unsaid.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ActionType {
    LIKE("LIKE"),
    UNLIKE("UNLIKE"),
    SAVED("SAVED");

    private final String value;
}
