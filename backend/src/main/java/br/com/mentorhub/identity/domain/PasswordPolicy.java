package br.com.mentorhub.identity.domain;

import br.com.mentorhub.shared.exception.BusinessException;

public final class PasswordPolicy {

    private PasswordPolicy() {
    }

    public static void validate(String password) {
        if (password == null || password.length() < 8 || password.length() > 72) {
            throw new BusinessException("WEAK_PASSWORD", "A senha deve ter entre 8 e 72 caracteres");
        }
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        if (!hasLetter || !hasDigit) {
            throw new BusinessException("WEAK_PASSWORD", "A senha deve conter letras e números");
        }
    }

    public static void requireConfirmation(String password, String confirmation) {
        if (confirmation != null && !confirmation.isBlank() && !confirmation.equals(password)) {
            throw new BusinessException("PASSWORD_MISMATCH", "As senhas não coincidem");
        }
    }
}
