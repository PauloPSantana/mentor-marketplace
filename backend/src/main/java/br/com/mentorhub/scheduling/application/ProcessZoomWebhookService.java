package br.com.mentorhub.scheduling.application;

import br.com.mentorhub.mentorships.domain.MentorshipSession;
import br.com.mentorhub.mentorships.domain.MentorshipSessionRepository;
import br.com.mentorhub.shared.exception.UnauthorizedException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;

@Service
public class ProcessZoomWebhookService {

    private static final Logger log = LoggerFactory.getLogger(ProcessZoomWebhookService.class);
    private static final long MAX_SKEW_SECONDS = 300;

    private final MentorshipSessionRepository mentorshipSessionRepository;
    private final ObjectMapper objectMapper;
    private final String webhookSecret;

    public ProcessZoomWebhookService(
            MentorshipSessionRepository mentorshipSessionRepository,
            ObjectMapper objectMapper,
            @Value("${mentorhub.zoom.webhook-secret:}") String webhookSecret
    ) {
        this.mentorshipSessionRepository = mentorshipSessionRepository;
        this.objectMapper = objectMapper;
        this.webhookSecret = webhookSecret == null ? "" : webhookSecret.trim();
    }

    public boolean isConfigured() {
        return !webhookSecret.isBlank();
    }

    @Transactional
    public Map<String, String> execute(String signature, String timestamp, String rawBody) {
        if (!isConfigured()) {
            throw new UnauthorizedException("Webhook Zoom não configurado");
        }
        JsonNode json;
        try {
            json = objectMapper.readTree(rawBody == null ? "{}" : rawBody);
        } catch (Exception ex) {
            throw new UnauthorizedException("Webhook Zoom inválido");
        }
        String event = json.path("event").asText("");
        if ("endpoint.url_validation".equals(event)) {
            return urlValidation(json.path("payload").path("plainToken").asText(""));
        }
        verifySignature(signature, timestamp, rawBody);
        String meetingId = readMeetingId(json.path("payload").path("object"));
        if (meetingId == null) {
            return Map.of("status", "ignored");
        }
        MentorshipSession session = mentorshipSessionRepository.findByZoomMeetingId(meetingId).orElse(null);
        if (session == null) {
            return Map.of("status", "ignored");
        }
        Instant at = Instant.now();
        MentorshipSession updated = switch (event) {
            case "meeting.started" -> session.markZoomStarted(at);
            case "meeting.ended" -> session.markZoomEnded(at);
            case "meeting.deleted" -> session;
            default -> session;
        };
        if (updated != session) {
            mentorshipSessionRepository.save(updated);
        }
        log.info("Webhook Zoom processado. event={} session={}", event, session.getId());
        return Map.of("status", "ok");
    }

    private Map<String, String> urlValidation(String plainToken) {
        return Map.of(
                "plainToken", plainToken,
                "encryptedToken", hmacHex(plainToken)
        );
    }

    private void verifySignature(String signature, String timestamp, String rawBody) {
        if (signature == null || timestamp == null) {
            throw new UnauthorizedException("Webhook Zoom inválido");
        }
        try {
            long ts = Long.parseLong(timestamp);
            long now = Instant.now().getEpochSecond();
            if (Math.abs(now - ts) > MAX_SKEW_SECONDS) {
                throw new UnauthorizedException("Webhook Zoom inválido");
            }
        } catch (NumberFormatException ex) {
            throw new UnauthorizedException("Webhook Zoom inválido");
        }
        String expected = "v0=" + hmacHex("v0:" + timestamp + ":" + (rawBody == null ? "" : rawBody));
        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8))) {
            throw new UnauthorizedException("Webhook Zoom inválido");
        }
    }

    private String hmacHex(String message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new UnauthorizedException("Webhook Zoom inválido");
        }
    }

    private static String readMeetingId(JsonNode object) {
        if (object == null || object.isMissingNode()) {
            return null;
        }
        JsonNode id = object.path("id");
        if (id.isNumber()) {
            return String.valueOf(id.asLong());
        }
        String text = id.asText(null);
        return text == null || text.isBlank() ? null : text;
    }
}
