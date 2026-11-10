package com.example.projectmanager.task;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.example.projectmanager.common.Timestamps;
import com.example.projectmanager.error.NotFoundException;
import com.example.projectmanager.error.ValidationException;
import com.example.projectmanager.project.ProjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TaskService {

    static final int MAX_TITLE_LENGTH = 100;

    private final TaskRepository repository;
    private final ProjectService projectService;
    private final Clock clock;

    public TaskService(TaskRepository repository, ProjectService projectService, Clock clock) {
        this.repository = repository;
        this.projectService = projectService;
        this.clock = clock;
    }

    public Task create(String projectId, TaskInput input) {
        projectService.get(projectId);
        String title = validateTitle(input.title());
        Instant now = Timestamps.now(clock);
        Task task = new Task(
                UUID.randomUUID().toString(),
                projectId,
                title,
                input.description(),
                input.hasStatus() ? input.status() : TaskStatus.TODO,
                input.hasPriority() ? input.priority() : TaskPriority.MEDIUM,
                input.dueDate(),
                now,
                now);
        repository.insert(task);
        return task;
    }

    @Transactional(readOnly = true)
    public List<Task> list(String projectId, TaskStatus status, TaskPriority priority) {
        projectService.get(projectId);
        return repository.findByProject(projectId, status, priority);
    }

    @Transactional(readOnly = true)
    public Task get(String id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("task not found"));
    }

    public Task update(String id, TaskInput input) {
        Task existing = get(id);
        if (input.isEmpty()) {
            throw new ValidationException("no updatable fields provided");
        }
        Task updated = new Task(
                id,
                existing.projectId(),
                input.hasTitle() ? validateTitle(input.title()) : existing.title(),
                input.hasDescription() ? input.description() : existing.description(),
                input.hasStatus() ? input.status() : existing.status(),
                input.hasPriority() ? input.priority() : existing.priority(),
                input.hasDueDate() ? input.dueDate() : existing.dueDate(),
                existing.createdAt(),
                Timestamps.nextAfter(existing.updatedAt(), clock));
        repository.update(updated);
        return updated;
    }

    public void delete(String id) {
        if (!repository.deleteById(id)) {
            throw new NotFoundException("task not found");
        }
    }

    private static String validateTitle(String title) {
        String trimmed = title == null ? "" : title.trim();
        if (trimmed.isEmpty()) {
            throw new ValidationException("title must not be blank");
        }
        if (trimmed.length() > MAX_TITLE_LENGTH) {
            throw new ValidationException("title must be at most " + MAX_TITLE_LENGTH + " characters");
        }
        return trimmed;
    }
}
