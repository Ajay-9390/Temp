package com.example.nba.institution.repository;

import com.example.nba.institution.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface InstitutionRepository
        extends JpaRepository<Institution, UUID>, JpaSpecificationExecutor<Institution> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, UUID id);
}
