package com.ntd.unsaid.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FollowStatus {
    ACCEPTED("ACCEPTED"),
    DENIED("DENIED"),
    PENDING("PENDING"),
    BLOCKED("BLOCKED");

    private final String value;
}
