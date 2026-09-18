package br.com.mentorhub.studyplans.domain;

import br.com.mentorhub.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudyPlanTest {

    @Test
    void shouldCreatePlanWithTitle() {
        StudyPlan plan = StudyPlan.create(UUID.randomUUID(), "  Arquitetura de Software  ", "Fundamentos");
        assertEquals("Arquitetura de Software", plan.getTitle());
        assertEquals("Fundamentos", plan.getDescription());
    }

    @Test
    void shouldRejectBlankTitle() {
        BusinessException error = assertThrows(
                BusinessException.class,
                () -> StudyPlan.create(UUID.randomUUID(), "  ", null)
        );
        assertEquals("INVALID_STUDY_PLAN", error.getCode());
    }
}

class StudyTaskTest {

    @Test
    void shouldCreateRequiredTaskWithMaterial() {
        StudyTask task = StudyTask.create(
                UUID.randomUUID(),
                "Assistir aula sobre SOLID",
                "Assistir antes da próxima sessão.",
                StudyTaskType.VIDEO,
                1,
                LocalDate.of(2026, 8, 30),
                true,
                "Introdução ao SOLID",
                "https://example.com/solid"
        );
        assertTrue(task.isRequired());
        assertEquals(StudyTaskType.VIDEO, task.getTaskType());
        assertEquals("https://example.com/solid", task.getResourceUrl());
    }

    @Test
    void shouldRejectInvalidLink() {
        BusinessException error = assertThrows(
                BusinessException.class,
                () -> StudyTask.create(
                        UUID.randomUUID(),
                        "Ler artigo",
                        null,
                        StudyTaskType.ARTICLE,
                        1,
                        null,
                        true,
                        null,
                        "ftp://files"
                )
        );
        assertEquals("INVALID_STUDY_TASK", error.getCode());
    }
}

class StudyTaskProgressTest {

    @Test
    void shouldCompleteAndReopen() {
        StudyTaskProgress progress = StudyTaskProgress.create(UUID.randomUUID(), UUID.randomUUID());
        assertEquals(StudyTaskStatus.PENDING, progress.getStatus());

        StudyTaskProgress completed = progress.changeStatus(StudyTaskStatus.COMPLETED);
        assertEquals(StudyTaskStatus.COMPLETED, completed.getStatus());
        assertTrue(completed.isCompleted());

        StudyTaskProgress reopened = completed.changeStatus(StudyTaskStatus.PENDING);
        assertEquals(StudyTaskStatus.PENDING, reopened.getStatus());
    }
}
