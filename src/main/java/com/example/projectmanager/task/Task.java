package com.example.projectmanager.task;

import java.time.Instant;
import java.time.LocalDate;

public record Task(
        String id,
        String projectId,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate,
        Instant createdAt,
        Instant updatedAt) {
}
