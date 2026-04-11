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
                "created_at timestamp)");

        // Ensure embedding column exists
        jdbcTemplate.execute("ALTER TABLE items ADD COLUMN IF NOT EXISTS embedding vector(768)");
        // Ensure learning_space_id column exists
        jdbcTemplate.execute("ALTER TABLE items ADD COLUMN IF NOT EXISTS learning_space_id bigint");
    }

    public void save(VectorChunk chunk, List<Double> embedding, Long roomId) {
        save(chunk, embedding, roomId, null);
    }

    public void save(VectorChunk chunk, List<Double> embedding, Long roomId, Long learningSpaceId) {
        String sql = "INSERT INTO items (file_id, room_id, learning_space_id, content, chunk_index, created_at, embedding) VALUES (?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, chunk.getFileId());
            if (roomId != null)
                ps.setLong(2, roomId);
            else
                ps.setNull(2, java.sql.Types.BIGINT);
            if (learningSpaceId != null)
                ps.setLong(3, learningSpaceId);
            else
                ps.setNull(3, java.sql.Types.BIGINT);
            ps.setString(4, chunk.getContent());
            ps.setInt(5, chunk.getChunkIndex());
            ps.setTimestamp(6, java.sql.Timestamp.from(java.time.Instant.now()));
            ps.setObject(7, new PGvector(embedding));
            return ps;
        });
    }

    public void save(VectorChunk chunk, float[] embedding, Long roomId) {
        save(chunk, embedding, roomId, null);
    }

    public void save(VectorChunk chunk, float[] embedding, Long roomId, Long learningSpaceId) {
        String sql = "INSERT INTO items (file_id, room_id, learning_space_id, content, chunk_index, created_at, embedding) VALUES (?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, chunk.getFileId());
            if (roomId != null)
                ps.setLong(2, roomId);
            else
                ps.setNull(2, java.sql.Types.BIGINT);
            if (learningSpaceId != null)
                ps.setLong(3, learningSpaceId);
            else
                ps.setNull(3, java.sql.Types.BIGINT);
            ps.setString(4, chunk.getContent());
            ps.setInt(5, chunk.getChunkIndex());
            ps.setTimestamp(6, java.sql.Timestamp.from(java.time.Instant.now()));
            ps.setObject(7, new PGvector(embedding));
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

    public List<VectorChunk> findSimilarByRoom(Long roomId, float[] embedding, int limit) {
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

    public List<VectorChunk> findSimilarByLearningSpace(Long learningSpaceId, float[] embedding, int limit) {
        String sql = "SELECT file_id, content, chunk_index FROM items " +
                "WHERE learning_space_id = ? " +
                "ORDER BY embedding <=> ?::vector " +
                "LIMIT ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> VectorChunk.builder()
                .fileId(rs.getString("file_id"))
                .content(rs.getString("content"))
                .chunkIndex(rs.getInt("chunk_index"))
                .build(),
                learningSpaceId, new PGvector(embedding), limit);
    }

    public List<VectorChunk> findByFileIdOrderByChunkIndex(String fileId) {
        String sql = "SELECT file_id, content, chunk_index FROM items " +
                "WHERE file_id = ? " +
                "ORDER BY chunk_index ASC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> VectorChunk.builder()
                .fileId(rs.getString("file_id"))
                .content(rs.getString("content"))
                .chunkIndex(rs.getInt("chunk_index"))
                .build(),
                fileId);
    }

    public List<VectorChunk> findSimilarByFileId(String fileId, float[] embedding, int limit) {
        String sql = "SELECT file_id, content, chunk_index FROM items " +
                "WHERE file_id = ? " +
                "ORDER BY embedding <=> ?::vector " +
                "LIMIT ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> VectorChunk.builder()
                .fileId(rs.getString("file_id"))
                .content(rs.getString("content"))
                .chunkIndex(rs.getInt("chunk_index"))
                .build(),
                fileId, new PGvector(embedding), limit);
    }

    public void deleteByFileIdAndLearningSpaceId(String fileId, Long learningSpaceId) {
        String sql = "DELETE FROM items WHERE file_id = ? AND learning_space_id = ?";
        jdbcTemplate.update(sql, fileId, learningSpaceId);
    }

    public void deleteByLearningSpaceId(Long learningSpaceId) {
        String sql = "DELETE FROM items WHERE learning_space_id = ?";
        jdbcTemplate.update(sql, learningSpaceId);
    }

    public List<VectorChunk> findRandomChunksByLearningSpace(Long learningSpaceId, int limit) {
        String sql = "SELECT file_id, content, chunk_index FROM items " +
                "WHERE learning_space_id = ? " +
                "ORDER BY RANDOM() " +
                "LIMIT ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> VectorChunk.builder()
                .fileId(rs.getString("file_id"))
                .content(rs.getString("content"))
                .chunkIndex(rs.getInt("chunk_index"))
                .build(),
                learningSpaceId, limit);
    }
}
