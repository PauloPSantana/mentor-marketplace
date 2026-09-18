package br.com.mentorhub.scheduling.application;

import br.com.mentorhub.mentorships.domain.MentorshipSession;
import br.com.mentorhub.mentorships.domain.MentorshipSessionRepository;
import br.com.mentorhub.mentorships.domain.ZoomMeetingStatus;
import br.com.mentorhub.shared.exception.UnauthorizedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessZoomWebhookServiceTest {

    private static final String SECRET = "zoom-webhook-secret";

    @Mock
    private MentorshipSessionRepository mentorshipSessionRepository;

    private ProcessZoomWebhookService service;

    @BeforeEach
    void setUp() {
        service = new ProcessZoomWebhookService(mentorshipSessionRepository, new ObjectMapper(), SECRET);
    }

    @Test
    void shouldAnswerUrlValidation() {
        Map<String, String> response = service.execute(
                null,
                null,
                "{\"event\":\"endpoint.url_validation\",\"payload\":{\"plainToken\":\"abc\"}}"
        );

        assertEquals("abc", response.get("plainToken"));
        assertEquals(hmac("abc"), response.get("encryptedToken"));
    }

    @Test
    void shouldMarkMeetingStarted() {
        MentorshipSession session = MentorshipSession.schedule(
                UUID.randomUUID(),
                Instant.now().plusSeconds(3600),
                60,
                null,
                null,
                UUID.randomUUID(),
                240
        ).attachZoomMeeting("555", "https://zoom.us/j/555", "https://zoom.us/s/555");
        when(mentorshipSessionRepository.findByZoomMeetingId("555")).thenReturn(Optional.of(session));
        when(mentorshipSessionRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));

        String body = "{\"event\":\"meeting.started\",\"payload\":{\"object\":{\"id\":555}}}";
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        service.execute("v0=" + hmac("v0:" + timestamp + ":" + body), timestamp, body);

        ArgumentCaptor<MentorshipSession> captor = ArgumentCaptor.forClass(MentorshipSession.class);
        verify(mentorshipSessionRepository).save(captor.capture());
        assertEquals(ZoomMeetingStatus.STARTED, captor.getValue().getZoomStatus());
    }

    @Test
    void shouldRejectInvalidSignature() {
        String body = "{\"event\":\"meeting.ended\",\"payload\":{\"object\":{\"id\":\"1\"}}}";
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        assertThrows(
                UnauthorizedException.class,
                () -> service.execute("v0=invalid", timestamp, body)
        );
    }

    private static String hmac(String message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
