package br.com.mentorhub.shared.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> home() {
        return ResponseEntity.ok(Map.of(
                "app", "mentorhub-api",
                "frontend", "http://localhost:3000",
                "googleConnect", "http://localhost:8080/api/integrations/google/connect"
        ));
    }
}
