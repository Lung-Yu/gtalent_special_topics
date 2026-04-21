package com.gtalent.helloworld.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gtalent.helloworld.service.entities.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
}
