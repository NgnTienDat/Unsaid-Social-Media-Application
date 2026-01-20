package com.ntd.unsaid.repository;

import com.ntd.unsaid.dto.response.FollowerResponse;
import com.ntd.unsaid.entity.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, String> {
    Optional<Follow> findByFollowerIdAndFollowingId(String followerId, String followingId);

    @Query("""
                select new com.ntd.unsaid.dto.response.FollowerResponse(
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
                select new com.ntd.unsaid.dto.response.FollowerResponse(
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

}