package com.gtalent.helloworld.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gtalent.helloworld.service.entities.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostId(Long postId);
}
