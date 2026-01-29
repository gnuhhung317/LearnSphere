package com.studyhub.learningservice.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "resources")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceType type;

    // URL for links, or File ID/Path for files
    @Column(columnDefinition = "TEXT")
    private String url;

    // Optional: Stores AI-generated summary or user notes
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(name = "is_completed")
    private boolean isCompleted;

    @Column(name = "status")
    private String status; // PENDING, PROCESSING, READY, FAILED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Quiz> quizzes = new ArrayList<>();

    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FlashcardDeck> flashcardDecks = new ArrayList<>();
}
