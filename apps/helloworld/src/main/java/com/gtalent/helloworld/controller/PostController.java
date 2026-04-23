package com.gtalent.helloworld.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.gtalent.helloworld.repository.PostRepository;
import com.gtalent.helloworld.service.entities.Post;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostRepository postRepository;

    public PostController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    // Create
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Post create(@Valid @RequestBody Post post) {
        if (post.getComments() != null) {
            post.getComments().forEach(c -> c.setPost(post));
        }
        return postRepository.save(post);
    }

    // Read all

    // ?page=0&size=10&sort=createdAt,desc
    @GetMapping
    public Page<Post> findAll(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
                
        return postRepository.findAll(pageable);
    }

    // Read one
    @GetMapping("/{id}")
    public Post findOne(@PathVariable Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found: " + id));
    }

    // Update
    @PutMapping("/{id}")
    public Post update(@PathVariable Long id, @Valid @RequestBody Post body) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found: " + id));
        post.setTitle(body.getTitle());
        post.setContent(body.getContent());
        return postRepository.save(post);
    }

    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!postRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found: " + id);
        }
        postRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
