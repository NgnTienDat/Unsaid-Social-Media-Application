package com.ntd.unsaid.domain.repository;

import com.ntd.unsaid.domain.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    @Query(value = """
        SELECT id
        FROM users
    """, nativeQuery = true)
    List<String> findAllIds();

    @Query("SELECT u FROM User u WHERE u.followerCount >= :threshold")
    List<User> findTopCelebs(@Param("threshold") long threshold, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.followerCount < :threshold")
    List<User> findTopNormalUsers(@Param("threshold") long threshold, Pageable pageable);
}