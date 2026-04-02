package com.zorvyn.fintech.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zorvyn.fintech.dto.request.CreateTransactionRequest;
import com.zorvyn.fintech.entity.Account;
import com.zorvyn.fintech.entity.Role;
import com.zorvyn.fintech.entity.User;
import com.zorvyn.fintech.repository.AccountRepository;
import com.zorvyn.fintech.repository.RoleRepository;
import com.zorvyn.fintech.repository.TransactionRepository;
import com.zorvyn.fintech.repository.UserRepository;
import com.zorvyn.fintech.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class TransactionIntegrationTest {

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

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private User testManager;
    private Account fromAccount;
    private Account toAccount;
    private String userToken;
    private String managerToken;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Roles
        Role userRole = new Role();
        userRole.setName("user");
        userRole.setPermissions(List.of("read", "write"));
        userRole = roleRepository.save(userRole);

        Role managerRole = new Role();
        managerRole.setName("finance_manager");
        managerRole.setPermissions(List.of("read", "write", "approve"));
        managerRole = roleRepository.save(managerRole);

        // Users
        testUser = new User();
        testUser.setEmail("txuser@zorvyn.com");
        testUser.setPasswordHash(passwordEncoder.encode("Pass@123"));
        testUser.setRole(userRole);
        testUser = userRepository.save(testUser);

        testManager = new User();
        testManager.setEmail("manager@zorvyn.com");
        testManager.setPasswordHash(passwordEncoder.encode("Pass@123"));
        testManager.setRole(managerRole);
        testManager = userRepository.save(testManager);

        // Accounts
        fromAccount = new Account();
        fromAccount.setUser(testUser);
        fromAccount.setAccountNumber("SRC-1234");
        fromAccount.setBalance(new BigDecimal("1000.00"));
        fromAccount = accountRepository.save(fromAccount);

        toAccount = new Account();
        toAccount.setUser(testUser);
        toAccount.setAccountNumber("DST-5678");
        toAccount.setBalance(new BigDecimal("0.00"));
        toAccount = accountRepository.save(toAccount);

        // Tokens (Mocked Session validation bypassed for integration tests or mocked)
        // Note: In a real environment, we'd need to mock the SessionService or use a Test config for Redis
        userToken = jwtTokenProvider.generateAccessToken(testUser.getId(), testUser.getEmail(), userRole.getName(), userRole.getPermissions());
        managerToken = jwtTokenProvider.generateAccessToken(testManager.getId(), testManager.getEmail(), managerRole.getName(), managerRole.getPermissions());
    }

    // Example test - may fail if we don't mock Redis check in JwtAuthenticationFilter for testing
    // To make it fully work, we might need a @MockBean for SessionService in this test context
}
