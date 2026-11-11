package com.example.projectmanager.apitests.config;

public final class Endpoints {

    public static final String HEALTH = "/health";
    public static final String PROJECTS = "/api/projects";
    public static final String PROJECT_BY_ID = "/api/projects/{id}";
    public static final String PROJECT_TASKS = "/api/projects/{id}/tasks";
    public static final String TASK_BY_ID = "/api/tasks/{id}";

    private Endpoints() {}
}
