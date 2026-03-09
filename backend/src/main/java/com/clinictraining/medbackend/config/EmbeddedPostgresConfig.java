package com.clinictraining.medbackend.config;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

/**
 * Configuration for embedded PostgreSQL database in demo mode.
 * This allows running the application without Docker or an external database.
 */
@Configuration
@Profile("demo")
public class EmbeddedPostgresConfig {

    private static final Logger log = LoggerFactory.getLogger(EmbeddedPostgresConfig.class);

    @Bean(destroyMethod = "close")
    public EmbeddedPostgres embeddedPostgres() throws Exception {
        log.info("========================================");
        log.info("Starting embedded PostgreSQL database...");
        log.info("(First run downloads PostgreSQL binary)");
        log.info("========================================");

        EmbeddedPostgres postgres = EmbeddedPostgres.builder()
                .start();

        log.info("Embedded PostgreSQL started on port {}", postgres.getPort());
        return postgres;
    }

    @Bean
    @Primary
    public DataSource dataSource(EmbeddedPostgres embeddedPostgres) {
        log.info("Configuring datasource for embedded PostgreSQL");
        return embeddedPostgres.getPostgresDatabase();
    }
}
