package com.example.nba.program.repository;

import com.example.nba.program.entity.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface ProgramRepository
        extends JpaRepository<Program, UUID>, JpaSpecificationExecutor<Program> {

    boolean existsByDepartmentIdAndCodeIgnoreCase(UUID departmentId, String code);

    boolean existsByDepartmentIdAndCodeIgnoreCaseAndIdNot(UUID departmentId, String code, UUID id);

    long countByDepartmentId(UUID departmentId);
}
