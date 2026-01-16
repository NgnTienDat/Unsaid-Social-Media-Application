package com.ntd.unsaid.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ActionType {
    LIKED("LIKED"),
    SAVED("SAVED");

    private final String value;
}
