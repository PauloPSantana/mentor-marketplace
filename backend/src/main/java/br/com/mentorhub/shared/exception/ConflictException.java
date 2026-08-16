package br.com.mentorhub.shared.exception;

public class ConflictException extends BusinessException {

    public ConflictException(String message) {
        super("CONFLICT", message);
    }
}
