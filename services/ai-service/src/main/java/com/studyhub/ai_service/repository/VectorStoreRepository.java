package com.studyhub.ai_service.repository;

import com.pgvector.PGvector;
import com.studyhub.ai_service.entity.VectorChunk;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class VectorStoreRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Initializes the database with pgvector extension and schema.
     * Should be called on startup or via Flyway.
     */
    public void initSchema() {
        jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS items (" +
                "id bigserial PRIMARY KEY, " +
                "file_id text, " +
                "room_id bigint, " +
                "content text, " +
                "chunk_index integer, " +
                "created_at timestamp, " +
                "embedding vector(768))"); // 768 is embedding dimension for text-embedding-004
    }

    public void save(VectorChunk chunk, List<Double> embedding, Long roomId) {
        String sql = "INSERT INTO items (file_id, room_id, content, chunk_index, created_at, embedding) VALUES (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, chunk.getFileId());
            ps.setLong(2, roomId);
            ps.setString(3, chunk.getContent());
            ps.setInt(4, chunk.getChunkIndex());
            ps.setTimestamp(5, java.sql.Timestamp.from(java.time.Instant.now()));
            ps.setObject(6, new PGvector(embedding));
            return ps;
        });
    }

    public List<VectorChunk> findSimilarByRoom(Long roomId, List<Double> embedding, int limit) {
        String sql = "SELECT file_id, content, chunk_index FROM items " +
                "WHERE room_id = ? " +
                "ORDER BY embedding <=> ?::vector " +
                "LIMIT ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> VectorChunk.builder()
                .fileId(rs.getString("file_id"))
                .content(rs.getString("content"))
                .chunkIndex(rs.getInt("chunk_index"))
                .build(),
                roomId, new PGvector(embedding), limit);
    }
}
