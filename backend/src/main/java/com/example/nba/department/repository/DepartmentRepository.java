package com.example.nba.department.repository;

import com.example.nba.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DepartmentRepository
        extends JpaRepository<Department, UUID>, JpaSpecificationExecutor<Department> {

    boolean existsByInstitutionIdAndCodeIgnoreCase(UUID institutionId, String code);

    boolean existsByInstitutionIdAndCodeIgnoreCaseAndIdNot(UUID institutionId, String code, UUID id);

    long countByInstitutionId(UUID institutionId);

    /** Department ids whose name or code matches the search term (for program cross-search). */
    @Query("select d.id from Department d "
            + "where lower(d.name) like lower(concat('%', :q, '%')) "
            + "or lower(d.code) like lower(concat('%', :q, '%'))")
    List<UUID> findIdsByNameOrCodeLike(@Param("q") String q);
}
