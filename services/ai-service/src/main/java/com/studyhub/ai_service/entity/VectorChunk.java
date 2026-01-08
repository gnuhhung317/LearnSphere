package com.studyhub.ai_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "items") // Using 'items' as per pgvector-java examples or 'chunks'
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VectorChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_id", nullable = false)
    private String fileId; // Reference to Media Service file ID

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "chunk_index")
    private Integer chunkIndex;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
    
    // We will not map the vector column with JPA directly to avoid complexity
    // We will use JdbcTemplate for inserting/querying the vector column
}
