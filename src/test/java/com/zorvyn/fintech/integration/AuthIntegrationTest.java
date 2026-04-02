package com.zorvyn.fintech.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zorvyn.fintech.dto.request.LoginRequest;
import com.zorvyn.fintech.dto.request.RegisterRequest;
import com.zorvyn.fintech.entity.Role;
import com.zorvyn.fintech.repository.RoleRepository;
import com.zorvyn.fintech.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // Disable MongoDB/Redis for this test to focus on JPA/Auth flow
        registry.add("spring.autoconfigure.exclude", () -> 
                "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
                "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration," +
                "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration," +
                "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        
        // Ensure USER role exists in test DB
        if (roleRepository.findByName("user").isEmpty()) {
            Role role = new Role();
            role.setName("user");
            role.setPermissions(List.of("read", "write"));
            roleRepository.save(role);
        }
    }

    @Test
    void registerAndLoginFlow() throws Exception {
        // 1. Register
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setEmail("integration@zorvyn.com");
        registerReq.setPassword("Password@123!");
        registerReq.setFirstName("Integration");
        registerReq.setLastName("Test");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.user.email").value("integration@zorvyn.com"));

        // 2. Login
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("integration@zorvyn.com");
        loginReq.setPassword("Password@123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists());
    }

    @Test
    void loginWithWrongPasswordFails() throws Exception {
        // Register first
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setEmail("wrongpass@zorvyn.com");
        registerReq.setPassword("Password@123!");
        registerReq.setFirstName("Wrong");
        registerReq.setLastName("Pass");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerReq)));

        // Login with bad password
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("wrongpass@zorvyn.com");
        loginReq.setPassword("BadPassword!!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }
}
