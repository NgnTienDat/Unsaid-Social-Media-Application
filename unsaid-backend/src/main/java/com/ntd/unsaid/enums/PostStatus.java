package com.ntd.unsaid.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PostStatus {
    ACTIVE("ACTIVE"),
    HIDDEN("HIDDEN"),
    DELETED("DELETED");

    private final String value;
}
