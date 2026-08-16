package br.com.mentorhub.feed.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface PostRepository {

    Post save(Post post);

    Optional<Post> findById(UUID id);

    Page<Post> findFeed(Pageable pageable);
}
