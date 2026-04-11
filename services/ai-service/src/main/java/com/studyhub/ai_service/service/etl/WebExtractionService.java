package com.studyhub.ai_service.service.etl;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class WebExtractionService {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    public String extractContent(String url) {
        if (isYouTubeUrl(url)) {
            return extractYouTubeContent(url);
        } else {
            return extractGeneralWebContent(url);
        }
    }

    private boolean isYouTubeUrl(String url) {
        return url.contains("youtube.com") || url.contains("youtu.be");
    }

    private String extractYouTubeContent(String url) {
        try {
            log.info("Extracting YouTube metadata and transcript for: {}", url);
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .get();

            String title = doc.select("meta[property=og:title]").attr("content");
            String description = doc.select("meta[property=og:description]").attr("content");

            if (title.isEmpty())
                title = doc.title();

            StringBuilder sb = new StringBuilder();
            sb.append("YouTube Video: ").append(title).append("\n\n");
            sb.append("Description:\n").append(description).append("\n\n");

            // Try to extract transcript
            String videoId = extractVideoId(url);
            if (videoId != null) {
                try {
                    io.github.thoroldvix.api.YoutubeTranscriptApi transcriptApi = io.github.thoroldvix.api.TranscriptApiFactory
                            .createDefault();
                    io.github.thoroldvix.api.TranscriptList transcriptList = transcriptApi.listTranscripts(videoId);

                    // Try to get English transcript (prefer manual, then auto-generated)
                    io.github.thoroldvix.api.Transcript transcript = null;
                    try {
                        transcript = transcriptList.findTranscript("en");
                    } catch (Exception e) {
                        // If no direct English, try to find any available and use it
                        log.warn("No direct English transcript found for {}, trying any available.", videoId);
                        try {
                            transcript = transcriptList.findTranscript("vi");
                        } catch (Exception e2) {
                            log.warn("No English transcript found for {}, trying any available.", videoId);
                        }
                    }

                    if (transcript != null) {
                        sb.append("Transcript:\n");
                        transcript.fetch().getContent().forEach(fragment -> sb.append(fragment.getText()).append(" "));
                        sb.append("\n\n");
                    }
                } catch (Exception e) {
                    log.warn("Could not retrieve transcript for YouTube video {}: {}", videoId, e.getMessage());
                    sb.append("[Transcript not available or disabled for this video]\n\n");
                }
            }

            return sb.toString();
        } catch (IOException e) {
            log.error("Failed to extract YouTube content: {}", url, e);
            return "Failed to extract YouTube content: " + e.getMessage();
        }
    }

    private String extractVideoId(String url) {
        String pattern = "(?<=watch\\?v=|/videos/|embed/|youtu.be/|/v/|/e/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#&?\\n]*";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private String extractGeneralWebContent(String url) {
        try {
            log.info("Extracting web content for: {}", url);
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(10000)
                    .get();

            // Remove scripts, styles, and other non-content elements
            doc.select("script, style, nav, footer, header, aside").remove();

            String title = doc.title();
            Element body = doc.body();

            // Try to find the main content area if possible
            Element mainContent = body.select("main, article, .content, #content, .post, .article").first();
            String text = (mainContent != null) ? mainContent.text() : body.text();

            StringBuilder sb = new StringBuilder();
            sb.append("Web Page: ").append(title).append("\n");
            sb.append("URL: ").append(url).append("\n\n");
            sb.append("Content:\n").append(text);

            return sb.toString();
        } catch (IOException e) {
            log.error("Failed to extract web content: {}", url, e);
            return "Failed to extract web content: " + e.getMessage();
        }
    }
}
