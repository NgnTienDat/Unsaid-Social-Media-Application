package com.ntd.unsaid.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AnonymityLevel {
    IDENTIFIED("IDENTIFIED"),
    ANONYMOUS("ANONYMOUS");

    private final String value;
}
