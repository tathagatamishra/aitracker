package com.example.aitracker.organization;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    List<Organization> findByOwner_Id(UUID ownerId);

    boolean existsByName(String name);
}