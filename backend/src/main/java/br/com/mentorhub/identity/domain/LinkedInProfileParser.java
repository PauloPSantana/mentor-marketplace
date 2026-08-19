package br.com.mentorhub.identity.domain;

import br.com.mentorhub.shared.exception.BusinessException;

import java.net.URI;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class LinkedInProfileParser {

    private static final Pattern PROFILE = Pattern.compile(
            "(?i)^https?://(?:[\\w-]+\\.)?linkedin\\.com/in/([A-Za-z0-9\\-_%]+)(?:/.*)?(?:\\?.*)?$"
    );

    private LinkedInProfileParser() {
    }

    public static Preview parse(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            throw new BusinessException("INVALID_LINKEDIN_URL", "Informe o link do seu perfil no LinkedIn");
        }
        String trimmed = rawUrl.trim();
        Matcher matcher = PROFILE.matcher(trimmed);
        if (!matcher.matches()) {
            throw new BusinessException(
                    "INVALID_LINKEDIN_URL",
                    "Use um link válido do LinkedIn, por exemplo https://www.linkedin.com/in/seu-perfil"
            );
        }
        String username = matcher.group(1).replace("%20", "-");
        String canonical = "https://www.linkedin.com/in/" + username;
        return new Preview(canonical, username, suggestedName(username));
    }

    public static String normalizeOrNull(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return null;
        }
        return parse(rawUrl).url();
    }

    public static boolean looksLikeLinkedInHost(String url) {
        try {
            URI uri = URI.create(url.trim());
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
            return host.equals("linkedin.com") || host.endsWith(".linkedin.com");
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private static String suggestedName(String username) {
        return Arrays.stream(username.split("[-_]+"))
                .filter(part -> !part.isBlank())
                .map(part -> Character.toUpperCase(part.charAt(0)) + part.substring(1).toLowerCase(Locale.ROOT))
                .collect(Collectors.joining(" "));
    }

    public record Preview(String url, String username, String suggestedName) {
    }
}
