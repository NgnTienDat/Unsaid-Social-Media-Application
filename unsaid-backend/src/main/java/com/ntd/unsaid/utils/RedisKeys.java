package com.ntd.unsaid.utils;

public final class RedisKeys {

    public static final String FEED_POST_VERSION = "v1";
    public static final String USER_FEED_VERSION = "v1";

    public static String feedPost(String postId) {
        return "post:data:" + FEED_POST_VERSION + ":" + postId;
    }

    public static String userFeed(String userId) {
        return "feed:user:" + USER_FEED_VERSION + ":" + userId;
    }

    public static String authorTimeline(String authorId) {
        return "posts:user:v1:" + authorId;
    }
}
