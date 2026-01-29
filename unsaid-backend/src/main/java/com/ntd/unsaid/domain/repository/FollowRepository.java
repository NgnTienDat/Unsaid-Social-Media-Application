package com.ntd.unsaid.domain.repository;

import com.ntd.unsaid.application.dto.response.FollowerResponse;
import com.ntd.unsaid.domain.entity.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface FollowRepository extends JpaRepository<Follow, String> {
    Optional<Follow> findByFollowerIdAndFollowingId(String followerId, String followingId);

    @Query("""
                select new com.ntd.unsaid.application.dto.response.FollowerResponse(
                    u.id,
                    u.username,
                    u.fullName,
                    u.avatar
                )
                from Follow f
                join f.follower u
                where f.following.id = :followingId
            """)
    Page<FollowerResponse> findFollowersDto(
            @Param("followingId") String followingId,
            Pageable pageable
    );

    @Query("""
                select f.follower.id
                from Follow f
                where f.following.id = :followingId
            """)
    List<String> findFollowerIdsByFollowingId(@Param("followingId") String followingId);

    @Query("""
                select new com.ntd.unsaid.application.dto.response.FollowerResponse(
                    u.id,
                    u.username,
                    u.fullName,
                    u.avatar
                )
                from Follow f
                join f.following u
                where f.follower.id = :followerId
            """)
    Page<FollowerResponse> findFollowingDto(
            @Param("followerId") String followerId,
            Pageable pageable
    );


    @Query(value = """
                SELECT following_id
                FROM follows
                WHERE follower_id = :followerId
                AND following_id = ANY(:ids)
            """, nativeQuery = true)
    Set<String> findFollowingIds(@Param("followerId") String followerId, @Param("ids") String[] ids);

    @Query(value = """
                SELECT following_id
                FROM follows
                WHERE follower_id = :followerId
            """, nativeQuery = true)
    Set<String> findFollowingIds(@Param("followerId") String followerId);

    @Query(value = """
                SELECT f.following_id 
                FROM follows f
                JOIN users u ON f.following_id = u.id
                WHERE f.follower_id = :followerId 
                  AND u.follower_count > 3000
            """, nativeQuery = true)
    List<String> findFollowedCelebrityIds(@Param("followerId") String followerId);

    long countFollowerIdsByFollowingId(String followingId);
}