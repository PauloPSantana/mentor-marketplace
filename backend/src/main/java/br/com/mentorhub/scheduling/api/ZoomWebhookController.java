package br.com.mentorhub.scheduling.api;

import br.com.mentorhub.scheduling.application.ProcessZoomWebhookService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/zoom")
public class ZoomWebhookController {

    private final ProcessZoomWebhookService processZoomWebhookService;

    public ZoomWebhookController(ProcessZoomWebhookService processZoomWebhookService) {
        this.processZoomWebhookService = processZoomWebhookService;
    }

    @PostMapping(value = "/webhooks", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> webhook(
            @RequestHeader(value = "x-zm-signature", required = false) String signature,
            @RequestHeader(value = "x-zm-request-timestamp", required = false) String timestamp,
            @RequestBody String rawBody
    ) {
        return ResponseEntity.ok(processZoomWebhookService.execute(signature, timestamp, rawBody));
    }
}
