package com.example.aitracker.project;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<Project> createProject(
            @RequestParam String name,
            @RequestParam UUID organizationId
    ) {
        Project project = projectService.createProject(name, organizationId);
        return ResponseEntity.status(HttpStatus.CREATED).body(project);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @GetMapping("/organization/{organizationId}")
    public ResponseEntity<List<Project>> getProjectsByOrganization(@PathVariable UUID organizationId) {
        return ResponseEntity.ok(projectService.getProjectsByOrganization(organizationId));
    }
}