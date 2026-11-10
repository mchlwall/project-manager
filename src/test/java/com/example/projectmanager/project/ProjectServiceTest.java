package com.example.projectmanager.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import com.example.projectmanager.error.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-10T12:00:00.000Z");

    @Mock
    ProjectRepository repository;

    private ProjectService service;

    @BeforeEach
    void setUp() {
        service = new ProjectService(repository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createTrimsNameAndSetsTimestampsFromClock() {
        Project project = service.create("  Alpha  ", "First");

        assertThat(project.name()).isEqualTo("Alpha");
        assertThat(project.createdAt()).isEqualTo(NOW);
        assertThat(project.updatedAt()).isEqualTo(NOW);
        verify(repository).insert(project);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t\n"})
    void createRejectsBlankName(String name) {
        assertThatThrownBy(() -> service.create(name, null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("name must not be blank");
        verify(repository, never()).insert(any());
    }

    @Test
    void createAcceptsNameAtMaxLengthAndRejectsOneOver() {
        assertThat(service.create("a".repeat(100), null).name()).hasSize(100);

        assertThatThrownBy(() -> service.create("a".repeat(101), null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("name must be at most 100 characters");
    }

    @Test
    void createRejectsDuplicateName() {
        when(repository.nameExists("Alpha", null)).thenReturn(true);

        assertThatThrownBy(() -> service.create(" Alpha ", null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("project name already exists");
        verify(repository, never()).insert(any());
    }

    @Test
    void renameExcludesProjectItselfFromDuplicateCheck() {
        Project existing = new Project("p1", "alpha", null, NOW, NOW);
        when(repository.findById("p1")).thenReturn(Optional.of(existing));

        Project updated = service.update("p1", new ProjectPatch(true, "Alpha", false, null));

        assertThat(updated.name()).isEqualTo("Alpha");
        verify(repository).nameExists("Alpha", "p1");
    }

    @Test
    void updateRejectsEmptyPatch() {
        when(repository.findById("p1")).thenReturn(Optional.of(new Project("p1", "Alpha", null, NOW, NOW)));

        assertThatThrownBy(() -> service.update("p1", new ProjectPatch(false, null, false, null)))
                .isInstanceOf(ValidationException.class)
                .hasMessage("no updatable fields provided");
        verify(repository, never()).update(any());
    }
}
