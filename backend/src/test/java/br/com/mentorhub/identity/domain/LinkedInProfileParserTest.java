package br.com.mentorhub.identity.domain;

import br.com.mentorhub.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LinkedInProfileParserTest {

    @Test
    void shouldParseCanonicalProfileUrl() {
        LinkedInProfileParser.Preview preview = LinkedInProfileParser.parse("https://www.linkedin.com/in/paulo-santana");

        assertEquals("https://www.linkedin.com/in/paulo-santana", preview.url());
        assertEquals("paulo-santana", preview.username());
        assertEquals("Paulo Santana", preview.suggestedName());
    }

    @Test
    void shouldRejectInvalidUrl() {
        BusinessException error = assertThrows(
                BusinessException.class,
                () -> LinkedInProfileParser.parse("https://example.com/in/paulo")
        );
        assertEquals("INVALID_LINKEDIN_URL", error.getCode());
    }
}
