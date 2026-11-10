package com.example.projectmanager.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import com.example.projectmanager.error.NotFoundException;
import com.example.projectmanager.error.ValidationException;
import com.example.projectmanager.project.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-10T12:00:00.000Z");

    @Mock
    TaskRepository repository;

    @Mock
    ProjectService projectService;

    private TaskService service;

    @BeforeEach
    void setUp() {
        service = new TaskService(repository, projectService, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createDefaultsStatusToTodoAndPriorityToMedium() {
        Task task = service.create("p1", titleOnly("Write spec"));

        assertThat(task.status()).isEqualTo(TaskStatus.TODO);
        assertThat(task.priority()).isEqualTo(TaskPriority.MEDIUM);
        verify(repository).insert(task);
    }

    @Test
    void createRejectsBlankTitle() {
        assertThatThrownBy(() -> service.create("p1", titleOnly("   ")))
                .isInstanceOf(ValidationException.class)
                .hasMessage("title must not be blank");
        verify(repository, never()).insert(any());
    }

    @Test
    void createForUnknownProjectThrowsNotFound() {
        when(projectService.get("missing")).thenThrow(new NotFoundException("project not found"));

        assertThatThrownBy(() -> service.create("missing", titleOnly("Write spec")))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("project not found");
        verify(repository, never()).insert(any());
    }

    @Test
    void updateAdvancesUpdatedAtEvenWithinSameMillisecondAndKeepsCreatedAt() {
        Instant createdAt = NOW.minusSeconds(60);
        when(repository.findById("t1")).thenReturn(Optional.of(task(TaskStatus.TODO, createdAt, NOW)));

        Task updated = service.update("t1", statusOnly(TaskStatus.DONE));

        assertThat(updated.createdAt()).isEqualTo(createdAt);
        assertThat(updated.updatedAt()).isEqualTo(NOW.plusMillis(1));
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("allStatusPairs")
    void everyStatusTransitionIsAllowed(TaskStatus from, TaskStatus to) {
        when(repository.findById("t1")).thenReturn(Optional.of(task(from, NOW, NOW)));

        Task updated = service.update("t1", statusOnly(to));

        assertThat(updated.status()).isEqualTo(to);
        verify(repository).update(updated);
    }

    static Stream<Arguments> allStatusPairs() {
        return Arrays.stream(TaskStatus.values())
                .flatMap(from -> Arrays.stream(TaskStatus.values()).map(to -> Arguments.of(from, to)));
    }

    private static TaskInput titleOnly(String title) {
        return new TaskInput(true, title, false, null, false, null, false, null, false, null);
    }

    private static TaskInput statusOnly(TaskStatus status) {
        return new TaskInput(false, null, false, null, true, status, false, null, false, null);
    }

    private static Task task(TaskStatus status, Instant createdAt, Instant updatedAt) {
        return new Task("t1", "p1", "Write spec", null, status, TaskPriority.MEDIUM, null, createdAt, updatedAt);
    }
}
