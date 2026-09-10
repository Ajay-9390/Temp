package com.example.nba.accreditation.repository;

import com.example.nba.accreditation.entity.AccreditationCycle;
import com.example.nba.accreditation.entity.AccreditationCycleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccreditationCycleRepository extends JpaRepository<AccreditationCycle, UUID> {

    List<AccreditationCycle> findByProgramIdOrderByApplicationYearDesc(UUID programId);

    boolean existsByProgramIdAndStatusIn(UUID programId, List<AccreditationCycleStatus> statuses);
}
