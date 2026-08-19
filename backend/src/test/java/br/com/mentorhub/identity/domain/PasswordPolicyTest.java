package br.com.mentorhub.identity.domain;

import br.com.mentorhub.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PasswordPolicyTest {

    @Test
    void shouldAcceptPasswordWithLettersAndDigits() {
        assertDoesNotThrow(() -> PasswordPolicy.validate("senha12345"));
    }

    @Test
    void shouldRejectShortPassword() {
        BusinessException error = assertThrows(BusinessException.class, () -> PasswordPolicy.validate("ab12"));
        assertEquals("WEAK_PASSWORD", error.getCode());
    }

    @Test
    void shouldRejectPasswordWithoutDigits() {
        BusinessException error = assertThrows(BusinessException.class, () -> PasswordPolicy.validate("senhasenha"));
        assertEquals("WEAK_PASSWORD", error.getCode());
    }

    @Test
    void shouldRejectMismatchedConfirmation() {
        BusinessException error = assertThrows(
                BusinessException.class,
                () -> PasswordPolicy.requireConfirmation("senha12345", "outra12345")
        );
        assertEquals("PASSWORD_MISMATCH", error.getCode());
    }
}
