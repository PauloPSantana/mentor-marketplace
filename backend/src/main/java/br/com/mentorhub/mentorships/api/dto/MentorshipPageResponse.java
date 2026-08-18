package br.com.mentorhub.mentorships.api.dto;

import java.util.List;

public record MentorshipPageResponse(
        List<MentorshipRelationshipResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
}
