package com.studyhub.ai_service.service.etl;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TextChunker {

    private static final int CHUNK_SIZE = 1000;
    private static final int OVERLAP = 100;

    public List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }

        // Clean text: remove excessive newlines/spaces
        String cleanText = text.replaceAll("\\s+", " ").trim();
        int length = cleanText.length();

        if (length <= CHUNK_SIZE) {
            chunks.add(cleanText);
            return chunks;
        }

        int start = 0;
        while (start < length) {
            int end = Math.min(start + CHUNK_SIZE, length);
            chunks.add(cleanText.substring(start, end));
            
            // Move start forward by CHUNK_SIZE - OVERLAP, but ensure we don't regress or stick
            start += (CHUNK_SIZE - OVERLAP);
        }

        return chunks;
    }
}
