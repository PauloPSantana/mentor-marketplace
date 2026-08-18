package br.com.mentorhub.feed.api;

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

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PostControllerIT {

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
    void shouldCreateListUpdateAndDeleteOwnPost() throws Exception {
        String token = registerAndLogin("Paulo Santana", UserRole.MENTOR);

        MvcResult created = mockMvc.perform(post("/api/v1/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "content", "Como a mentoria pode acelerar sua carreira profissional?"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Como a mentoria pode acelerar sua carreira profissional?"))
                .andExpect(jsonPath("$.authorName").value("Paulo Santana"))
                .andExpect(jsonPath("$.authorRole").value("MENTOR"))
                .andReturn();

        String postId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/v1/posts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(postId))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.last").value(true));

        mockMvc.perform(put("/api/v1/posts/" + postId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "content", "Texto atualizado",
                                "imageUrl", "https://cdn.example.com/post.png"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Texto atualizado"))
                .andExpect(jsonPath("$.imageUrl").value("https://cdn.example.com/post.png"));

        mockMvc.perform(delete("/api/v1/posts/" + postId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/posts/" + postId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldForbidEditingSomeoneElsesPost() throws Exception {
        String authorToken = registerAndLogin("Paulo Santana", UserRole.MENTOR);
        String otherToken = registerAndLogin("João Silva", UserRole.MENTEE);

        MvcResult created = mockMvc.perform(post("/api/v1/posts")
                        .header("Authorization", "Bearer " + authorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("content", "Post do Paulo"))))
                .andExpect(status().isCreated())
                .andReturn();

        String postId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(put("/api/v1/posts/" + postId)
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("content", "Tentativa"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRequireAuthToReadFeed() throws Exception {
        mockMvc.perform(get("/api/v1/posts"))
                .andExpect(status().isUnauthorized());
    }

    private String registerAndLogin(String name, UserRole role) throws Exception {
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

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", "senha12345"
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("accessToken")
                .asText();
    }
}
