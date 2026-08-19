package br.com.mentorhub.payments.api;

import br.com.mentorhub.identity.domain.UserRole;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PaymentReviewCompletionIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("mentorhub")
            .withUsername("mentorhub")
            .withPassword("mentorhub");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("mentorhub.security.jwt.secret", () -> "mentorhub-test-secret-key-32-bytes-min!!");
        registry.add("mentorhub.payments.webhook-secret", () -> "mentorhub-test-webhook-secret");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldChargePayAndBlockCompleteUntilSessionsAreDone() throws Exception {
        Auth mentor = register("Paulo Mentor", UserRole.MENTOR);
        String mentorProfileId = setupMentorProfile(mentor.token());
        Auth mentee = register("Ana Mentee", UserRole.MENTEE);

        mockMvc.perform(post("/api/v1/mentorship-requests")
                        .header("Authorization", "Bearer " + mentee.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "mentorId", mentorProfileId,
                                "message", "Quero mentoria"
                        ))))
                .andExpect(status().isCreated());

        MvcResult received = mockMvc.perform(get("/api/v1/mentorship-requests/received")
                        .header("Authorization", "Bearer " + mentor.token()))
                .andExpect(status().isOk())
                .andReturn();
        String requestId = objectMapper.readTree(received.getResponse().getContentAsString())
                .get(0).get("id").asText();

        MvcResult accepted = mockMvc.perform(patch("/api/v1/mentorship-requests/" + requestId + "/accept")
                        .header("Authorization", "Bearer " + mentor.token()))
                .andExpect(status().isOk())
                .andReturn();
        String mentorshipId = objectMapper.readTree(accepted.getResponse().getContentAsString())
                .get("activeMentorshipId").asText();

        MvcResult createdPayment = mockMvc.perform(post("/api/v1/mentorships/relationships/" + mentorshipId + "/payments")
                        .header("Authorization", "Bearer " + mentee.token())
                        .header("Idempotency-Key", "it-" + mentorshipId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(150.00))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();
        JsonNode payment = objectMapper.readTree(createdPayment.getResponse().getContentAsString());
        String paymentId = payment.get("id").asText();
        String providerTx = payment.get("providerTransactionId").asText();

        mockMvc.perform(post("/api/v1/mentorships/relationships/" + mentorshipId + "/payments")
                        .header("Authorization", "Bearer " + mentee.token())
                        .header("Idempotency-Key", "it-" + mentorshipId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(paymentId));

        mockMvc.perform(get("/api/v1/payments/" + paymentId)
                        .header("Authorization", "Bearer " + mentee.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));

        mockMvc.perform(post("/api/v1/payments/webhooks/SIMULATED")
                        .header("X-Payment-Webhook-Secret", "mentorhub-test-webhook-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "providerTransactionId", providerTx,
                                "status", "PAID"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        mockMvc.perform(post("/api/v1/payments/webhooks/SIMULATED")
                        .header("X-Payment-Webhook-Secret", "mentorhub-test-webhook-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "providerTransactionId", providerTx,
                                "status", "PAID"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        mockMvc.perform(post("/api/v1/mentorships/relationships/" + mentorshipId + "/reviews")
                        .header("Authorization", "Bearer " + mentee.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("rating", 5))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("MENTORSHIP_NOT_COMPLETED"));

        mockMvc.perform(patch("/api/v1/mentorships/relationships/" + mentorshipId + "/complete")
                        .header("Authorization", "Bearer " + mentor.token()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("SESSIONS_INCOMPLETE"));

        mockMvc.perform(get("/api/v1/mentors/" + mentorProfileId + "/rating"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ratingCount").value(0));
    }

    private String setupMentorProfile(String token) throws Exception {
        mockMvc.perform(put("/api/v1/mentors/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "headline", "Engenheiro de Software",
                                "bio", "Mentoria Java",
                                "yearsExperience", 10,
                                "sessionPrice", 150.00,
                                "modality", "ONLINE",
                                "skills", List.of("Backend"),
                                "technologies", List.of("Java"),
                                "active", true
                        ))))
                .andExpect(status().isOk());

        MvcResult me = mockMvc.perform(get("/api/v1/mentors/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(me.getResponse().getContentAsString()).get("id").asText();
    }

    private Auth register(String name, UserRole role) throws Exception {
        String email = name.toLowerCase().replace(" ", ".") + "-" + UUID.randomUUID() + "@email.com";
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", name,
                                "email", email,
                                "password", "senha12345",
                                "role", role.name()
                        ))))
                .andExpect(status().isCreated());
        MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", "senha12345"
                        ))))
                .andExpect(status().isOk())
                .andReturn();
        String token = objectMapper.readTree(login.getResponse().getContentAsString()).get("accessToken").asText();
        return new Auth(token);
    }

    private record Auth(String token) {
    }
}
