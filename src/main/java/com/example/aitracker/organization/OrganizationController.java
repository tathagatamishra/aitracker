package com.example.aitracker.organization;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @PostMapping
    public ResponseEntity<Organization> createOrganization(
            @RequestParam String name,
            @RequestParam UUID ownerUserId
    ) {
        Organization organization = organizationService.createOrganization(name, ownerUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(organization);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Organization> getOrganizationById(@PathVariable UUID id) {
        return ResponseEntity.ok(organizationService.getOrganizationById(id));
    }

    @GetMapping("/owner/{ownerUserId}")
    public ResponseEntity<List<Organization>> getOrganizationsByOwner(@PathVariable UUID ownerUserId) {
        return ResponseEntity.ok(organizationService.getOrganizationsByOwner(ownerUserId));
    }
}