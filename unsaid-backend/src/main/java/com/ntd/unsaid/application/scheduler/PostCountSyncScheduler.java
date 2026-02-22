package com.ntd.unsaid.application.scheduler;

import com.ntd.unsaid.utils.RedisKeys;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PostCountSyncScheduler {

    StringRedisTemplate redisTemplate;
    JdbcTemplate jdbcTemplate;

    static final int SYNC_BATCH_SIZE = 500;


    @Scheduled(fixedDelay = 5000)
    public void syncCountsToDatabase() {
        List<String> dirtyPostIds = redisTemplate.opsForSet().pop(RedisKeys.dirtyPosts(), SYNC_BATCH_SIZE);

        if (dirtyPostIds == null || dirtyPostIds.isEmpty()) {
            return;
        }

        List<String> keys = dirtyPostIds.stream()
                .map(RedisKeys::postLikeCount)
                .toList();

        List<String> counts = redisTemplate.opsForValue().multiGet(keys);

        List<Object[]> batchArgs = new ArrayList<>();
        int i = 0;
        for (String postIdStr : dirtyPostIds) {
            String countStr = counts.get(i++);
            if (countStr != null) {
                // Params: {like_count, post_id}
                batchArgs.add(new Object[]{Long.valueOf(countStr), postIdStr});
            } else {
                redisTemplate.opsForSet().add(RedisKeys.dirtyPosts(), postIdStr);
            }
        }

        if (!batchArgs.isEmpty()) {
            batchUpdatePostCounts(batchArgs);
        }
    }

    private void batchUpdatePostCounts(List<Object[]> batchArgs) {
        String sql = "UPDATE posts SET like_count = ? WHERE id = ?";

        try {
            int[] updateResult = jdbcTemplate.batchUpdate(sql, batchArgs);
//            log.info("Synced like_count for {} posts to Database.", updateResult.length);
        } catch (Exception e) {
            log.error("Failed to sync post counts", e);
            // Fail-safe: Re-add IDs to dirty set for retry later. Ensure data consistency.
            reAddIdsToDirtySet(batchArgs);
        }
    }

    private void reAddIdsToDirtySet(List<Object[]> failedBatch) {
        String[] ids = failedBatch.stream()
                .map(args -> String.valueOf(args[1])) // args[1] is postId
                .toArray(String[]::new);
        redisTemplate.opsForSet().add(RedisKeys.dirtyPosts(), ids);
    }
}