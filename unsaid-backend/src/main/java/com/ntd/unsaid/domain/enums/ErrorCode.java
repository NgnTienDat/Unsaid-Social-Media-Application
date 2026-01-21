package com.ntd.unsaid.domain.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ErrorCode {
    // App: 1XXX
    UNCATEGORIZED_ERROR(1000, "Uncategorized Error", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_NOT_FOUND(1001, "User not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTED(1002, "User already exists", HttpStatus.CONFLICT),
    UNAUTHENTICATED(1003, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1004, "You dont have permission", HttpStatus.FORBIDDEN),
    ACCOUNT_LOCKED(1006, "Your account is locked", HttpStatus.BAD_REQUEST),
    INVALID_YEAR_FORMAT(1009, "Invalid year", HttpStatus.BAD_REQUEST),
    TOO_MANY_REQUESTS(1012, "Too many requests", HttpStatus.TOO_MANY_REQUESTS),
    USERNAME_ALREADY_EXISTED(1013, "Username already exists", HttpStatus.CONFLICT),
    UPLOAD_FILE_FAILED(1014, "Upload file failed", HttpStatus.INTERNAL_SERVER_ERROR),
    CANNOT_FOLLOW_YOURSELF(1015, "Cannot follow yourself", HttpStatus.BAD_REQUEST),
    ALREADY_FOLLOWING(1016, "You are already following this user", HttpStatus.BAD_REQUEST),
    CANNOT_UNFOLLOW_YOURSELF(1017, "Cannot unfollow yourself", HttpStatus.BAD_REQUEST),
    FOLLOW_NOT_FOUND(1018, "Follow relationship not found", HttpStatus.NOT_FOUND),
    MEDIA_LIMIT_EXCEEDED(1019, "Media limit exceeded, at most 10 files", HttpStatus.BAD_REQUEST),
    POST_NOT_FOUND(1020, "Post not found", HttpStatus.NOT_FOUND),
    VIDEO_DURATION_EXCEEDED(1021, "Video duration exceeded", HttpStatus.BAD_REQUEST),
    MEDIA_UPLOAD_FAILED(1022, "Media upload failed", HttpStatus.INTERNAL_SERVER_ERROR),
    POST_CONTENT_OR_MEDIA_REQUIRED(1023, "Post content or media is required", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE(1024, "File size is too large", HttpStatus.BAD_REQUEST),
    INVALID_FOLLOW_TYPE(1025, "Invalid follow type", HttpStatus.BAD_REQUEST),

    // Validation: 2XXX
    INVALID_MESSAGE_KEY(2001, "Invalid Message Key", HttpStatus.BAD_REQUEST),
    NOT_BLANK(2002, "Cannot blank this field", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(2003, "Invalid email address" , HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(2004, "Invalid password", HttpStatus.BAD_REQUEST),
    INVALID_NAME(2005, "Invalid name account", HttpStatus.BAD_REQUEST),
    ;

    private int code;
    private String message;
    private HttpStatusCode httpStatusCode;
}
