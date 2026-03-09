package com.clinictraining.integration;

import com.clinictraining.medbackend.MedBackendApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests using Testcontainers with PostgreSQL.
 *
 * <p>This class provides:
 * <ul>
 *   <li>Singleton PostgreSQL container shared across all integration tests (performance optimization)</li>
 *   <li>Spring Boot test context with full application configuration</li>
 *   <li>Test profile activation for test-specific configuration</li>
 *   <li>Automatic transaction rollback after each test (@Transactional)</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>
 * class MyServiceIT extends AbstractIntegrationTest {
 *     // Your tests here - no need to configure Testcontainers
 * }
 * </pre>
 *
 * <p><strong>Note:</strong> The PostgreSQL container is started once and reused across all test classes
 * for better performance. The container is automatically stopped when the JVM exits.
 */
@SpringBootTest(classes = MedBackendApplication.class)
@Testcontainers
@ActiveProfiles("test")
@Transactional
public abstract class AbstractIntegrationTest {

    /**
     * Shared PostgreSQL container instance.
     * Static container is started once and shared across all test classes for performance.
     */
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    static {
        // Start container once at class loading time
        postgres.start();
    }

    /**
     * Dynamically configures Spring datasource properties to point to the Testcontainers database.
     * This method is called before the Spring context is loaded.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
