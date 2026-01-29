package com.ntd.unsaid.domain.repository;

import com.ntd.unsaid.domain.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface PostRepository extends JpaRepository<Post, String> {
    @Query("""
                select p
                from Post p
                where p.status = 'ACTIVE'
                  and (
                       p.author.id in (
                           select f.following.id
                           from Follow f
                           where f.follower.id = :userId
                       )
                       or p.postVisibility = com.ntd.unsaid.domain.enums.PostVisibility.PUBLIC
                  )
                order by p.createdAt desc
            """)
    Page<Post> findFeedPosts(
            @Param("userId") String userId,
            Pageable pageable
    );

    @Query(value = """
        SELECT * FROM (
            (
                -- Nhánh 1: Bài viết từ Friend (Ưu tiên Index follower_id)
                SELECT p.*
                FROM posts p
                INNER JOIN follows f ON p.author_id = f.following_id
                WHERE p.status = 'ACTIVE' 
                  AND f.follower_id = :userId
                ORDER BY p.created_at DESC
                LIMIT :scanLimit
            )
            UNION ALL
            (
                -- Nhánh 2: Bài viết PUBLIC (Ưu tiên Index status_visibility_created)
                SELECT p.*
                FROM posts p
                WHERE p.status = 'ACTIVE' 
                  AND p.post_visibility = 'PUBLIC'
                ORDER BY p.created_at DESC
                LIMIT :scanLimit
            )
        ) AS feed_temp_table
        ORDER BY feed_temp_table.created_at DESC
        """, nativeQuery = true)
    Slice<Post> findFeedPosts(
            @Param("userId") String userId,
            @Param("scanLimit") int scanLimit,
            Pageable pageable
    );


    List<Post> findAllByIdIn(Set<String> postIds);

    List<Post> findByAuthorId(String authorId, Pageable pageable);


}