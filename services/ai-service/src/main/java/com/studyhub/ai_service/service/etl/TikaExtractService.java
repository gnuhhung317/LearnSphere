package com.studyhub.ai_service.service.etl;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@Slf4j
public class TikaExtractService {

    private final Tika tika = new Tika();

    public String extractText(InputStream stream) {
        try {
            return tika.parseToString(stream);
        } catch (Exception e) {
            log.error("Failed to extract text using Tika", e);
            throw new RuntimeException("Text extraction failed", e);
        }
    }
}
