package com.example.nba.institution.dto;

import com.example.nba.institution.entity.InstitutionStatus;

import java.time.Instant;
import java.util.UUID;

/** Institution representation returned by the API. */
public record InstitutionResponse(
        UUID id,
        String name,
        String code,
        String type,
        String address,
        String city,
        String state,
        String country,
        String website,
        String contactEmail,
        String contactPhone,
        InstitutionStatus status,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy
) {
}
