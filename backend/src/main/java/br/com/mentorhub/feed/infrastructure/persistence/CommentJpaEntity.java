package br.com.mentorhub.feed.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "post_comments")
public class CommentJpaEntity {

    @Id
    private UUID id;

    @Column(name = "post_id", nullable = false)
    private UUID postId;

    @Column(name = "parent_comment_id")
    private UUID parentCommentId;

    @Column(name = "author_user_id", nullable = false)
    private UUID authorUserId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "author_name", nullable = false, length = 120)
    private String authorName;

    @Column(name = "author_photo_url", length = 500)
    private String authorPhotoUrl;

    @Column(name = "author_headline", length = 180)
    private String authorHeadline;

    @Column(name = "author_role", nullable = false, length = 30)
    private String authorRole;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected CommentJpaEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPostId() {
        return postId;
    }

    public void setPostId(UUID postId) {
        this.postId = postId;
    }

    public UUID getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(UUID parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    public UUID getAuthorUserId() {
        return authorUserId;
    }

    public void setAuthorUserId(UUID authorUserId) {
        this.authorUserId = authorUserId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorPhotoUrl() {
        return authorPhotoUrl;
    }

    public void setAuthorPhotoUrl(String authorPhotoUrl) {
        this.authorPhotoUrl = authorPhotoUrl;
    }

    public String getAuthorHeadline() {
        return authorHeadline;
    }

    public void setAuthorHeadline(String authorHeadline) {
        this.authorHeadline = authorHeadline;
    }

    public String getAuthorRole() {
        return authorRole;
    }

    public void setAuthorRole(String authorRole) {
        this.authorRole = authorRole;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
