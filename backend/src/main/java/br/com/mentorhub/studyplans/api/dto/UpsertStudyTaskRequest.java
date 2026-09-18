package br.com.mentorhub.studyplans.api.dto;

import br.com.mentorhub.studyplans.domain.StudyTaskType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpsertStudyTaskRequest(
        @NotBlank(message = "Título da atividade é obrigatório")
        @Size(max = 180, message = "Título deve ter no máximo 180 caracteres")
        String title,
        @Size(max = 2000, message = "Descrição deve ter no máximo 2000 caracteres")
        String description,
        @NotNull(message = "Informe o tipo da atividade")
        StudyTaskType taskType,
        @Min(value = 1, message = "A ordem da atividade deve ser pelo menos 1")
        Integer orderNumber,
        LocalDate dueDate,
        Boolean required,
        @Size(max = 180, message = "Título do material deve ter no máximo 180 caracteres")
        String resourceTitle,
        @Size(max = 500, message = "Link deve ter no máximo 500 caracteres")
        String resourceUrl
) {
}
