package com.ntd.unsaid.infrastructure.messaging.worker;

import com.ntd.unsaid.domain.enums.ActionType;
import com.ntd.unsaid.domain.event.ActionMessage;
import jakarta.annotation.PreDestroy;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.*;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ActionWorker {
    JdbcTemplate jdbcTemplate;
    SimpMessagingTemplate messagingTemplate;

    List<ActionMessage> buffer = new ArrayList<>();
    private static final int BATCH_SIZE = 100;

    public void handle(ActionMessage message) {
        System.out.println("Message received in ActionWorker: " + message);
        synchronized (buffer) {
            buffer.add(message);

            if (buffer.size() >= BATCH_SIZE) {
                flushInternal();
            }
        }
    }

    // And this kitty, it will be used every 1000ms to ensure with low traffic,
    // messages are saved to DB
    @Scheduled(fixedDelay = 1000)
    public void flushByTime() {
        log.warn("Flushing ActionWorker buffer by time :)))))))))");
        flushInternal();
    }


    // Actually, this sh*t will be called before server restart or something!
    @PreDestroy
    public void onShutdown() {
        log.info("Flushing ActionWorker buffer on shutdown");
        flushInternal();
    }

    private void flushInternal() {
        List<ActionMessage> batch;

        synchronized (buffer) {
            if (buffer.isEmpty()){
                log.warn("NO ActionWorker buffer to flush");
                return;
            }
            batch = new ArrayList<>(buffer);
            buffer.clear();
        }

        try {
            handleBatch(batch);
        } catch (Exception e) {
            log.error("Failed to process action batch, size={}", batch.size(), e);
        }
    }

    private void handleBatch(List<ActionMessage> batch) {

        Map<String, ActionMessage> latestMap = new HashMap<>();

        for (ActionMessage msg : batch) {
            String key = msg.userId() + ":" + msg.postId();
            latestMap.merge(
                    key,
                    msg,
                    (oldMsg, newMsg) ->
                            newMsg.createdAt()
                                    .isAfter(oldMsg.createdAt()) ? newMsg : oldMsg
            );
        }

        List<ActionMessage> insertLikes = new ArrayList<>();
        List<ActionMessage> deleteLikes = new ArrayList<>();

        for (ActionMessage msg : latestMap.values()) {
            if (ActionType.LIKE.getValue().equals(msg.actionType())) {
                insertLikes.add(msg);
            } else if (ActionType.UNLIKE.getValue().equals(msg.actionType())) {
                deleteLikes.add(msg);
            }
        }

        batchInsertLikes(insertLikes);
        batchDeleteLikes(deleteLikes);

        log.debug("Processed batch: insert={}, delete={}",
                insertLikes.size(), deleteLikes.size());
    }

    private void batchInsertLikes(List<ActionMessage> likes) {
        if (likes.isEmpty()) return;

        String sql = """
                INSERT INTO actions (id, user_id, post_id, action_type, created_at)
                VALUES (?, ?, ?, 'LIKE', ?)
                ON CONFLICT (user_id, post_id, action_type) DO NOTHING
                """;

        jdbcTemplate.batchUpdate(
                sql,
                likes,
                100,
                (ps, msg) -> {
                    ps.setObject(1, UUID.randomUUID());
//                    ps.setString(2, msg.userId());
//                    ps.setString(3, msg.postId());
                    ps.setObject(2, UUID.fromString(msg.userId()));
                    ps.setObject(3, UUID.fromString(msg.postId()));
                    ps.setTimestamp(4, Timestamp.from(msg.createdAt()));
                }
        );
    }


    private void batchDeleteLikes(List<ActionMessage> unlikes) {
        if (unlikes.isEmpty()) return;

        String sql = """
                DELETE FROM actions
                WHERE user_id = ?
                  AND post_id = ?
                  AND action_type = 'LIKE'
                """;

        jdbcTemplate.batchUpdate(
                sql,
                unlikes,
                100,
                (ps, msg) -> {
                    ps.setString(1, msg.userId());
                    ps.setString(2, msg.postId());
                }
        );
    }

}

