package br.com.mentorhub.feed.application;

import br.com.mentorhub.feed.domain.Comment;
import br.com.mentorhub.feed.domain.CommentRepository;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRole;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteCommentService {

    private final CommentComposer commentComposer;
    private final CommentRepository commentRepository;

    public DeleteCommentService(CommentComposer commentComposer, CommentRepository commentRepository) {
        this.commentComposer = commentComposer;
        this.commentRepository = commentRepository;
    }

    @Transactional
    public void execute(UUID postId, UUID commentId, UUID currentUserId) {
        commentComposer.requirePost(postId);
        Comment comment = commentComposer.requireCommentOnPost(postId, commentId);
        User currentUser = commentComposer.requireUser(currentUserId);
        boolean isOwner = comment.isOwnedBy(currentUserId);
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Apenas o autor ou um administrador pode excluir o comentário");
        }
        if (comment.isDeleted()) {
            return;
        }
        commentRepository.save(comment.markAsDeleted());
    }
}
