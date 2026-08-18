package br.com.mentorhub.identity.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findAllByIds(Collection<UUID> ids);
}
