package com.studyhub.ai_service.config;

import com.studyhub.ai_service.repository.VectorStoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SchemaInitializer implements CommandLineRunner {

    private final VectorStoreRepository vectorStoreRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing database schema...");
        try {
            vectorStoreRepository.initSchema();
            log.info("Database schema initialized successfully.");
        } catch (Exception e) {
            log.error("Failed to initialize database schema", e);
        }
    }
}
