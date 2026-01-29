package com.ntd.unsaid.infrastructure.caching;

import com.ntd.unsaid.application.dto.FeedPostDTO;
import com.ntd.unsaid.application.mapper.PostMapper;
import com.ntd.unsaid.domain.entity.Post;
import com.ntd.unsaid.domain.entity.User;
import com.ntd.unsaid.domain.repository.FollowRepository;
import com.ntd.unsaid.domain.repository.PostRepository;
import com.ntd.unsaid.domain.repository.UserRepository;
import com.ntd.unsaid.utils.RedisKeys;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisDataSeeder {
    StringRedisTemplate redisTemplate;
    RedisTemplate<String, FeedPostDTO> feedPostRedisTemplate;
    PostRepository postRepository;
    FollowRepository followRepository;
    UserRepository userRepository;
    RedisRepository redisRepository;
    PostMapper postMapper;


    public void seedRedisForStressTest() {
        log.info("Bắt đầu nạp dữ liệu mục tiêu cho 5000 users...");

        // 1. Lấy danh sách 100 Celebs và 4900 Normal Users
        List<User> celebs = userRepository.findTopCelebs(3000, PageRequest.of(0, 41));
        List<User> normalUsers = userRepository.findTopNormalUsers(3000, PageRequest.of(0, 2000));

        List<User> targetUsers = new ArrayList<>();
        targetUsers.addAll(celebs);
        targetUsers.addAll(normalUsers);

        // --- [MỚI] TẠO SET ĐỂ TRA CỨU NHANH ---
        // Dùng HashSet để kiểm tra user có nằm trong danh sách test hay không cực nhanh
        Set<String> targetUserIdSet = targetUsers.stream()
                .map(User::getId)
                .collect(Collectors.toSet());
        // -------------------------------------

        String csvFileName = "target_users.csv";

        try (PrintWriter writer = new PrintWriter(new FileWriter(csvFileName))) {
            for (User user : targetUsers) {
                writer.printf("%s%n", user.getId());
            }
            System.out.println("Đã xuất file CSV thành công: " + csvFileName);
        } catch (IOException e) {
            System.err.println("Lỗi khi ghi file: " + e.getMessage());
        }

        // Chia 5000 user thành các đợt nhỏ (batch 100) để tránh timeout
        int batchSize = 100;
        var serializer = redisTemplate.getStringSerializer();
        var dtoSerializer = (RedisSerializer<FeedPostDTO>) feedPostRedisTemplate.getValueSerializer();

        for (int i = 0; i < targetUsers.size(); i += batchSize) {
            int end = Math.min(i + batchSize, targetUsers.size());
            List<User> batch = targetUsers.subList(i, end);

            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                for (User user : batch) {
                    String userId = user.getId();

                    // A. Nạp post timeline
                    List<Post> posts = postRepository.findByAuthorId(userId, PageRequest.of(0, 10));
                    if (!posts.isEmpty()) {

                        for (Post p : posts) {
                            String postKey = RedisKeys.feedPost(p.getId());
                            FeedPostDTO dto = postMapper.toFeedPostDTO(p);

                            byte[] serializedKey = serializer.serialize(postKey);
                            byte[] serializedValue = dtoSerializer.serialize(dto);

                            // Nạp DTO vào Redis với TTL 6 tiếng
                            connection.setEx(serializedKey, Duration.ofHours(6).getSeconds(), serializedValue);
                        }

                        byte[] outboxKey = serializer.serialize("posts:user:" + userId);
                        for (Post p : posts) {
                            connection.zAdd(outboxKey, p.getCreatedAt().toEpochMilli(), serializer.serialize(p.getId()));
                        }
                    }

                    // B. Nạp Following Celebs (Giữ nguyên)
                    List<String> followedCelebs = followRepository.findFollowedCelebrityIds(userId);
                    if (!followedCelebs.isEmpty()) {
                        byte[] celebSetKey = serializer.serialize("user:following_celebs:" + userId);
                        for (String cId : followedCelebs) {
                            connection.sAdd(celebSetKey, serializer.serialize(cId));
                        }
                    }

                    // C. [ĐÃ SỬA] Fan-out có chọn lọc
                    if (user.getFollowerCount() < 3000) {
                        // Lấy tất cả follower từ DB
                        List<String> allFollowerIds = followRepository.findFollowerIdsByFollowingId(userId);

                        for (String fId : allFollowerIds) {
                            // --- [MỚI] CHECK LỌC TẠI ĐÂY ---
                            // Chỉ nạp vào Redis Inbox nếu người follower này CŨNG nằm trong danh sách test
                            if (targetUserIdSet.contains(fId)) {
                                byte[] inboxKey = serializer.serialize("feed:user:" + fId);
                                for (Post p : posts) {
                                    connection.zAdd(inboxKey, p.getCreatedAt().toEpochMilli(), serializer.serialize(p.getId()));
                                }
                            }
                        }
                    }
                }
                return null;
            });
            log.info("Đã nạp xong: {}/{} users", end, targetUsers.size());
        }
    }
}
