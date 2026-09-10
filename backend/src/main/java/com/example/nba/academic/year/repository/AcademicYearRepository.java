package com.example.nba.academic.year.repository;

import com.example.nba.academic.AcademicLifecycleStatus;
import com.example.nba.academic.year.entity.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AcademicYearRepository extends JpaRepository<AcademicYear, UUID> {

    List<AcademicYear> findByProgramIdOrderByStartDateDesc(UUID programId);

    boolean existsByProgramIdAndNameIgnoreCase(UUID programId, String name);

    boolean existsByProgramIdAndNameIgnoreCaseAndIdNot(UUID programId, String name, UUID id);

    Optional<AcademicYear> findFirstByProgramIdAndStatus(UUID programId, AcademicLifecycleStatus status);

    List<AcademicYear> findByProgramIdAndStatusAndIdNot(UUID programId, AcademicLifecycleStatus status, UUID id);

    List<AcademicYear> findByProgramId(UUID programId);
}
