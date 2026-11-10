package com.example.projectmanager.project;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.example.projectmanager.common.Timestamps;
import com.example.projectmanager.error.NotFoundException;
import com.example.projectmanager.error.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProjectService {

    static final int MAX_NAME_LENGTH = 100;

    private final ProjectRepository repository;
    private final Clock clock;

    public ProjectService(ProjectRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public Project create(String name, String description) {
        String validName = validateName(name, null);
        Instant now = Timestamps.now(clock);
        Project project = new Project(UUID.randomUUID().toString(), validName, description, now, now);
        repository.insert(project);
        return project;
    }

    @Transactional(readOnly = true)
    public Project get(String id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("project not found"));
    }

    @Transactional(readOnly = true)
    public List<Project> list() {
        return repository.findAll();
    }

    public Project update(String id, ProjectPatch patch) {
        Project existing = get(id);
        if (patch.isEmpty()) {
            throw new ValidationException("no updatable fields provided");
        }
        String name = patch.hasName() ? validateName(patch.name(), id) : existing.name();
        String description = patch.hasDescription() ? patch.description() : existing.description();
        Project updated = new Project(
                id, name, description, existing.createdAt(), Timestamps.nextAfter(existing.updatedAt(), clock));
        repository.update(updated);
        return updated;
    }

    public void delete(String id) {
        if (!repository.deleteById(id)) {
            throw new NotFoundException("project not found");
        }
    }

    private String validateName(String name, String excludeId) {
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isEmpty()) {
            throw new ValidationException("name must not be blank");
        }
        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw new ValidationException("name must be at most " + MAX_NAME_LENGTH + " characters");
        }
        if (repository.nameExists(trimmed, excludeId)) {
            throw new ValidationException("project name already exists");
        }
        return trimmed;
    }
}
