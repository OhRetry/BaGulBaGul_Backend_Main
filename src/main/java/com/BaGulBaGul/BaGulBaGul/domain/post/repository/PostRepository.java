package com.BaGulBaGul.BaGulBaGul.domain.post.repository;

import com.BaGulBaGul.BaGulBaGul.domain.post.Post;
import java.util.Optional;

import com.BaGulBaGul.BaGulBaGul.domain.post.repository.queryDSL.FindPostByCondition;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long>, FindPostByCondition {
    @EntityGraph(attributePaths = {"user", "categories.category"})
    Optional<Post> findWithUserAndCategoriesById(Long id);

    @Modifying(clearAutomatically = true)
    @Query(value = "update from Post post set post.likeCount = post.likeCount + 1 where post = :post")
    void increaseLikeCount(@Param(value = "post") Post post);

    @Modifying(clearAutomatically = true)
    @Query(value = "update from Post post set post.likeCount = post.likeCount - 1 where post = :post")
    void decreaseLikeCount(@Param(value = "post") Post post);
}
