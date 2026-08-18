package br.com.mentorhub.feed.infrastructure.persistence;

import java.util.UUID;

public interface PostIdCountView {

    UUID getPostId();

    Long getCnt();
}
