package com.studyhub.ai_service.config;

import com.studyhub.ai_service.repository.VectorStoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SchemaInitializer implements CommandLineRunner {

    private final VectorStoreRepository repository;

    @Override
    public void run(String... args) throws Exception {
        repository.initSchema();
    }
}
