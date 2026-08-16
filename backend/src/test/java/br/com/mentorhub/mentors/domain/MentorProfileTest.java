package br.com.mentorhub.mentors.domain;

import br.com.mentorhub.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MentorProfileTest {

    @Test
    void shouldCreateEmptyActiveProfile() {
        UUID userId = UUID.randomUUID();
        MentorProfile profile = MentorProfile.create(userId);

        assertEquals(userId, profile.getUserId());
        assertTrue(profile.isActive());
        assertTrue(profile.getSkills().isEmpty());
        assertEquals(0, BigDecimal.ZERO.compareTo(profile.getRatingAvg()));
    }

    @Test
    void shouldUpdateProfileFields() {
        MentorProfile profile = MentorProfile.create(UUID.randomUUID());

        profile.update(
                "Engenheiro de Software",
                "Bio",
                8,
                null,
                "https://linkedin.com/in/paulo",
                "https://github.com/paulo",
                new BigDecimal("150.00"),
                MentorshipModality.ONLINE,
                Set.of("Arquitetura de Software", "Backend"),
                Set.of("Java", "Spring Boot"),
                true
        );

        assertEquals("Engenheiro de Software", profile.getHeadline());
        assertEquals(MentorshipModality.ONLINE, profile.getModality());
        assertEquals(2, profile.getSkills().size());
        assertEquals(2, profile.getTechnologies().size());
        assertEquals(0, new BigDecimal("150.00").compareTo(profile.getSessionPrice()));
    }

    @Test
    void shouldRejectNegativeSessionPrice() {
        MentorProfile profile = MentorProfile.create(UUID.randomUUID());

        assertThrows(
                BusinessException.class,
                () -> profile.update(
                        "Headline",
                        null,
                        1,
                        null,
                        null,
                        null,
                        new BigDecimal("-10"),
                        MentorshipModality.ONLINE,
                        Set.of(),
                        Set.of(),
                        true
                )
        );
    }
}
