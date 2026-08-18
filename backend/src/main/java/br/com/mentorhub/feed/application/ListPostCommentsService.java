package br.com.mentorhub.feed.application;

import br.com.mentorhub.feed.api.dto.CommentResponse;
import br.com.mentorhub.feed.api.dto.PostCommentsResponse;
import br.com.mentorhub.feed.domain.Comment;
import br.com.mentorhub.feed.domain.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ListPostCommentsService {

    private final CommentComposer commentComposer;
    private final CommentRepository commentRepository;

    public ListPostCommentsService(CommentComposer commentComposer, CommentRepository commentRepository) {
        this.commentComposer = commentComposer;
        this.commentRepository = commentRepository;
    }

    @Transactional(readOnly = true)
    public PostCommentsResponse execute(UUID postId) {
        commentComposer.requirePost(postId);
        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        long activeCount = comments.stream().filter(Comment::isActive).count();
        return new PostCommentsResponse(activeCount, buildTree(comments));
    }

    private List<CommentResponse> buildTree(List<Comment> comments) {
        Map<UUID, CommentResponse> byId = new LinkedHashMap<>();
        List<CommentResponse> roots = new ArrayList<>();

        for (Comment comment : comments) {
            byId.put(comment.getId(), CommentResponse.from(comment));
        }

        for (Comment comment : comments) {
            CommentResponse current = byId.get(comment.getId());
            if (comment.getParentCommentId() == null) {
                roots.add(current);
                continue;
            }
            CommentResponse parent = byId.get(comment.getParentCommentId());
            if (parent == null) {
                roots.add(current);
                continue;
            }
            parent.replies().add(current);
        }

        sortRepliesRecursively(roots);
        return roots;
    }

    private void sortRepliesRecursively(List<CommentResponse> items) {
        items.sort(Comparator.comparing(CommentResponse::createdAt));
        for (CommentResponse item : items) {
            if (!item.replies().isEmpty()) {
                sortRepliesRecursively(item.replies());
            }
        }
    }
}
