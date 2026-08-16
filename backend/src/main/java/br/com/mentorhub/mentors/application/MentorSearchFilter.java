package br.com.mentorhub.mentors.application;

import br.com.mentorhub.mentors.domain.MentorshipModality;

public record MentorSearchFilter(
        String q,
        String name,
        String technology,
        String skill,
        MentorshipModality modality
) {
}
