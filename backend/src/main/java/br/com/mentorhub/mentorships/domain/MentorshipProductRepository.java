package br.com.mentorhub.mentorships.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MentorshipProductRepository {

    MentorshipProduct save(MentorshipProduct product);

    Optional<MentorshipProduct> findById(UUID id);

    List<MentorshipProduct> findByIdIn(Collection<UUID> ids);

    List<MentorshipProduct> findByMentorId(UUID mentorId);

    Optional<MentorshipProduct> findFirstByMentorId(UUID mentorId);

    List<MentorshipProduct> findPublished();

    List<MentorshipProduct> findPublishedByMentorId(UUID mentorId);
}
