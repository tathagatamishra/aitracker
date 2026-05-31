package com.example.aitracker.project;

import com.example.aitracker.organization.Organization;
import com.example.aitracker.organization.OrganizationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final OrganizationRepository organizationRepository;

    public ProjectService(ProjectRepository projectRepository, OrganizationRepository organizationRepository) {
        this.projectRepository = projectRepository;
        this.organizationRepository = organizationRepository;
    }

    public Project createProject(String name, UUID organizationId) {
        if (projectRepository.existsByNameAndOrganization_Id(name, organizationId)) {
            throw new RuntimeException("Project name already exists in this organization");
        }

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        Project project = Project.builder()
                .name(name)
                .organization(organization)
                .createdAt(LocalDateTime.now())
                .build();

        return projectRepository.save(project);
    }

    public List<Project> getProjectsByOrganization(UUID organizationId) {
        return projectRepository.findByOrganization_Id(organizationId);
    }

    public Project getProjectById(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
    }
}