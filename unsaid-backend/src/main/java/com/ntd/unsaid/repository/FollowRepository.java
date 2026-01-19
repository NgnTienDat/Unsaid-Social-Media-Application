package com.ntd.unsaid.repository;

import com.ntd.unsaid.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, String> {
    Optional<Follow> findByFollowerIdAndFollowingId(String followerId, String followingId);
}