package com.example.aitracker.project;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findByOrganization_Id(UUID organizationId);

    boolean existsByNameAndOrganization_Id(String name, UUID organizationId);
}