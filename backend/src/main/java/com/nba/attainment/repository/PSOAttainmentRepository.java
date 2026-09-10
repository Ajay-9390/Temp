package com.nba.attainment.repository;

import com.nba.attainment.entity.PSOAttainment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence contract for PSO attainment records.
 */
@Repository
public interface PSOAttainmentRepository extends JpaRepository<PSOAttainment, UUID> {

    List<PSOAttainment> findByProgramIdAndAcademicYear(UUID programId, String academicYear);

    List<PSOAttainment> findByPsoId(UUID psoId);

    List<PSOAttainment> findByPsoIdAndAcademicYear(UUID psoId, String academicYear);

    Optional<PSOAttainment> findByProgramIdAndPsoIdAndAcademicYearAndCalculationVersion(
            UUID programId, UUID psoId, String academicYear, String calculationVersion);

    boolean existsByProgramIdAndPsoIdAndAcademicYearAndCalculationVersion(
            UUID programId, UUID psoId, String academicYear, String calculationVersion);

    List<PSOAttainment> findByProgramId(UUID programId);

    @Modifying
    @Query("DELETE FROM PSOAttainment p WHERE p.programId = :programId " +
           "AND p.academicYear = :academicYear " +
           "AND p.calculationVersion = :version")
    int deleteByProgramIdAndAcademicYearAndCalculationVersion(
            @Param("programId") UUID programId,
            @Param("academicYear") String academicYear,
            @Param("version") String version);

    /**
     * Atomic upsert using PostgreSQL ON CONFLICT DO UPDATE.
     */
    @Modifying
    @Query(value = """
            INSERT INTO pso_attainment
                (id, program_id, pso_id, pso_code, academic_year,
                 direct_attainment, indirect_attainment, final_attainment,
                 calculation_method, calculation_version, calculation_trace,
                 created_at, updated_at)
            VALUES
                (gen_random_uuid(), :programId, :psoId, :psoCode, :academicYear,
                 :directAttainment, :indirectAttainment, :finalAttainment,
                 :calculationMethod, :calculationVersion, CAST(:calculationTrace AS jsonb),
                 now(), now())
            ON CONFLICT (program_id, pso_id, academic_year, calculation_version)
            DO UPDATE SET
                pso_code            = EXCLUDED.pso_code,
                direct_attainment   = EXCLUDED.direct_attainment,
                indirect_attainment = EXCLUDED.indirect_attainment,
                final_attainment    = EXCLUDED.final_attainment,
                calculation_trace   = EXCLUDED.calculation_trace,
                updated_at          = now()
            """, nativeQuery = true)
    void upsert(
            @Param("programId")          UUID   programId,
            @Param("psoId")              UUID   psoId,
            @Param("psoCode")            String psoCode,
            @Param("academicYear")       String academicYear,
            @Param("directAttainment")   Double directAttainment,
            @Param("indirectAttainment") Double indirectAttainment,
            @Param("finalAttainment")    Double finalAttainment,
            @Param("calculationMethod")  String calculationMethod,
            @Param("calculationVersion") String calculationVersion,
            @Param("calculationTrace")   String calculationTrace);
}
