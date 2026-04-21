package com.gtalent.helloworld.controller;

import java.util.List;

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

import com.gtalent.helloworld.repository.CommentRepository;
import com.gtalent.helloworld.repository.PostRepository;
import com.gtalent.helloworld.service.entities.Comment;
import com.gtalent.helloworld.service.entities.Post;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    private static final String POST_NOT_FOUND = "Post not found: ";
    private static final String COMMENT_NOT_FOUND = "Comment not found: ";
    private static final String COMMENT_NOT_BELONG = "Comment does not belong to post: ";

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public CommentController(PostRepository postRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    // Create - 透過 Post 便利方法同步雙向關係
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Comment create(@PathVariable Long postId, @Valid @RequestBody Comment comment) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, POST_NOT_FOUND + postId));
        post.addComment(comment);
        // 直接存 comment 以取得受管理實體(含生成的 id)
        return commentRepository.save(comment);
    }

    // Read all by post
    @GetMapping
    public List<Comment> findAll(@PathVariable Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, POST_NOT_FOUND + postId);
        }
        return commentRepository.findByPostId(postId);
    }

    // Read one
    @GetMapping("/{commentId}")
    public Comment findOne(@PathVariable Long postId, @PathVariable Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, COMMENT_NOT_FOUND + commentId));
        if (comment.getPost() == null || !comment.getPost().getId().equals(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, COMMENT_NOT_BELONG + postId);
        }
        return comment;
    }

    // Update
    @PutMapping("/{commentId}")
    public Comment update(@PathVariable Long postId, @PathVariable Long commentId,
                          @Valid @RequestBody Comment body) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, COMMENT_NOT_FOUND + commentId));
        if (comment.getPost() == null || !comment.getPost().getId().equals(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, COMMENT_NOT_BELONG + postId);
        }
        comment.setAuthor(body.getAuthor());
        comment.setContent(body.getContent());
        return commentRepository.save(comment);
    }

    // Delete - 使用 orphanRemoval 從 Post 端移除
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(@PathVariable Long postId, @PathVariable Long commentId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, POST_NOT_FOUND + postId));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, COMMENT_NOT_FOUND + commentId));
        if (comment.getPost() == null || !comment.getPost().getId().equals(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, COMMENT_NOT_BELONG + postId);
        }
        post.removeComment(comment);
        postRepository.save(post);
        return ResponseEntity.noContent().build();
    }
}
