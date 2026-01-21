package com.ntd.unsaid.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ActionType {
    LIKED("LIKED"),
    SAVED("SAVED");

    private final String value;
}
