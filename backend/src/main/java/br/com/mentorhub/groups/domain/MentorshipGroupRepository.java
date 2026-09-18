package br.com.mentorhub.groups.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MentorshipGroupRepository {

    MentorshipGroup save(MentorshipGroup group);

    Optional<MentorshipGroup> findById(UUID id);

    List<MentorshipGroup> findByMemberUserId(UUID userId);
}
