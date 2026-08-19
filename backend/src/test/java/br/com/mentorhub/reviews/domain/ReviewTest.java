package br.com.mentorhub.reviews.domain;

import br.com.mentorhub.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReviewTest {

    @Test
    void shouldCreatePublishedReview() {
        Review review = Review.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 5, "Excelente");

        assertEquals(ReviewStatus.ACTIVE, review.getStatus());
        assertEquals(5, review.getRating());
        assertTrue(review.countsForAverage());
    }

    @Test
    void shouldRejectInvalidRating() {
        BusinessException error = assertThrows(
                BusinessException.class,
                () -> Review.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 6, null)
        );
        assertEquals("INVALID_RATING", error.getCode());
    }

    @Test
    void shouldAllowEditInsideWindow() {
        Review review = Review.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 4, null);
        Review updated = review.update(5, "Atualizado", Instant.now());

        assertEquals(5, updated.getRating());
        assertEquals("Atualizado", updated.getComment());
    }

    @Test
    void shouldRejectEditAfterWindow() {
        Review review = Review.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 4, null);
        BusinessException error = assertThrows(
                BusinessException.class,
                () -> review.update(5, "Tarde", review.getCreatedAt().plus(Review.EDIT_WINDOW).plusSeconds(1))
        );
        assertEquals("REVIEW_EDIT_EXPIRED", error.getCode());
    }

    @Test
    void shouldExcludeHiddenReviewsFromAverage() {
        Review hidden = Review.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 1, "Ruim").hide();
        Review removed = Review.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 2, "Moderado").remove();

        assertEquals(ReviewStatus.HIDDEN, hidden.getStatus());
        assertEquals(ReviewStatus.REMOVED, removed.getStatus());
        assertFalse(hidden.countsForAverage());
        assertFalse(removed.countsForAverage());
    }
}
