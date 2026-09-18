package br.com.mentorhub.scheduling.domain;

import br.com.mentorhub.mentorships.domain.MeetingProvider;

import java.util.UUID;

public interface VideoConferenceService {

    MeetingProvider provider();

    boolean canCreate(UUID hostUserId);

    MeetingDetails create(UUID hostUserId, MeetingCreateCommand command);

    void update(UUID hostUserId, String meetingId, MeetingCreateCommand command);

    void delete(UUID hostUserId, String meetingId);
}
