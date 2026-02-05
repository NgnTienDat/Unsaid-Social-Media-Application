package com.ntd.unsaid.utils;

public final class RedisKeys {

    public static final String FEED_POST_VERSION = "v1";
    public static final String USER_FEED_VERSION = "v1";

    public static String postData(String postId) {
        return "post:data:" + FEED_POST_VERSION + ":" + postId;
    }

    public static String userFeed(String userId) {
        return "feed:user:" + USER_FEED_VERSION + ":" + userId;
    }

    public static String userFeedComputed(String userId) {
        return "feed:computed:" + USER_FEED_VERSION + ":" + userId;
    }

    public static String userPostTimeline(String authorId) {
        return "posts:user:" + authorId;
    }

    public static String postLikeCount(String postId) {
        return "posts:like_count:" + postId;
    }
    public static String dirtyPosts() {
        return "sys:dirty_posts";
    }
    public static String userLiked(String postId) {
        return "posts:liked_users:" + postId;
    }
}
