package br.com.mentorhub.studyplans.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudyTaskProgressRepository {

    StudyTaskProgress save(StudyTaskProgress progress);

    Optional<StudyTaskProgress> findByStudyTaskIdAndMenteeUserId(UUID studyTaskId, UUID menteeUserId);

    List<StudyTaskProgress> findByStudyTaskIdInAndMenteeUserId(Collection<UUID> studyTaskIds, UUID menteeUserId);
}
