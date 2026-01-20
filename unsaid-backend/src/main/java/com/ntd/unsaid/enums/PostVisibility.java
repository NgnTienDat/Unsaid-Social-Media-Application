package com.ntd.unsaid.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PostVisibility {
    PUBLIC("PUBLIC"),
    PRIVATE("PRIVATE"),
    FOLLOWERS_ONLY("FOLLOWERS_ONLY");

    private final String value;
}
