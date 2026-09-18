package br.com.mentorhub.identity.application;

import java.util.UUID;

public record InstitutionUserRegisteredEvent(UUID userId, String institutionName) {
}
