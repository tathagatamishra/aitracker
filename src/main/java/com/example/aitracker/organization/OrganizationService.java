package com.example.aitracker.organization;

import com.example.aitracker.user.User;
import com.example.aitracker.user.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    public OrganizationService(OrganizationRepository organizationRepository, UserRepository userRepository) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
    }

    public Organization createOrganization(String name, UUID ownerUserId) {
        if (organizationRepository.existsByName(name)) {
            throw new RuntimeException("Organization name already exists");
        }

        User owner = userRepository.findById(ownerUserId)
                .orElseThrow(() -> new RuntimeException("Owner user not found"));

        Organization organization = Organization.builder()
                .name(name)
                .owner(owner)
                .createdAt(LocalDateTime.now())
                .build();

        return organizationRepository.save(organization);
    }

    public List<Organization> getOrganizationsByOwner(UUID ownerUserId) {
        return organizationRepository.findByOwner_Id(ownerUserId);
    }

    public Organization getOrganizationById(UUID id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
    }
}