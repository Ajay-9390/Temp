package com.example.nba.academic.semester.repository;

import com.example.nba.academic.semester.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SemesterRepository extends JpaRepository<Semester, UUID> {

    List<Semester> findByAcademicYearIdOrderBySemesterNumberAsc(UUID academicYearId);

    boolean existsByAcademicYearIdAndSemesterNumber(UUID academicYearId, Integer semesterNumber);

    boolean existsByAcademicYearIdAndSemesterNumberAndIdNot(UUID academicYearId, Integer semesterNumber, UUID id);

    long countByAcademicYearId(UUID academicYearId);
}
