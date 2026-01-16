package com.ntd.unsaid.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MediaType {
    IMAGE("IMAGE"),
    VIDEO("VIDEO");

    private final String value;
}
