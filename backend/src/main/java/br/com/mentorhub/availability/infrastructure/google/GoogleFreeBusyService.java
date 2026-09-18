package br.com.mentorhub.availability.infrastructure.google;

import br.com.mentorhub.availability.domain.BusyPeriod;
import br.com.mentorhub.scheduling.application.GoogleConnectionTokenService;
import br.com.mentorhub.scheduling.domain.GoogleConnectionRepository;
import br.com.mentorhub.shared.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class GoogleFreeBusyService {

    private static final Logger log = LoggerFactory.getLogger(GoogleFreeBusyService.class);
    private static final String FREE_BUSY_URL = "https://www.googleapis.com/calendar/v3/freeBusy";

    private final GoogleConnectionRepository googleConnectionRepository;
    private final GoogleConnectionTokenService googleConnectionTokenService;
    private final RestClient restClient;

    public GoogleFreeBusyService(
            GoogleConnectionRepository googleConnectionRepository,
            GoogleConnectionTokenService googleConnectionTokenService
    ) {
        this.googleConnectionRepository = googleConnectionRepository;
        this.googleConnectionTokenService = googleConnectionTokenService;
        this.restClient = RestClient.create();
    }

    public List<BusyPeriod> listBusy(UUID mentorUserId, Instant from, Instant to) {
        if (mentorUserId == null || googleConnectionRepository.findByUserId(mentorUserId).isEmpty()) {
            return List.of();
        }
        try {
            String accessToken = googleConnectionTokenService.accessToken(mentorUserId);
            JsonNode json = restClient.post()
                    .uri(FREE_BUSY_URL)
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "timeMin", from.toString(),
                            "timeMax", to.toString(),
                            "timeZone", "UTC",
                            "items", List.of(Map.of("id", "primary"))
                    ))
                    .retrieve()
                    .body(JsonNode.class);
            if (json == null) {
                return List.of();
            }
            JsonNode busy = json.path("calendars").path("primary").path("busy");
            List<BusyPeriod> periods = new ArrayList<>();
            if (busy.isArray()) {
                for (JsonNode item : busy) {
                    Instant start = parseInstant(item.path("start").asText(null));
                    Instant end = parseInstant(item.path("end").asText(null));
                    if (start != null && end != null && end.isAfter(start)) {
                        periods.add(new BusyPeriod(start, end));
                    }
                }
            }
            return List.copyOf(periods);
        } catch (BusinessException ex) {
            log.warn("Google FreeBusy indisponível. code={}", ex.getCode());
            return List.of();
        } catch (org.springframework.web.client.RestClientResponseException ex) {
            log.warn("Falha ao consultar horários ocupados no Google Calendar. status={}", ex.getStatusCode().value());
            return List.of();
        } catch (Exception ex) {
            log.warn("Falha ao consultar horários ocupados no Google Calendar. type={}", ex.getClass().getSimpleName());
            return List.of();
        }
    }

    private static Instant parseInstant(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (Exception ex) {
            return OffsetDateTime.parse(value).toInstant();
        }
    }
}
