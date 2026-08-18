package br.com.mentorhub.mentorships.domain;

import br.com.mentorhub.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MentorshipProductTest {

    @Test
    void shouldCreatePublishedProduct() {
        MentorshipProduct product = MentorshipProduct.create(
                UUID.randomUUID(),
                " Mentoria Java ",
                "Mentoria-Java",
                "Descrição",
                "Backend",
                "TODOS",
                4,
                8,
                5,
                new BigDecimal("200.00")
        );

        assertEquals("Mentoria Java", product.getTitle());
        assertEquals("mentoria-java", product.getSlug());
        assertTrue(product.isPublished());
        assertTrue(product.hasVacancy(4));
        assertFalse(product.hasVacancy(5));
    }

    @Test
    void shouldAllowZeroPriceWhenNotDefined() {
        MentorshipProduct product = MentorshipProduct.create(
                UUID.randomUUID(),
                "Mentoria",
                "mentoria",
                "Descrição",
                "Geral",
                "TODOS",
                4,
                4,
                10,
                BigDecimal.ZERO
        );

        assertEquals(new BigDecimal("0"), product.getPrice());
        assertTrue(product.isPublished());
    }

    @Test
    void shouldRejectNegativePrice() {
        BusinessException error = assertThrows(
                BusinessException.class,
                () -> MentorshipProduct.create(
                        UUID.randomUUID(),
                        "Mentoria",
                        "mentoria",
                        "Descrição",
                        "Geral",
                        "TODOS",
                        4,
                        4,
                        10,
                        new BigDecimal("-10.00")
                )
        );
        assertEquals("INVALID_PRODUCT_PRICE", error.getCode());
    }
}
