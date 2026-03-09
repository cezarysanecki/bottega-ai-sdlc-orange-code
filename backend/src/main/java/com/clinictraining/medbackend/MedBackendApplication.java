package com.clinictraining.medbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.clinictraining")
@EntityScan("com.clinictraining")
@EnableJpaRepositories("com.clinictraining")
@EnableScheduling
public class MedBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedBackendApplication.class, args);
    }
}
