package br.com.mentorhub.studyplans.api.dto;

import br.com.mentorhub.studyplans.domain.StudyTaskStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStudyTaskProgressRequest(
        @NotNull(message = "Informe o status da atividade")
        StudyTaskStatus status
) {
}
