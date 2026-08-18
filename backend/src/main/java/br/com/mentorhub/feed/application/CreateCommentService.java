package br.com.mentorhub.feed.application;

import br.com.mentorhub.feed.api.dto.CommentResponse;
import br.com.mentorhub.feed.domain.Comment;
import br.com.mentorhub.feed.domain.Post;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CreateCommentService {

    private final CommentComposer commentComposer;

    public CreateCommentService(CommentComposer commentComposer) {
        this.commentComposer = commentComposer;
    }

    @Transactional
    public CommentResponse execute(UUID postId, UUID currentUserId, String content) {
        Post post = commentComposer.requirePost(postId);
        Comment saved = commentComposer.persist(post, currentUserId, content, null);
        return CommentResponse.from(saved);
    }
}
