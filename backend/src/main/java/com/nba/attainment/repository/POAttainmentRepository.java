package com.nba.attainment.repository;

import com.nba.attainment.entity.POAttainment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence contract for PO attainment records.
 *
 * <p>Responsibilities: read, write, update, and delete {@link POAttainment}
 * rows. This repository does NOT perform calculations, orchestration, or
 * data-fetching from other modules.
 */
@Repository
public interface POAttainmentRepository extends JpaRepository<POAttainment, UUID> {

    List<POAttainment> findByProgramIdAndAcademicYear(UUID programId, String academicYear);

    List<POAttainment> findByPoId(UUID poId);

    List<POAttainment> findByPoIdAndAcademicYear(UUID poId, String academicYear);

    Optional<POAttainment> findByProgramIdAndPoIdAndAcademicYearAndCalculationVersion(
            UUID programId, UUID poId, String academicYear, String calculationVersion);

    boolean existsByProgramIdAndPoIdAndAcademicYearAndCalculationVersion(
            UUID programId, UUID poId, String academicYear, String calculationVersion);

    List<POAttainment> findByProgramId(UUID programId);

    @Modifying
    @Query("DELETE FROM POAttainment p WHERE p.programId = :programId " +
           "AND p.academicYear = :academicYear " +
           "AND p.calculationVersion = :version")
    int deleteByProgramIdAndAcademicYearAndCalculationVersion(
            @Param("programId") UUID programId,
            @Param("academicYear") String academicYear,
            @Param("version") String version);

    /**
     * Atomic upsert using PostgreSQL ON CONFLICT DO UPDATE.
     * Prevents duplicate key errors when the same calculation is re-run.
     */
    @Modifying
    @Query(value = """
            INSERT INTO po_attainment
                (id, program_id, po_id, po_code, academic_year,
                 direct_attainment, indirect_attainment, final_attainment,
                 calculation_method, calculation_version, calculation_trace,
                 created_at, updated_at)
            VALUES
                (gen_random_uuid(), :programId, :poId, :poCode, :academicYear,
                 :directAttainment, :indirectAttainment, :finalAttainment,
                 :calculationMethod, :calculationVersion, CAST(:calculationTrace AS jsonb),
                 now(), now())
            ON CONFLICT (program_id, po_id, academic_year, calculation_version)
            DO UPDATE SET
                po_code             = EXCLUDED.po_code,
                direct_attainment   = EXCLUDED.direct_attainment,
                indirect_attainment = EXCLUDED.indirect_attainment,
                final_attainment    = EXCLUDED.final_attainment,
                calculation_trace   = EXCLUDED.calculation_trace,
                updated_at          = now()
            """, nativeQuery = true)
    void upsert(
            @Param("programId")          UUID   programId,
            @Param("poId")               UUID   poId,
            @Param("poCode")             String poCode,
            @Param("academicYear")       String academicYear,
            @Param("directAttainment")   Double directAttainment,
            @Param("indirectAttainment") Double indirectAttainment,
            @Param("finalAttainment")    Double finalAttainment,
            @Param("calculationMethod")  String calculationMethod,
            @Param("calculationVersion") String calculationVersion,
            @Param("calculationTrace")   String calculationTrace);
}
