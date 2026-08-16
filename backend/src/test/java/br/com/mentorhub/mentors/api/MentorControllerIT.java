package br.com.mentorhub.mentors.api;

import br.com.mentorhub.identity.domain.UserRole;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class MentorControllerIT {

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
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldUpdateAndReadOwnMentorProfile() throws Exception {
        String email = "mentor-profile-" + UUID.randomUUID() + "@email.com";

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Paulo Santana",
                                "email", email,
                                "password", "senha12345",
                                "role", UserRole.MENTOR.name()
                        ))))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", "senha12345"
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("accessToken")
                .asText();

        mockMvc.perform(put("/api/v1/mentors/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "headline", "Engenheiro de Software",
                                "bio", "Especialista em Java e Spring Boot",
                                "yearsExperience", 10,
                                "linkedinUrl", "https://linkedin.com/in/paulo",
                                "githubUrl", "https://github.com/paulo",
                                "sessionPrice", 150.00,
                                "modality", "ONLINE",
                                "skills", List.of("Arquitetura de Software", "Backend"),
                                "technologies", List.of("Java", "Spring Boot", "Docker"),
                                "active", true
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headline").value("Engenheiro de Software"))
                .andExpect(jsonPath("$.modality").value("ONLINE"))
                .andExpect(jsonPath("$.skills.length()").value(2))
                .andExpect(jsonPath("$.technologies.length()").value(3));

        MvcResult meResult = mockMvc.perform(get("/api/v1/mentors/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Paulo Santana"))
                .andReturn();

        String profileId = objectMapper.readTree(meResult.getResponse().getContentAsString())
                .get("id")
                .asText();

        mockMvc.perform(get("/api/v1/mentors/" + profileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headline").value("Engenheiro de Software"));

        mockMvc.perform(get("/api/v1/mentors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].headline").value("Engenheiro de Software"));
    }

    @Test
    void shouldRequireAuthForOwnProfile() throws Exception {
        mockMvc.perform(get("/api/v1/mentors/me"))
                .andExpect(status().isUnauthorized());
    }
}
