package br.com.mentorhub.studyplans.api;

import br.com.mentorhub.studyplans.api.dto.StudyPlanResponse;
import br.com.mentorhub.studyplans.api.dto.UpdateStudyTaskProgressRequest;
import br.com.mentorhub.studyplans.api.dto.UpsertStudyPlanRequest;
import br.com.mentorhub.studyplans.api.dto.UpsertStudyTaskRequest;
import br.com.mentorhub.studyplans.application.CreateStudyTaskService;
import br.com.mentorhub.studyplans.application.DeleteStudyTaskService;
import br.com.mentorhub.studyplans.application.GetStudyPlanService;
import br.com.mentorhub.studyplans.application.UpdateStudyTaskProgressService;
import br.com.mentorhub.studyplans.application.UpdateStudyTaskService;
import br.com.mentorhub.studyplans.application.UpsertStudyPlanService;
import br.com.mentorhub.shared.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mentorships/relationships/{mentorshipId}/study-plan")
public class StudyPlanController {

    private final GetStudyPlanService getStudyPlanService;
    private final UpsertStudyPlanService upsertStudyPlanService;
    private final CreateStudyTaskService createStudyTaskService;
    private final UpdateStudyTaskService updateStudyTaskService;
    private final DeleteStudyTaskService deleteStudyTaskService;
    private final UpdateStudyTaskProgressService updateStudyTaskProgressService;

    public StudyPlanController(
            GetStudyPlanService getStudyPlanService,
            UpsertStudyPlanService upsertStudyPlanService,
            CreateStudyTaskService createStudyTaskService,
            UpdateStudyTaskService updateStudyTaskService,
            DeleteStudyTaskService deleteStudyTaskService,
            UpdateStudyTaskProgressService updateStudyTaskProgressService
    ) {
        this.getStudyPlanService = getStudyPlanService;
        this.upsertStudyPlanService = upsertStudyPlanService;
        this.createStudyTaskService = createStudyTaskService;
        this.updateStudyTaskService = updateStudyTaskService;
        this.deleteStudyTaskService = deleteStudyTaskService;
        this.updateStudyTaskProgressService = updateStudyTaskProgressService;
    }

    @GetMapping
    public ResponseEntity<StudyPlanResponse> get(@PathVariable UUID mentorshipId) {
        return getStudyPlanService.execute(SecurityUtils.requireCurrentUserId(), mentorshipId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PutMapping
    public ResponseEntity<StudyPlanResponse> upsert(
            @PathVariable UUID mentorshipId,
            @Valid @RequestBody UpsertStudyPlanRequest request
    ) {
        return ResponseEntity.ok(upsertStudyPlanService.execute(
                SecurityUtils.requireCurrentUserId(),
                mentorshipId,
                request
        ));
    }

    @PostMapping("/tasks")
    public ResponseEntity<StudyPlanResponse> createTask(
            @PathVariable UUID mentorshipId,
            @Valid @RequestBody UpsertStudyTaskRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createStudyTaskService.execute(
                SecurityUtils.requireCurrentUserId(),
                mentorshipId,
                request
        ));
    }

    @PatchMapping("/tasks/{taskId}")
    public ResponseEntity<StudyPlanResponse> updateTask(
            @PathVariable UUID mentorshipId,
            @PathVariable UUID taskId,
            @Valid @RequestBody UpsertStudyTaskRequest request
    ) {
        return ResponseEntity.ok(updateStudyTaskService.execute(
                SecurityUtils.requireCurrentUserId(),
                mentorshipId,
                taskId,
                request
        ));
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<StudyPlanResponse> deleteTask(
            @PathVariable UUID mentorshipId,
            @PathVariable UUID taskId
    ) {
        return ResponseEntity.ok(deleteStudyTaskService.execute(
                SecurityUtils.requireCurrentUserId(),
                mentorshipId,
                taskId
        ));
    }

    @PatchMapping("/tasks/{taskId}/progress")
    public ResponseEntity<StudyPlanResponse> updateProgress(
            @PathVariable UUID mentorshipId,
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateStudyTaskProgressRequest request
    ) {
        return ResponseEntity.ok(updateStudyTaskProgressService.execute(
                SecurityUtils.requireCurrentUserId(),
                mentorshipId,
                taskId,
                request
        ));
    }
}
