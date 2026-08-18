package br.com.mentorhub.mentorships.api;

import br.com.mentorhub.mentorships.api.dto.MentorshipSessionResponse;
import br.com.mentorhub.mentorships.application.ListAgendaService;
import br.com.mentorhub.shared.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/agenda")
public class AgendaController {

    private final ListAgendaService listAgendaService;

    public AgendaController(ListAgendaService listAgendaService) {
        this.listAgendaService = listAgendaService;
    }

    @GetMapping
    public ResponseEntity<List<MentorshipSessionResponse>> list(
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to
    ) {
        return ResponseEntity.ok(listAgendaService.execute(SecurityUtils.requireCurrentUserId(), from, to));
    }

    @GetMapping("/next")
    public ResponseEntity<MentorshipSessionResponse> next() {
        MentorshipSessionResponse next = listAgendaService.next(SecurityUtils.requireCurrentUserId());
        if (next == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(next);
    }
}
