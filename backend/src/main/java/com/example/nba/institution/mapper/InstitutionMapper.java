package com.example.nba.institution.mapper;

import com.example.nba.institution.dto.InstitutionRequest;
import com.example.nba.institution.dto.InstitutionResponse;
import com.example.nba.institution.entity.Institution;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** MapStruct mapper between Institution entity and its DTOs. */
@Mapper(componentModel = "spring")
public interface InstitutionMapper {

    Institution toEntity(InstitutionRequest request);

    InstitutionResponse toResponse(Institution entity);

    /** Applies non-null request fields onto an existing entity (partial update semantics). */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(InstitutionRequest request, @MappingTarget Institution entity);
}
