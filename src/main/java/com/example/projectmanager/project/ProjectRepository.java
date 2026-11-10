package com.example.projectmanager.project;

import java.util.List;
import java.util.Optional;

import com.example.projectmanager.common.Timestamps;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ProjectRepository {

    private static final RowMapper<Project> ROW_MAPPER = (rs, rowNum) -> new Project(
            rs.getString("id"),
            rs.getString("name"),
            rs.getString("description"),
            Timestamps.parse(rs.getString("created_at")),
            Timestamps.parse(rs.getString("updated_at")));

    private final JdbcTemplate jdbc;

    public ProjectRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(Project project) {
        jdbc.update(
                "INSERT INTO projects (id, name, description, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
                project.id(),
                project.name(),
                project.description(),
                Timestamps.format(project.createdAt()),
                Timestamps.format(project.updatedAt()));
    }

    public Optional<Project> findById(String id) {
        return jdbc.query("SELECT * FROM projects WHERE id = ?", ROW_MAPPER, id).stream().findFirst();
    }

    public List<Project> findAll() {
        return jdbc.query("SELECT * FROM projects ORDER BY created_at, id", ROW_MAPPER);
    }

    /** Case-insensitive name check, ignoring the project with {@code excludeId} (may be null). */
    public boolean nameExists(String name, String excludeId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM projects WHERE name = ? COLLATE NOCASE AND (? IS NULL OR id <> ?)",
                Integer.class,
                name, excludeId, excludeId);
        return count != null && count > 0;
    }

    public void update(Project project) {
        jdbc.update(
                "UPDATE projects SET name = ?, description = ?, updated_at = ? WHERE id = ?",
                project.name(),
                project.description(),
                Timestamps.format(project.updatedAt()),
                project.id());
    }

    public boolean deleteById(String id) {
        return jdbc.update("DELETE FROM projects WHERE id = ?", id) > 0;
    }
}
