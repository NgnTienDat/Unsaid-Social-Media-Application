package com.ntd.unsaid.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {
    NEW_FOLLOW_REQUEST("NEW_FOLLOW_REQUEST"),
    FOLLOW_ACCEPTED("FOLLOW_ACCEPTED"),
    POST_REACTED("POST_REACTED"),
    POST_REPLIED("POST_REPLIED"),
    POST_REPOSTED("POST_REPOSTED");

    private final String value;
}
