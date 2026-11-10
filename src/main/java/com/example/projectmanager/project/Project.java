package com.example.projectmanager.project;

import java.time.Instant;

public record Project(String id, String name, String description, Instant createdAt, Instant updatedAt) {
}
