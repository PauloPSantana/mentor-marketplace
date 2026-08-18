package br.com.mentorhub.feed.application;

import br.com.mentorhub.feed.api.dto.CommentResponse;
import br.com.mentorhub.feed.domain.Comment;
import br.com.mentorhub.feed.domain.Post;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ReplyCommentService {

    private final CommentComposer commentComposer;

    public ReplyCommentService(CommentComposer commentComposer) {
        this.commentComposer = commentComposer;
    }

    @Transactional
    public CommentResponse execute(UUID postId, UUID parentCommentId, UUID currentUserId, String content) {
        Post post = commentComposer.requirePost(postId);
        Comment parent = commentComposer.requireCommentOnPost(postId, parentCommentId);
        commentComposer.validateParentComment(post, parent);
        Comment saved = commentComposer.persist(post, currentUserId, content, parent);
        return CommentResponse.from(saved);
    }
}
