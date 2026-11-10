package com.example.projectmanager.task;

import java.time.LocalDate;

import com.example.projectmanager.common.JsonFields;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Task fields from a create or update body. The {@code has*} flags distinguish an omitted field
 * from one sent as null, so {@code "dueDate": null} clears the due date on update.
 */
public record TaskInput(
        boolean hasTitle, String title,
        boolean hasDescription, String description,
        boolean hasStatus, TaskStatus status,
        boolean hasPriority, TaskPriority priority,
        boolean hasDueDate, LocalDate dueDate) {

    public static TaskInput fromJson(JsonNode body) {
        JsonFields.requireObject(body);
        return new TaskInput(
                body.has("title"), JsonFields.text(body, "title"),
                body.has("description"), JsonFields.text(body, "description"),
                body.has("status"), JsonFields.enumValue(body, "status", TaskStatus.class),
                body.has("priority"), JsonFields.enumValue(body, "priority", TaskPriority.class),
                body.has("dueDate"), JsonFields.date(body, "dueDate"));
    }

    public boolean isEmpty() {
        return !hasTitle && !hasDescription && !hasStatus && !hasPriority && !hasDueDate;
    }
}
