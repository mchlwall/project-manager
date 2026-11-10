package com.example.projectmanager.task;

import java.net.URI;
import java.util.List;

import com.example.projectmanager.common.JsonFields;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @PostMapping("/api/projects/{projectId}/tasks")
    public ResponseEntity<Task> create(@PathVariable String projectId, @RequestBody JsonNode body) {
        Task task = service.create(projectId, TaskInput.fromJson(body));
        return ResponseEntity.created(URI.create("/api/tasks/" + task.id())).body(task);
    }

    @GetMapping("/api/projects/{projectId}/tasks")
    public List<Task> list(
            @PathVariable String projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority) {
        return service.list(
                projectId,
                isBlank(status) ? null : JsonFields.parseEnum(TaskStatus.class, status, "status"),
                isBlank(priority) ? null : JsonFields.parseEnum(TaskPriority.class, priority, "priority"));
    }

    @GetMapping("/api/tasks/{id}")
    public Task get(@PathVariable String id) {
        return service.get(id);
    }

    @PatchMapping("/api/tasks/{id}")
    public Task update(@PathVariable String id, @RequestBody JsonNode body) {
        return service.update(id, TaskInput.fromJson(body));
    }

    @DeleteMapping("/api/tasks/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
