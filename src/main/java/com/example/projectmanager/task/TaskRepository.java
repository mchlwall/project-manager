package com.example.projectmanager.task;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.example.projectmanager.common.Timestamps;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class TaskRepository {

    private static final RowMapper<Task> ROW_MAPPER = (rs, rowNum) -> {
        String dueDate = rs.getString("due_date");
        return new Task(
                rs.getString("id"),
                rs.getString("project_id"),
                rs.getString("title"),
                rs.getString("description"),
                TaskStatus.valueOf(rs.getString("status")),
                TaskPriority.valueOf(rs.getString("priority")),
                dueDate == null ? null : LocalDate.parse(dueDate),
                Timestamps.parse(rs.getString("created_at")),
                Timestamps.parse(rs.getString("updated_at")));
    };

    private final JdbcTemplate jdbc;

    public TaskRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(Task task) {
        jdbc.update(
                """
                INSERT INTO tasks (id, project_id, title, description, status, priority, due_date, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                task.id(),
                task.projectId(),
                task.title(),
                task.description(),
                task.status().name(),
                task.priority().name(),
                dueDateOrNull(task.dueDate()),
                Timestamps.format(task.createdAt()),
                Timestamps.format(task.updatedAt()));
    }

    public Optional<Task> findById(String id) {
        return jdbc.query("SELECT * FROM tasks WHERE id = ?", ROW_MAPPER, id).stream().findFirst();
    }

    /** Tasks for a project, oldest first; null filters are not applied. */
    public List<Task> findByProject(String projectId, TaskStatus status, TaskPriority priority) {
        StringBuilder sql = new StringBuilder("SELECT * FROM tasks WHERE project_id = ?");
        List<Object> args = new ArrayList<>(List.of(projectId));
        if (status != null) {
            sql.append(" AND status = ?");
            args.add(status.name());
        }
        if (priority != null) {
            sql.append(" AND priority = ?");
            args.add(priority.name());
        }
        sql.append(" ORDER BY created_at, id");
        return jdbc.query(sql.toString(), ROW_MAPPER, args.toArray());
    }

    public void update(Task task) {
        jdbc.update(
                """
                UPDATE tasks
                SET title = ?, description = ?, status = ?, priority = ?, due_date = ?, updated_at = ?
                WHERE id = ?
                """,
                task.title(),
                task.description(),
                task.status().name(),
                task.priority().name(),
                dueDateOrNull(task.dueDate()),
                Timestamps.format(task.updatedAt()),
                task.id());
    }

    public boolean deleteById(String id) {
        return jdbc.update("DELETE FROM tasks WHERE id = ?", id) > 0;
    }

    private static String dueDateOrNull(LocalDate dueDate) {
        return dueDate == null ? null : dueDate.toString();
    }
}
