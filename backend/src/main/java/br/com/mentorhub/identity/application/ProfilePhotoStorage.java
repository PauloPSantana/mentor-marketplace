package br.com.mentorhub.identity.application;

import java.util.UUID;

public interface ProfilePhotoStorage {

    String save(UUID userId, byte[] content, String extension);
}
