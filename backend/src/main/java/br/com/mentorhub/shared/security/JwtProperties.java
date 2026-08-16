package br.com.mentorhub.shared.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mentorhub.security.jwt")
public record JwtProperties(String secret, long expirationMs) {
}
