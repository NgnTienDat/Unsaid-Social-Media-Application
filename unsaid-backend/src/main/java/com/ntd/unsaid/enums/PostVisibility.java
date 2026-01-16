package com.ntd.unsaid.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PostVisibility {
    PUBLIC("PUBLIC"),
    FOLLOWERS_ONLY("FOLLOWERS_ONLY");

    private final String value;
}
